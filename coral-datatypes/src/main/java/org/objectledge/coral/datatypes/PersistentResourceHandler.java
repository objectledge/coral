package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.query.ResourceQueryHandler;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.DefaultOutputRecord;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;

/**
 * An implementation of {@ResourceHandler} interface using the
 * <code>PersistenceService</code>.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResourceHandler.java,v 1.19 2008-01-01 22:36:16 rafal Exp $
 */
public class PersistentResourceHandler<T extends PersistentResource>
    extends AbstractResourceHandler<T>
{
    private static final ResourceQueryHandler queryHandler = new PersistentResourceQueryHandler();

    // instance variables ////////////////////////////////////////////////////

    /** The persistence. */
    private Persistence persistence;

    /**
     * Constructor.
     * 
     * @param coralSchema the coral schema.
     * @param resourceClass the resource class.
     * @param database the database.
     * @param persistence the persistence.
     * @param instantiator the instantiator.
     * @param cacheFactory the cache factory.
     * @param logger the logger.
     * @throws Exception if there is a problem instantiating an resource object.
     */
    public PersistentResourceHandler(CoralSchema coralSchema, CoralSecurity coralSecurity,
        ResourceClass<T> resourceClass, Database database, Persistence persistence,
        Instantiator instantiator, CacheFactory cacheFactory, Logger logger)
        throws Exception
    {
        super(coralSchema, instantiator, resourceClass, database, cacheFactory, logger);
        this.persistence = persistence;
    }

    // schema operations (not supported) /////////////////////////////////////

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
            if(value != null && (repr.isCustom() || tableShouldExist))
            {
                addAttributeValues(attribute, value, repr, conn, tableShouldExist);
                revert(resourceClass, conn);
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
        revert(resourceClass, conn);
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

            Persistent view = PersistentResource.getCreateView(parent, attributes, conn);
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

    // implementation ////////////////////////////////////////////////////////

    /**
     * Checks if the passed resource is really a {@link Persistent}. implemenation
     * 
     * @param resource the resource to check.
     */
    protected void checkResource(Resource resource)
    {
        if(!(resource instanceof PersistentResource))
        {
            throw new ClassCastException("PersistenceResourceHanler won't operate on "
                + resource.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Object getData(Resource delegate, Connection conn, Object prev)
        throws SQLException
    {
        Map<Long, Map<ResourceClass<?>, InputRecord>> data;
        if(prev == null)
        {
            data = new HashMap<Long, Map<ResourceClass<?>, InputRecord>>();
        }
        else
        {
            @SuppressWarnings("unchecked")
            final Map<Long, Map<ResourceClass<?>, InputRecord>> cast = (Map<Long, Map<ResourceClass<?>, InputRecord>>)prev;
            data = cast;
        }
        if(delegate.getResourceClass().getDbTable() != null)
        {
            data.put(delegate.getIdObject(), getInputRecords(delegate));
        }
        return data;
    }

    private Map<ResourceClass<?>, InputRecord> getInputRecords(Resource delegate)
        throws SQLException
    {
        Map<ResourceClass<?>, InputRecord> map = new HashMap<ResourceClass<?>, InputRecord>();
        map.put(delegate.getResourceClass(), getInputRecord(delegate, delegate.getResourceClass()));
        for(ResourceClass<?> parentClass : delegate.getResourceClass().getParentClasses())
        {
            if(parentClass.getDbTable() != null)
            {
                map.put(parentClass, getInputRecord(delegate, parentClass));
            }
        }
        return map;
    }

    private InputRecord getInputRecord(Resource delegate, final ResourceClass<?> rClass)
        throws SQLException
    {
        List<InputRecord> irs = persistence.loadInputRecords(
            PersistentResource.getRetrieveView(rClass), "resource_id = ?", delegate.getId());
        if(irs.isEmpty())
        {
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & AttributeFlags.REQUIRED) != 0)
                {
                    throw new SQLException("missing data for "
                        + delegate.getResourceClass().getName() + " WHERE resource_id = "
                        + delegate.getIdString());
                }
            }
            return null;
        }
        return irs.get(0);
    }

    /**
     * {@inheritDoc}
     */
    protected Object getData(ResourceClass<?> rc, Connection conn)
        throws SQLException
    {
        WeakHashMap<Resource, Object> rset;
        synchronized(cache)
        {
            rset = cache.get(rc);
        }
        Map<Long, Map<ResourceClass<?>, InputRecord>> data = new HashMap<Long, Map<ResourceClass<?>, InputRecord>>();
        if(rset != null)
        {
            synchronized(rset)
            {
                Set<Resource> orig = new HashSet<Resource>(rset.keySet());
                for(Resource r : orig)
                {
                    data.put(r.getIdObject(), getInputRecords(r));
                }
            }
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object getData(Connection conn)
        throws SQLException
    {
        return null;
    }

    /**
     * @return Returns the persistence.
     */
    Persistence getPersistence()
    {
        return persistence;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getFallbackResourceImplClass()
    {
        return PersistentResource.class;
    }

    @Override
    public ResourceQueryHandler getQueryHandler()
    {
        return queryHandler;
    }
}
