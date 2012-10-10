package org.objectledge.coral.datatypes;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;

/**
 * The base class for resource handlers.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 */
public abstract class StandardResourceHandler<T extends Resource>
    implements ResourceHandler<T>
{
    /** The resource class this handler is responsible for. */
    protected ResourceClass<T> resourceClass;

    /** The schema. */
    protected CoralSchema coralSchema;

    /** The instnatiator. */
    protected Instantiator instantiator;

    /** resource sets, keyed by resource class. Resources are kept through weak references. */
    protected Map<ResourceClass<?>, WeakHashMap<ResourceAttributesSupport, Object>> cache = new HashMap<ResourceClass<?>, WeakHashMap<ResourceAttributesSupport, Object>>();

    /** the database. */
    private Database database;

    /** the logger. */
    private Logger logger;

    private final CacheFactory cacheFactory;

    /**
     * The base constructor.
     * 
     * @param coralSchema the coral schema.
     * @param instantiator the instantiator.
     * @param resourceClass the resource class.
     * @param database the database.
     * @param cacheFactory the cache factory.
     * @param logger the logger.
     */
    public StandardResourceHandler(CoralSchema coralSchema, Instantiator instantiator,
        ResourceClass<T> resourceClass, Database database, CacheFactory cacheFactory, Logger logger)
    {
        this.coralSchema = coralSchema;
        this.instantiator = instantiator;
        this.resourceClass = resourceClass;
        this.database = database;
        this.cacheFactory = cacheFactory;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getFallbackResourceImplClass()
    {
        return StandardResource.class;
    }

    /**
     * {@inheritDoc}
     */
    public T create(Resource delegate, Map<AttributeDefinition<?>, ?> attributes, Connection conn)
        throws ValueRequiredException, SQLException
    {
        checkDelegate(delegate);
        checkRequiredAttributes(delegate.getResourceClass(), attributes);
        checkConstraints(attributes);
        T resource = instantiate();
        StandardResource instance = (StandardResource)resource;
        instance.setDelegate(delegate);
        Map<ResourceHandler<?>, Set<ResourceClass<?>>> ch = partitionByHandler(delegate
            .getResourceClass());
        for(ResourceHandler<?> h : ch.keySet())
        {
            ((StandardResourceHandler<?>)h).create(delegate, instance, ch.get(h), attributes, conn);
        }
        addToCache(delegate.getResourceClass(), instance);
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    public T retrieve(Resource delegate, Connection conn, Object data)
        throws SQLException
    {
        checkDelegate(delegate);
        T resource = instantiate();
        StandardResource instance = (StandardResource)resource;
        instance.setDelegate(delegate);
        Map<ResourceHandler<?>, Set<ResourceClass<?>>> ch = partitionByHandler(delegate
            .getResourceClass());
        for(ResourceHandler<?> h : ch.keySet())
        {
            data = ((StandardResourceHandler<?>)h).getData(delegate, conn, data);
        }
        for(ResourceHandler<?> h : ch.keySet())
        {
            ((StandardResourceHandler<?>)h).retrieve(delegate, (StandardResource)resource,
                ch.get(h), data, conn);
        }
        addToCache(delegate.getResourceClass(), instance);
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    public void update(T resource, Connection conn)
        throws SQLException
    {
        checkResource(resource);
        Map<ResourceHandler<?>, Set<ResourceClass<?>>> ch = partitionByHandler(resource
            .getResourceClass());
        for(ResourceHandler<?> h : ch.keySet())
        {
            ((StandardResourceHandler<?>)h).update(resource.getDelegate(),
                (StandardResource)resource, ch.get(h), conn);
        }
        resource.getDelegate().update();
    }

    /**
     * {@inheritDoc}
     */
    public void revert(T resource, Connection conn, Object data)
        throws SQLException
    {
        checkResource(resource);
        Resource delegate = resource.getDelegate();
        checkDelegate(delegate);
        StandardResource instance = (StandardResource)resource;
        instance.resetAttributes();
        Map<ResourceHandler<?>, Set<ResourceClass<?>>> ch = partitionByHandler(resource
            .getResourceClass());
        for(ResourceHandler<?> h : ch.keySet())
        {
            data = ((StandardResourceHandler<?>)h).getData(delegate, conn, data);
        }
        for(ResourceHandler<?> h : ch.keySet())
        {
            ((StandardResourceHandler<?>)h).revert(delegate, (StandardResource)resource, ch.get(h),
                data, conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void delete(Resource resource, Connection conn)
        throws SQLException
    {
        checkResource(resource);
        Resource delegate = resource.getDelegate();
        checkDelegate(delegate);
        Map<ResourceHandler<?>, Set<ResourceClass<?>>> ch = partitionByHandler(resource
            .getResourceClass());
        for(ResourceHandler<?> h : ch.keySet())
        {
            ((StandardResourceHandler<?>)h).delete(delegate, (StandardResource)resource, ch.get(h),
                conn);
        }
        dropFromCache(resource);
    }

    /**
     * Lazy-load attribute value.
     * 
     * @param attribute the attribute definition.
     * @param aId attribute value id.
     * @return attribute value.
     */
    public <A> A loadValue(AttributeDefinition<A> attribute, long aId)
    {
        Connection conn = null;
        try
        {
            conn = getDatabase().getConnection();
            return attribute.getAttributeClass().getHandler().retrieve(aId, conn);
        }
        catch(Exception e)
        {
            throw new BackendException("failed to retrieve attribute value "
                + "(attribute definition = " + attribute.getName() + " , attribute id = " + aId
                + ")", e);
        }
        finally
        {
            DatabaseUtils.close(conn);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void create(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Map<AttributeDefinition<?>, ?> attributes, Connection conn)
        throws SQLException;

    protected abstract void retrieve(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Object data, Connection conn)
        throws SQLException;

    protected abstract void update(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Connection conn)
        throws SQLException;

    protected abstract void revert(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Object data, Connection conn)
        throws SQLException;

    protected abstract void delete(Resource delegate, ResourceAttributesSupport instance,
        Set<ResourceClass<?>> classes, Connection conn)
        throws SQLException;

    // ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve attribute information for a specific resource.
     * 
     * @param delegate the security delegate object.
     * @param conn database connection.
     * @param prev data produced by previous handler, or {@code null} if the handler is first one in
     *        the chain.
     * @return opaque data object.
     * @throws SQLException if information retrieval fails.
     */
    protected abstract Object getData(Resource delegate, Connection conn, Object prev)
        throws SQLException;

    /**
     * Retrieve attribute information for resources of a specific class.
     * 
     * @param rc the resource class.
     * @param conn database connection.
     * @return opaque data object.
     * @throws SQLException if information retrieval fails.
     */
    protected abstract Object getData(ResourceClass<?> rc, Connection conn)
        throws SQLException;

    /**
     * {@inheritDoc}
     */
    public Resource[] getResourceReferences(Resource resource, boolean clearable)
    {
        List<Resource> temp = new ArrayList<Resource>();
        for(AttributeDefinition<?> attr : resourceClass.getAllAttributes())
        {
            AttributeHandler<?> handler = attr.getAttributeClass().getHandler();
            if(handler.containsResourceReferences()
                && resource.isDefined(attr)
                && (clearable || (!handler.isComposite() && (attr.getFlags() & (AttributeFlags.READONLY | AttributeFlags.REQUIRED)) != 0))
                && ((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0))
            {
                Resource[] refs = getResourceReferences(resource, attr);
                temp.addAll(Arrays.asList(refs));
            }
        }
        Resource[] result = new Resource[temp.size()];
        temp.toArray(result);
        return result;
    }

    private <A> Resource[] getResourceReferences(Resource resource, AttributeDefinition<A> attr)
    {
        AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
        return handler.getResourceReferences(resource.get(attr));
    }

    /**
     * {@inheritDoc}
     */
    public void clearResourceReferences(Resource resource)
    {
        for(AttributeDefinition<?> attr : resourceClass.getAllAttributes())
        {
            AttributeHandler<?> handler = attr.getAttributeClass().getHandler();
            if(handler.containsResourceReferences()
                && resource.isDefined(attr)
                && (handler.isComposite() || (attr.getFlags() & (AttributeFlags.READONLY
                    | AttributeFlags.REQUIRED | AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0))
            {
                if(handler.isComposite())
                {
                    clearResourceReferences(resource, attr);
                }
                else
                {
                    try
                    {
                        resource.unset(attr);
                    }
                    catch(Exception e)
                    {
                        throw new BackendException("unexpected exception", e);
                    }
                }
            }
        }
        resource.update();
    }

    private <A> void clearResourceReferences(Resource resource, AttributeDefinition<A> attr)
    {
        AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
        handler.clearResourceReferences(resource.get(attr));
    }

    private Map<ResourceHandler<?>, Set<ResourceClass<?>>> partitionByHandler(
        ResourceClass<?> rClass)
    {
        Map<ResourceHandler<?>, Set<ResourceClass<?>>> map = new HashMap<ResourceHandler<?>, Set<ResourceClass<?>>>();
        Set<ResourceClass<?>> cs = new HashSet<ResourceClass<?>>();
        cs.add(rClass);
        map.put(rClass.getHandler(), cs);
        for(ResourceClass<?> parent : rClass.getParentClasses())
        {
            cs = map.get(parent.getHandler());
            if(cs == null)
            {
                cs = new HashSet<ResourceClass<?>>();
                map.put(parent.getHandler(), cs);
            }
            cs.add(parent);
        }
        return map;
    }

    /**
     * Instantiate an implementation object.
     * 
     * @param rClass the resource class to be instantiated
     * @return implementation object.
     * @throws BackendException if failed to instantiate.
     */
    protected T instantiate()
        throws BackendException
    {
        try
        {
            return instantiator.newInstance(resourceClass.getJavaClass());
        }
        catch(VirtualMachineError e)
        {
            throw e;
        }
        catch(ThreadDeath e)
        {
            throw e;
        }
        catch(Throwable e)
        {
            throw new BackendException("failed to instantiate " + resourceClass.getName(), e);
        }
    }

    /**
     * Checks if the passed resource is really a {@link GenericResource}.
     * 
     * @param resource the resource to check.
     */
    protected void checkResource(Resource resource)
    {
        if(!(resource instanceof StandardResource))
        {
            throw new ClassCastException("StandardResourceHanler won't operate on "
                + resource.getClass().getName());
        }
    }

    /**
     * Chekcks if the passed delegate object specifies {@link GenericResource} as the javaClass.
     * 
     * @param delegate the delegate to check.
     */
    protected void checkDelegate(Resource delegate)
    {
        if((delegate.getResourceClass().getJavaClass().getModifiers() & Modifier.INTERFACE) != 0)
        {
            throw new ClassCastException(delegate.getResourceClass().getName() + " specifies "
                + delegate.getResourceClass().getJavaClass() + " as implementation class");
        }
        if(!StandardResource.class.isAssignableFrom(delegate.getResourceClass().getJavaClass()))
        {
            throw new ClassCastException("StandardResourceHandler won't operate on "
                + delegate.getResourceClass().getName());
        }
    }

    private void checkRequiredAttributes(ResourceClass<?> rClass,
        Map<AttributeDefinition<?>, ?> attributes)
        throws ValueRequiredException
    {
        for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                Object value = attributes.get(attr);
                if(value == null)
                {
                    if((attr.getFlags() & AttributeFlags.REQUIRED) != 0)
                    {
                        throw new ValueRequiredException("value for REQUIRED attribute "
                            + attr.getName() + " is missing");
                    }
                }
            }
        }
    }

    private void checkConstraints(Map<AttributeDefinition<?>, ?> attributes)
    {
        for(Map.Entry<AttributeDefinition<?>, ?> entry : attributes.entrySet())
        {
            checkConstraint(entry.getKey(), entry.getValue());
        }
    }

    private <A> void checkConstraint(AttributeDefinition<A> attr, Object rawValue)
    {
        final String domain = attr.getDomain();
        if(domain != null && rawValue != null)
        {
            AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
            A value = handler.toAttributeValue(rawValue);
            handler.checkDomain(domain, value);
        }
    }

    /**
     * Adds the loaded/created resource to the internal cache.
     * 
     * @param res the resource to add to the cache.
     */
    private void addToCache(ResourceClass<?> rClass, ResourceAttributesSupport res)
    {
        addToCache(rClass, res);
        for(ResourceClass<?> parent : rClass.getParentClasses())
        {
            addToCache(parent, res);
        }
    }

    /**
     * Adds the loaded/created resource to the internal cache.
     * 
     * @param rc a ResourceClass to use for cache key.
     * @param res a Resource.
     */
    private void addToCache(ResourceClass<?> rc, StandardResource res)
    {
        WeakHashMap<ResourceAttributesSupport, Object> rset;
        synchronized(cache)
        {
            rset = cache.get(rc);
            if(rset == null)
            {
                rset = new WeakHashMap<ResourceAttributesSupport, Object>();
                cacheFactory.registerForPeriodicExpunge(rset);
                cache.put(rc, rset);
            }
        }
        synchronized(rset)
        {
            rset.put(res, null);
        }
    }

    private void dropFromCache(Resource res)
    {
        dropFromCache(res.getResourceClass(), res);
        for(ResourceClass<?> parent : res.getResourceClass().getParentClasses())
        {
            dropFromCache(parent, res);
        }
    }

    private void dropFromCache(ResourceClass<?> rc, Resource res)
    {
        WeakHashMap<ResourceAttributesSupport, Object> rset;
        synchronized(cache)
        {
            rset = cache.get(rc);
        }
        if(rset != null)
        {
            synchronized(rset)
            {
                rset.remove(res);
            }
        }
    }

    /**
     * Reverts all cached resources of the specific class (including subclasses).
     * 
     * @param rc the resource class to revert.
     * @param conn the JDBC connection to use.
     * @throws SQLException if the database operation fails.
     */
    public synchronized void revert(ResourceClass<?> rc, Connection conn)
        throws SQLException
    {
        revert0(rc, conn);
        for(ResourceClass<?> child : rc.getDirectChildClasses())
        {
            child.getHandler().revert(child, conn);
        }
    }

    /**
     * Revert the object of this particular class only (not subclasses).
     * 
     * @param rClass the resource class.
     * @param conn connection to read data from.
     * @throws SQLException if data reading fails.
     */
    private void revert0(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        WeakHashMap<ResourceAttributesSupport, Object> rset;
        synchronized(cache)
        {
            rset = cache.get(rClass);
        }
        if(rset != null)
        {
            synchronized(rset)
            {
                Object data = getData(rClass, conn);
                Set<ResourceAttributesSupport> cached = new HashSet<ResourceAttributesSupport>(
                    rset.keySet());
                Set<ResourceClass<?>> classes = new HashSet<ResourceClass<?>>();
                classes.add(rClass);
                for(ResourceClass<?> parent : rClass.getParentClasses())
                {
                    classes.add(parent);
                }
                for(ResourceAttributesSupport instance : cached)
                {
                    instance.resetAttributes();
                    absHandler(rClass)
                        .revert(instance.getDelegate(), instance, classes, data, conn);
                }
            }
        }
    }

    private static <R extends Resource> StandardResourceHandler<R> absHandler(
        ResourceClass<R> rClass)
    {
        return (StandardResourceHandler<R>)rClass.getHandler();
    }

    /**
     * @return Returns the database.
     */
    Database getDatabase()
    {
        return database;
    }

    /**
     * @return Returns the logger.
     */
    Logger getLogger()
    {
        return logger;
    }
}
