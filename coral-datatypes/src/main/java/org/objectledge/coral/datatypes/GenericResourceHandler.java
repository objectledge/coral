package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.query.ResourceQueryHandler;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;

/**
 * Handles persistence of {@link GenericResource} objects.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResourceHandler.java,v 1.23 2008-01-01 22:36:16 rafal Exp $
 */
public class GenericResourceHandler<T extends Resource>
    extends StandardResourceHandler<T>
{
    private static final ResourceQueryHandler queryHandler = new GenericResourceQueryHandler();

    private final GenericSchemaHandler<T> schemaHandler;

    // Member objects ////////////////////////////////////////////////////////

    /**
     * The constructor.
     * 
     * @param coralSchema the coral schema.
     * @param resourceClass the resource class.
     * @param database the database.
     * @param instantiator the instantiator.
     * @param cacheFactory the cache factory.
     * @param logger the logger.
     */
    public GenericResourceHandler(CoralSchema coralSchema, ResourceClass<T> resourceClass,
        Database database, Instantiator instantiator, CacheFactory cacheFactory, Logger logger)
    {
        super(coralSchema, instantiator, resourceClass, database, cacheFactory, logger);
        schemaHandler = new GenericSchemaHandler<T>(resourceClass);
    }

    // Schema operations /////////////////////////////////////////////////////////////////////////

    @Override
    public <A> void addAttribute(AttributeDefinition<A> attribute, A value, Connection conn)
        throws ValueRequiredException, SQLException
    {
        schemaHandler.addAttribute(attribute, value, conn);
        revert(resourceClass, conn);
    }

    @Override
    public void deleteAttribute(AttributeDefinition<?> attribute, Connection conn)
        throws SQLException
    {
        schemaHandler.deleteAttribute(attribute, conn);
        revert(resourceClass, conn);
    }

    @Override
    public void addParentClass(ResourceClass<?> parent, Map<AttributeDefinition<?>, ?> attributes,
        Connection conn)
        throws ValueRequiredException, SQLException
    {
        schemaHandler.addParentClass(parent, attributes, conn);
        revert(resourceClass, conn);
    }

    @Override
    public void deleteParentClass(ResourceClass<?> parent, Connection conn)
        throws SQLException
    {
        schemaHandler.deleteParentClass(parent, conn);
        revert(resourceClass, conn);
    }

    // Resource handler interface ////////////////////////////////////////////

    /**
     * Retrieve the data keys.
     * 
     * @param delegate the delegate resource.
     * @param conn the connection.
     * @return the map of data keys.
     * @throws SQLException if happens.
     */
    public Object getData(Resource delegate, Connection conn, Object prev)
        throws SQLException
    {
        Map<Long, Map<AttributeDefinition<?>, Long>> data;
        if(prev == null)
        {
            data = new HashMap<Long, Map<AttributeDefinition<?>, Long>>();
        }
        else
        {
            @SuppressWarnings("unchecked")
            final Map<Long, Map<AttributeDefinition<?>, Long>> cast = (Map<Long, Map<AttributeDefinition<?>, Long>>)prev;
            data = cast;
        }

        Map<AttributeDefinition<?>, Long> dataKeys = new HashMap<AttributeDefinition<?>, Long>();
        Map<AttributeDefinition<?>, Long> resData = data.get(delegate.getIdObject());
        if(resData == null)
        {
            resData = new HashMap<>();
            data.put(delegate.getIdObject(), resData);
        }
        PreparedStatement stmt = conn
            .prepareStatement("SELECT attribute_definition_id, data_key FROM coral_generic_resource WHERE resource_id = ?");
        ResultSet rs = null;
        try
        {
            stmt.setLong(1, delegate.getId());
            rs = stmt.executeQuery();
            while(rs.next())
            {
                dataKeys.put(coralSchema.getAttribute(rs.getLong(1)), new Long(rs.getLong(2)));
            }
            resData.putAll(dataKeys);
            return data;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("corrupted data", e);
        }
        finally
        {
            DatabaseUtils.close(rs);
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getData(ResourceClass<?> rc, Connection conn)
        throws SQLException
    {
        Map<Long, Map<AttributeDefinition<?>, Long>> keyMap = new HashMap<Long, Map<AttributeDefinition<?>, Long>>();
        PreparedStatement stmt = conn
            .prepareStatement("SELECT resource_id, attribute_definition_id, data_key FROM coral_generic_resource "
                + "JOIN coral_resource USING(resource_id) WHERE resource_class_id = ?");
        ResultSet rs = null;
        try
        {
            stmt.setLong(1, rc.getId());
            rs = stmt.executeQuery();
            Map<AttributeDefinition<?>, Long> dataKeys = null;
            Long resId = null;
            while(rs.next())
            {
                if(dataKeys == null || resId == null || resId.longValue() != rs.getLong(1))
                {
                    resId = new Long(rs.getLong(1));
                    dataKeys = new HashMap<AttributeDefinition<?>, Long>();
                    keyMap.put(resId, dataKeys);
                }
                dataKeys.put(coralSchema.getAttribute(rs.getLong(2)), new Long(rs.getLong(3)));
            }
            return keyMap;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("corrupted data", e);
        }
        finally
        {
            DatabaseUtils.close(rs);
            DatabaseUtils.close(stmt);
        }
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
        GenericResourceHelper helper = new GenericResourceHelper(delegate, instance);
        helper.create(classes, attributes, conn);
    }

    @Override
    protected void retrieve(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Object data, Connection conn)
        throws SQLException
    {
        GenericResourceHelper helper = new GenericResourceHelper(delegate, instance);
        helper.retrieve(classes, data, conn);
    }

    @Override
    protected void update(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Connection conn)
        throws SQLException

    {
        GenericResourceHelper helper = new GenericResourceHelper(delegate, instance);
        helper.update(classes, conn);
    }

    @Override
    protected void revert(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Object data, Connection conn)
        throws SQLException
    {
        GenericResourceHelper helper = new GenericResourceHelper(delegate, instance);
        helper.revert(classes, data, conn);
    }

    @Override
    protected void delete(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Connection conn)
        throws SQLException
    {
        GenericResourceHelper helper = new GenericResourceHelper(delegate, instance);
        helper.delete(classes, conn);
    }
}
