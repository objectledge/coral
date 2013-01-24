package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.query.ResourceQueryHandler;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.SchemaIntegrityException;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.Persistence;

/**
 * An implementation of {@ResourceHandler} interface using the
 * <code>PersistenceService</code>.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResourceHandler.java,v 1.19 2008-01-01 22:36:16 rafal Exp $
 */
public class PersistentResourceHandler<T extends Resource>
    extends StandardResourceHandler<T>
{
    private static final ResourceQueryHandler queryHandler = new PersistentResourceQueryHandler();

    // instance variables ////////////////////////////////////////////////////

    /** The persistence. */
    private final Persistence persistence;

    private final PersistentSchemaHandler<T> schemaHandler;

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
        if(resourceClass.getDbTable() == null)
        {
            throw new SchemaIntegrityException("no database table defined for class "
                + resourceClass.getName());
        }
        this.persistence = persistence;
        this.schemaHandler = new PersistentSchemaHandler<T>(resourceClass, persistence);
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
        schemaHandler.addAttribute(attribute, value, conn);
        revert(resourceClass, conn);
    }

    static String columnName(AttributeDefinition<?> attr)
        throws SQLException
    {
        String name = attr.getDbColumn();
        return name != null ? name : attr.getName();
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
        schemaHandler.deleteAttribute(attribute, conn);
        revert(resourceClass, conn);
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
        schemaHandler.addParentClass(parent, attributes, conn);
        revert(resourceClass, conn);
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
        schemaHandler.deleteParentClass(parent, conn);
        revert(resourceClass, conn);
    }

    @Override
    public void setDbTable(String oldTable, String newTable)
        throws SQLException
    {
        schemaHandler.setDbTable(oldTable, newTable);
    }

    @Override
    public void setDbColumn(AttributeDefinition<?> attr, String oldColumn, String newColumn)
        throws SQLException
    {
        schemaHandler.setDbColumn(attr, oldColumn, newColumn);
    }

    public <A> A loadValue(AttributeDefinition<A> attribute, long aId)
    {
        if(Entity.class.isAssignableFrom(attribute.getAttributeClass().getJavaClass()))
        {
            AttributeHandler<A> handler = attribute.getAttributeClass().getHandler();
            return handler.toAttributeValue(Long.toString(aId));
        }
        else
        {
            return super.loadValue(attribute, aId);
        }
    }

    // implementation ////////////////////////////////////////////////////////

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
            Map<ResourceClass<?>, InputRecord> resData = data.get(delegate.getIdObject());
            if(resData == null)
            {
                resData = new HashMap<>();
                data.put(delegate.getIdObject(), resData);
            }
            resData.putAll(getInputRecords(delegate));
        }
        return data;
    }

    private Map<ResourceClass<?>, InputRecord> getInputRecords(Resource delegate)
        throws SQLException
    {
        Map<ResourceClass<?>, InputRecord> map = new HashMap<ResourceClass<?>, InputRecord>();
        if(tableShouldExist(delegate.getResourceClass()))
        {
            map.put(delegate.getResourceClass(),
                getInputRecord(delegate, delegate.getResourceClass()));
        }
        for(ResourceClass<?> parentClass : delegate.getResourceClass().getParentClasses())
        {
            if(parentClass.getDbTable() != null && tableShouldExist(parentClass))
            {
                map.put(parentClass, getInputRecord(delegate, parentClass));
            }
        }
        return map;
    }

    private boolean tableShouldExist(ResourceClass<?> rClass)
    {
        boolean tableShouldExist = false;
        for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                tableShouldExist = true;
            }
        }
        return tableShouldExist;
    }

    private InputRecord getInputRecord(Resource delegate, final ResourceClass<?> rClass)
        throws SQLException
    {
        List<InputRecord> irs = persistence.loadInputRecords(
            PersistentResourceHelper.getRetrieveView(rClass), "resource_id = ?", delegate.getId());
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
        WeakHashMap<ResourceAttributesSupport, Object> rset;
        synchronized(cache)
        {
            rset = cache.get(rc);
        }
        Map<Long, Map<ResourceClass<?>, InputRecord>> data = new HashMap<Long, Map<ResourceClass<?>, InputRecord>>();
        if(rset != null)
        {
            synchronized(rset)
            {
                Set<ResourceAttributesSupport> orig = new HashSet<ResourceAttributesSupport>(
                    rset.keySet());
                for(ResourceAttributesSupport r : orig)
                {
                    data.put(r.getDelegate().getIdObject(), getInputRecords(r.getDelegate()));
                }
            }
        }
        return data;
    }

    @Override
    public ResourceQueryHandler getQueryHandler()
    {
        return queryHandler;
    }

    @Override
    protected void create(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Map<AttributeDefinition<?>, ?> attributes, Connection conn)
        throws SQLException
    {
        PersistentResourceHelper helper = new PersistentResourceHelper(delegate, instance,
            persistence);
        for(ResourceClass<?> rClass : classes)
        {
            helper.create(rClass, attributes, conn);
        }
    }

    @Override
    protected void retrieve(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Object data, Connection conn)
        throws SQLException
    {
        PersistentResourceHelper helper = new PersistentResourceHelper(delegate, instance,
            persistence);
        helper.retrieve(data, conn, classes);
    }

    @Override
    protected void update(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Connection conn)
        throws SQLException

    {
        PersistentResourceHelper helper = new PersistentResourceHelper(delegate, instance,
            persistence);
        for(ResourceClass<?> rClass : classes)
        {
            helper.update(rClass, conn);
        }
    }

    @Override
    protected void revert(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Object data, Connection conn)
        throws SQLException
    {
        PersistentResourceHelper helper = new PersistentResourceHelper(delegate, instance,
            persistence);
        helper.revert(data, conn, classes);
    }

    @Override
    protected void delete(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Connection conn)
        throws SQLException
    {
        PersistentResourceHelper helper = new PersistentResourceHelper(delegate, instance,
            persistence);
        for(ResourceClass<?> rClass : classes)
        {
            helper.delete(rClass, conn);
        }
    }
}
