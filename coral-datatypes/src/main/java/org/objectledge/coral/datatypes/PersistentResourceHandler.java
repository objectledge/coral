package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.Instantiator;
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
            if(repr.isCustom() && value != null)
            {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE "
                    + attribute.getDeclaringClass().getDbTable() + " SET " + attribute.getName()
                    + " = ? WHERE resource_id = ?");
                try
                {
                    ResultSet rset = stmt.executeQuery("SELECT resource_id FROM "
                        + attribute.getDeclaringClass().getDbTable());
                    try
                    {
                        boolean batchReady = false;
                        while(rset.next())
                        {
                            pstmt.setLong(1,
                                attribute.getAttributeClass().getHandler().create(value, conn));
                            pstmt.setLong(2, rset.getLong(1));
                            pstmt.addBatch();
                            batchReady = true;
                        }
                        if(batchReady)
                        {
                            pstmt.executeBatch();
                        }
                    }
                    finally
                    {
                        rset.close();
                    }
                }
                finally
                {
                    pstmt.close();
                }
                revert(resourceClass, conn);
            }
        }
        finally
        {
            stmt.close();
        }
    }

    private <A> void columnSpec(AttributeDefinition<A> attribute, A value,
        CoralAttributeMapping repr, StringBuilder buff)
    {
        buff.append(attribute.getName()).append(" ");
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
    {
        buff.append("CONSTRAINT fk_").append(attribute.getDeclaringClass().getDbTable())
            .append("_").append(attribute.getName());
        buff.append(" FOREIGN KEY (").append(attribute.getName()).append(") ");
        buff.append("\n REFERENCES ").append(repr.getFkTable());
        buff.append("(").append(repr.getFkKeyColumn()).append(")");
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
            StringBuilder buff = new StringBuilder();
            if(shouldDropTable)
            {
                buff.append("DROP TABLE ").append(attribute.getDeclaringClass().getDbTable());
            }
            else
            {
                if(repr.isFk())
                {
                    buff.append("ALTER TABLE ").append(attribute.getDeclaringClass().getDbTable());
                    buff.append("\n DROP CONSTRAINT fk_")
                        .append(attribute.getDeclaringClass().getDbTable()).append("_")
                        .append(attribute.getName());
                    stmt.execute(buff.toString());
                    buff.setLength(0);

                }
                buff.append("ALTER TABLE ").append(attribute.getDeclaringClass().getDbTable())
                    .append("\n DROP COLUMN ").append(attribute.getName());
            }
            stmt.execute(buff.toString());
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
        throw new UnsupportedOperationException("schema modifications are not supported"
            + " for this resource class");
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
        WeakHashMap<AbstractResource, Object> rset;
        synchronized(cache)
        {
            rset = cache.get(rc);
        }
        Map<Long, Map<ResourceClass<?>, InputRecord>> data = new HashMap<Long, Map<ResourceClass<?>, InputRecord>>();
        if(rset != null)
        {
            synchronized(rset)
            {
                Set<AbstractResource> orig = new HashSet<AbstractResource>(rset.keySet());
                for(AbstractResource r : orig)
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
}
