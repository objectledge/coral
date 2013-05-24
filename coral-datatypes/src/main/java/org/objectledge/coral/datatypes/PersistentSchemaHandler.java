package org.objectledge.coral.datatypes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.persistence.DefaultOutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;

public class PersistentSchemaHandler<T extends Resource>
{
    private final ResourceClass<T> resourceClass;

    private final Persistence persistence;

    public PersistentSchemaHandler(ResourceClass<T> resourceClass, Persistence persistence)
    {
        this.resourceClass = resourceClass;
        this.persistence = persistence;
    }

    /**
     * Called when an attribute is added to a structured resource class.
     * <p>
     * Concrete resource classes will probably deny this operation by throwing
     * <code>UnsupportedOperationException</code>.
     * </p>
     * 
     * @param attribute the new attribute.
     * @param value the initial value to be set for existing instances of this class.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *        part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod should consider rolling
     *         back the whole transaction.
     * @throws ValueRequiredException if <code>null</code> value was provided for a REQUIRED
     *         attribute.
     */
    public <A> void addAttribute(AttributeDefinition<A> attribute, A value, Connection conn)
        throws ValueRequiredException, SQLException
    {
        boolean tableShouldExist = false;
        for(AttributeDefinition<?> attr : attribute.getDeclaringClass().getDeclaredAttributes())
        {
            if(!attr.equals(attribute) && (attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                tableShouldExist = true;
            }
        }
        Statement stmt = conn.createStatement();
        try
        {
            CoralAttributeMapping repr;
            repr = persistence.load(CoralAttributeMapping.FACTORY, attribute.getAttributeClass()
                .getId());
            StringBuilder buff = new StringBuilder();
            if(!tableShouldExist)
            {
                buff.append("CREATE TABLE ");
                buff.append(attribute.getDeclaringClass().getDbTable());
                buff.append(" (\n resource_id BIGINT,\n ");
                columnSpec(attribute, value, repr, buff);
                buff.append(",\n PRIMARY KEY (resource_id)");
                if(repr.isFk())
                {
                    buff.append(",\n ");
                    constraintSpec(attribute, repr, buff);
                }
                buff.append("\n)");
                stmt.execute(buff.toString());
            }
            else
            {
                buff.append("ALTER TABLE ");
                buff.append(attribute.getDeclaringClass().getDbTable());
                buff.append("\n ADD COLUMN ");
                columnSpec(attribute, value, repr, buff);
                stmt.execute(buff.toString());
                if(repr.isFk())
                {
                    buff.setLength(0);
                    buff.append("ALTER TABLE ");
                    buff.append(attribute.getDeclaringClass().getDbTable());
                    buff.append("\n ADD ");
                    constraintSpec(attribute, repr, buff);
                    stmt.execute(buff.toString());
                }
            }
            if(value != null && repr.isCustom())
            {
                addAttributeValues(attribute, value, repr, conn, tableShouldExist);
                if((attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
                {
                    buff.setLength(0);
                    buff.append("ALTER TABLE ");
                    buff.append(attribute.getDeclaringClass().getDbTable());
                    buff.append("\n ALTER COLUMN ");
                    buff.append(columnName(attribute));
                    buff.append("\n NOT NULL");
                }
            }
        }
        finally
        {
            stmt.close();
        }
    }

    static String columnName(AttributeDefinition<?> attr)
        throws SQLException
    {
        String name = attr.getDbColumn();
        return name != null ? name : attr.getName();
    }

    private <A> void columnSpec(AttributeDefinition<A> attribute, A value,
        CoralAttributeMapping repr, StringBuilder buff)
        throws SQLException
    {
        buff.append(columnName(attribute)).append(" ");
        buff.append(repr.getSqlType());
        if(value != null && !repr.isCustom())
        {
            buff.append(" DEFAULT ");
            value = attribute.getAttributeClass().getHandler().toAttributeValue(value);
            buff.append(attribute.getAttributeClass().getHandler().toExternalString(value));
        }
        if((attribute.getFlags() & AttributeFlags.REQUIRED) != 0 && !repr.isCustom())
        {
            buff.append(" NOT NULL");
        }
    }

    private <A> void constraintSpec(AttributeDefinition<A> attribute, CoralAttributeMapping repr,
        StringBuilder buff)
        throws SQLException
    {
        final String columnName = columnName(attribute);
        buff.append("CONSTRAINT fk_").append(attribute.getDeclaringClass().getDbTable())
            .append("_").append(columnName);
        buff.append(" FOREIGN KEY (").append(columnName).append(") ");
        buff.append("\n REFERENCES ").append(repr.getFkTable());
        buff.append("(").append(repr.getFkKeyColumn()).append(")");
    }

    private <A> void addAttributeValues(AttributeDefinition<A> attribute, A value,
        CoralAttributeMapping repr, Connection conn, boolean tableShouldExist)
        throws SQLException
    {
        String sql;
        PreparedStatement outStmt;
        final String columnName = columnName(attribute);
        value = attribute.getAttributeClass().getHandler().toAttributeValue(value);
        if(tableShouldExist)
        {
            sql = "UPDATE " + attribute.getDeclaringClass().getDbTable() + " SET " + columnName
                + " = ? WHERE resource_id = ?";
        }
        else
        {
            sql = "INSERT INTO " + attribute.getDeclaringClass().getDbTable() + " (" + columnName
                + ", resource_id) VALUES (?, ?)";
        }
        outStmt = conn.prepareStatement(sql);
        try
        {
            PreparedStatement inStmt = conn
                .prepareStatement("SELECT resource_id FROM coral_resource WHERE resource_class_id = ?");
            try
            {
                List<ResourceClass<?>> rClasses = new ArrayList<ResourceClass<?>>();
                rClasses.add(attribute.getDeclaringClass());
                rClasses.addAll(Arrays.asList(attribute.getDeclaringClass().getChildClasses()));
                for(ResourceClass<?> rClass : rClasses)
                {
                    inStmt.setLong(1, rClass.getId());
                    ResultSet rset = inStmt.executeQuery();
                    try
                    {
                        boolean batchReady = false;
                        while(rset.next())
                        {
                            if(repr.isCustom())
                            {
                                outStmt.setLong(1, attribute.getAttributeClass().getHandler()
                                    .create(value, conn));
                            }
                            else
                            {
                                DefaultOutputRecord.setValue(1, value, -1, outStmt);
                            }
                            outStmt.setLong(2, rset.getLong(1));
                            outStmt.addBatch();
                            batchReady = true;
                        }
                        if(batchReady)
                        {
                            outStmt.executeBatch();
                        }
                    }
                    finally
                    {
                        rset.close();
                    }
                }
            }
            finally
            {
                inStmt.close();
            }
        }
        finally
        {
            outStmt.close();
        }
    }

    /**
     * Called when an attribute is removed from a structured resource class.
     * <p>
     * Concrete resource classes will probably deny this operation by throwing
     * <code>UnsupportedOperationException</code>.
     * </p>
     * 
     * @param attribute the removed attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *        part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod should consider rolling
     *         back the whole transaction.
     */
    public void deleteAttribute(AttributeDefinition<?> attribute, Connection conn)
        throws SQLException
    {
        boolean shouldDropTable = true;
        for(AttributeDefinition<?> attr : attribute.getDeclaringClass().getDeclaredAttributes())
        {
            if(!attr.equals(attribute) && (attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                shouldDropTable = false;
            }
        }
        Statement stmt = conn.createStatement();
        try
        {
            CoralAttributeMapping repr;
            repr = persistence.load(CoralAttributeMapping.FACTORY, attribute.getAttributeClass()
                .getId());
            if(repr.isCustom())
            {
                deleteAttributeValues(attribute, conn);
            }

            StringBuilder buff = new StringBuilder();
            if(shouldDropTable)
            {
                buff.append("DROP TABLE ").append(attribute.getDeclaringClass().getDbTable());
            }
            else
            {
                final String columnName = columnName(attribute);
                if(repr.isFk())
                {
                    buff.append("ALTER TABLE ").append(attribute.getDeclaringClass().getDbTable());
                    buff.append("\n DROP CONSTRAINT fk_")
                        .append(attribute.getDeclaringClass().getDbTable()).append("_")
                        .append(columnName);
                    stmt.execute(buff.toString());
                    buff.setLength(0);

                }
                buff.append("ALTER TABLE ").append(attribute.getDeclaringClass().getDbTable())
                    .append("\n DROP COLUMN ").append(columnName);
            }
            stmt.execute(buff.toString());
        }
        finally
        {
            stmt.close();
        }
    }

    private <A> void deleteAttributeValues(AttributeDefinition<A> attribute, Connection conn)
        throws SQLException
    {
        String columnName = columnName(attribute);
        StringBuilder buff = new StringBuilder();
        buff.append("SELECT ").append(columnName);
        buff.append("\nFROM ").append(attribute.getDeclaringClass().getDbTable());
        buff.append("\nWHERE ").append(columnName).append(" IS NOT NULL");
        buff.append("\nGROUP BY ").append(columnName);
        Statement stmt = conn.createStatement();
        try
        {
            ResultSet rset = stmt.executeQuery(buff.toString());
            while(rset.next())
            {
                attribute.getAttributeClass().getHandler().delete(rset.getLong(1), conn);
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new SQLException("failed to delete attribute value", e);
        }
        finally
        {
            stmt.close();
        }
    }

    /**
     * Called when a parent class is added to a sturctured resource class.
     * <p>
     * Concrete resource classes will probably deny this operation by throwing
     * <code>UnsupportedOperationException</code>.
     * </p>
     * 
     * @param parent the new parent class.
     * @param attributes the initial values of the attributes. Values are keyed with
     *        {@link AttributeDefinition} objects.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *        part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod should consider rolling
     *         back the whole transaction.
     * @throws ValueRequiredException if values for any of parent class REQUIRED attributes are
     *         missing from <code>attributes</code> map.
     */
    public void addParentClass(ResourceClass<?> parent, Map<AttributeDefinition<?>, ?> attributes,
        Connection conn)
        throws ValueRequiredException, SQLException
    {
        for(AttributeDefinition<?> attr : parent.getAllAttributes())
        {
            if((attr.getFlags() & AttributeFlags.REQUIRED) != 0
                && (attr.getFlags() & AttributeFlags.BUILTIN) == 0 && !attributes.containsKey(attr))
            {
                throw new ValueRequiredException("missing value for REQUIRED attribute "
                    + attr.getName());
            }
        }
        if(!attributes.isEmpty())
        {
            addParentClass(parent, resourceClass, attributes, conn);
            for(ResourceClass<?> child : resourceClass.getChildClasses())
            {
                if(child.getDbTable() != null)
                {
                    addParentClass(parent, child, attributes, conn);
                }
            }
        }
    }

    private void addParentClass(ResourceClass<?> parent, ResourceClass<?> child,
        Map<AttributeDefinition<?>, ?> attributes, Connection conn)
        throws SQLException
    {
        PreparedStatement stmt = conn
            .prepareStatement("SELECT resource_id FROM coral_resource WHERE resource_class_id = ?");
        try
        {
            stmt.setLong(1, child.getId());

            Persistent view = PersistentResourceHelper.getCreateView(
                DummyResourceAttributes.INSTANCE, parent, attributes, conn);
            PreparedStatement out = DefaultOutputRecord.getInsertStatement(view, conn);
            try
            {
                ResultSet rset = stmt.executeQuery();
                try
                {
                    boolean batchReady = false;
                    while(rset.next())
                    {
                        DefaultOutputRecord.refeshInsertStatement(view, out);
                        out.setLong(1, rset.getLong(1));
                        out.addBatch();
                        batchReady = true;
                    }
                    if(batchReady)
                    {
                        out.executeBatch();
                    }
                }
                finally
                {
                    rset.close();
                }
            }
            finally
            {
                out.close();
            }
        }
        finally
        {
            stmt.close();
        }
    }

    /**
     * Called when a parent class is removed from a structured resource class.
     * <p>
     * Concrete resource classes will probably deny this operation by throwing
     * <code>UnsupportedOperationException</code>.
     * </p>
     * 
     * @param parent the new parent class.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *        part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod should consider rolling
     *         back the whole transaction.
     */
    public void deleteParentClass(ResourceClass<?> parent, Connection conn)
        throws SQLException
    {
        deleteAttributeValues(parent, this.resourceClass, conn);
        deleteDataRows(parent, this.resourceClass, conn);
    }

    private void deleteAttributeValues(ResourceClass<?> parent, ResourceClass<?> child,
        Connection conn)
        throws SQLException
    {
        List<AttributeDefinition<?>> customAttrs = new ArrayList<AttributeDefinition<?>>();
        for(AttributeDefinition<?> attr : parent.getDeclaredAttributes())
        {
            if(!attr.getAttributeClass().getHandler().supportsExternalString())
            {
                customAttrs.add(attr);
            }
        }
        if(!customAttrs.isEmpty())
        {
            StringBuilder buff = new StringBuilder();
            buff.append(child.getIdString());
            for(ResourceClass<?> rc : child.getChildClasses())
            {
                buff.append(", ").append(rc.getIdString());
            }
            String classIds = buff.toString();
            buff.setLength(0);

            buff.append("SELECT ");
            Iterator<AttributeDefinition<?>> i = customAttrs.iterator();
            while(i.hasNext())
            {
                buff.append("d.").append(columnName(i.next()));
                if(i.hasNext())
                {
                    buff.append(", ");
                }
            }
            buff.append("\nFROM ").append(parent.getDbTable()).append(" d ");
            buff.append("\nJOIN coral_resource r USING (resource_id)");
            buff.append("\nWHERE r.resource_class_id in (");
            buff.append(classIds).append(")");
            Statement stmt = conn.createStatement();
            try
            {
                ResultSet rset = stmt.executeQuery(buff.toString());
                try
                {
                    while(rset.next())
                    {
                        for(int j = 0; j < customAttrs.size(); j++)
                        {
                            customAttrs.get(j).getAttributeClass().getHandler()
                                .delete(rset.getLong(j + 1), conn);
                        }
                    }
                }
                finally
                {
                    rset.close();
                }
            }
            catch(EntityDoesNotExistException e)
            {
                throw new SQLException("failed to delete attribute value", e);
            }
            finally
            {
                stmt.close();
            }
        }
    }

    private void deleteDataRows(ResourceClass<?> parent, ResourceClass<?> child, Connection conn)
        throws SQLException
    {
        boolean tableShouldExist = false;
        for(AttributeDefinition<?> attr : parent.getDeclaredAttributes())
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                tableShouldExist = true;
            }
        }
        if(parent.getDbTable() == null || !tableShouldExist)
        {
            return;
        }

        StringBuilder buff = new StringBuilder();
        buff.append(child.getIdString());
        for(ResourceClass<?> rc : child.getChildClasses())
        {
            buff.append(", ").append(rc.getIdString());
        }
        String classIds = buff.toString();
        buff.setLength(0);

        buff.append("DELETE FROM ").append(parent.getDbTable());
        buff.append("\nWHERE resource_id IN (");
        buff.append("\nSELECT resource_id");
        buff.append("\nFROM coral_resource");
        buff.append("\nWHERE resource_class_id in (");
        buff.append(classIds).append(")");
        buff.append("\n)");

        Statement stmt = conn.createStatement();
        try
        {
            stmt.execute(buff.toString());
        }
        finally
        {
            stmt.close();
        }
    }

    /**
     * Called when database table for this resource class is changed.
     * 
     * @param oldTable name of the current table.
     * @param newTable name of the requested table.
     */
    public void setDbTable(String oldTable, String newTable)
        throws SQLException
    {
        try(Connection conn = persistence.getDatabase().getConnection();
            Statement stmt = conn.createStatement())
        {
            stmt.execute(String.format("ALTER TABLE %s RENAME TO %s", oldTable, newTable));
        }
    }

    /**
     * Called when database column for one of the declared attributes is changed.
     * 
     * @param attr the attribute being modified.
     * @param oldColumn current column name.
     * @param newColumn requested column name.
     */
    public void setDbColumn(AttributeDefinition<?> attr, String oldColumn, String newColumn)
        throws SQLException
    {
        if(!attr.getDeclaringClass().equals(resourceClass))
        {
            throw new IllegalArgumentException("attempting to delete attribute " + attr.getName()
                + " of class " + attr.getDeclaringClass().getName() + " through hanlder of class "
                + resourceClass.getName());
        }
        try(Connection conn = persistence.getDatabase().getConnection();
            Statement stmt = conn.createStatement())
        {
            stmt.execute(String.format("ALTER TABLE %s RENAME COLUMN %s TO %s",
                resourceClass.getDbTable(), oldColumn, newColumn));
        }
    }

    private static class DummyResourceAttributes
        implements InvocationHandler
    {
        public static final ResourceAttributes INSTANCE = (ResourceAttributes)Proxy
            .newProxyInstance(DummyResourceAttributes.class.getClassLoader(),
                new Class[] { ResourceAttributes.class }, new DummyResourceAttributes());

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
        {
            if(method.getName().equals("getDelegate"))
            {
                return Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[] { Resource.class }, this);
            }
            if(method.getName().equals("getId"))
            {
                return Long.valueOf("-1");
            }
            return null;
        }
    }
}
