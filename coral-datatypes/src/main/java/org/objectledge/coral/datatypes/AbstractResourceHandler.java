package org.objectledge.coral.datatypes;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;

/**
 * The base class for resource handlers.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AbstractResourceHandler.java,v 1.9 2005-06-20 08:20:22 rafal Exp $
 */
public abstract class AbstractResourceHandler 
    implements ResourceHandler
{
    /** The resource class this handler is responsible for. */
    protected ResourceClass resourceClass;

    /** The security. */
    private CoralSecurity coralSecurity;
    
    /** The schema. */
    protected CoralSchema coralSchema;
    
    /** The instnatiator. */
    protected Instantiator instantiator;
    
    /** resource sets, keyed by resource class. Resources are kept through  weak  references. */
    private Map<ResourceClass,Map> cache = new HashMap<ResourceClass,Map>();
    
    /** to avoid multiple cache processing. */
    private Map<Resource,Object> cached = new WeakHashMap<Resource,Object>();
    
    /** the database. */
    private Database database;
    
    /** the logger. */
    private Logger logger;

    /**
     * The base constructor.
     * 
     * @param coralSchema the coral schema.
     * @param coralSecurity the coral security.
     * @param instantiator the instantiator.
     * @param resourceClass the resource class.
     */
    public AbstractResourceHandler(CoralSchema coralSchema, CoralSecurity coralSecurity,
        Instantiator instantiator, ResourceClass resourceClass, Database database, Logger logger)
    {
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
        this.instantiator = instantiator;
        this.resourceClass = resourceClass;
        this.database = database;
        this.logger = logger;
    }
    
    /**
     * {@inheritDoc}
     */
    public Resource create(Resource delegate, Map attributes, Connection conn)
        throws ValueRequiredException, SQLException
    {
        checkDelegate(delegate);
        AbstractResource res = instantiate(resourceClass);
        res.create(delegate, resourceClass, attributes, conn);
        addToCache(res);
        return res;
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(Resource resource, Connection conn)
        throws SQLException
    {
        checkResource(resource);
        ((AbstractResource)resource).delete(conn);
    }

    /**
     * {@inheritDoc}
     */
    public Resource retrieve(Resource delegate, Connection conn, Object data)
        throws SQLException
    {
        checkDelegate(delegate);
        AbstractResource res = instantiate(resourceClass);
        if(data == null)
        {
            data = getData(delegate, conn);
        }
        ((AbstractResource)res).retrieve(delegate, resourceClass, conn, data);
        addToCache(res);
        return res;
    }
    
    /**
     * {@inheritDoc}
     */
    public void revert(Resource resource, Connection conn, Object data)
        throws SQLException
    {
        Resource delegate = resource.getDelegate();
        checkDelegate(delegate);
        if(data == null)
        {
            data = getData(delegate, conn);
        }
        ((AbstractResource)resource).revert(resourceClass, conn, data);        
    }

    /**
     * {@inheritDoc}
     */
    public void update(Resource resource, Connection conn)
        throws SQLException
    {
        checkResource(resource);
        ((AbstractResource)resource).update(conn);
    }

    
    /**
     * {@inheritDoc}
     */
    public Resource[] getResourceReferences(Resource resource, boolean clearable)
    {
        ArrayList temp = new ArrayList();
        for(AttributeDefinition attr : resourceClass.getAllAttributes())
        {
            AttributeHandler handler = attr.getAttributeClass().getHandler();
            if(handler.containsResourceReferences() &&
               resource.isDefined(attr) && 
               (clearable || (!handler.isComposite() && 
                   (attr.getFlags() & (AttributeFlags.READONLY | AttributeFlags.REQUIRED)) != 0)) &&
               ((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0))
            {
                Resource[] refs = attr.getAttributeClass().
                    getHandler().getResourceReferences(resource.get(attr));
                temp.addAll(Arrays.asList(refs));
            }
        }
        Resource[] result = new Resource[temp.size()];
        temp.toArray(result);
        return result;
    }   
    
    /**
     * {@inheritDoc}
     */
    public void clearResourceReferences(Resource resource)
    {
        for(AttributeDefinition attr : resourceClass.getAllAttributes())
        {
            AttributeHandler handler = attr.getAttributeClass().getHandler();
            if(handler.containsResourceReferences() &&
               resource.isDefined(attr) && 
               (handler.isComposite() || 
                    (attr.getFlags() & (AttributeFlags.READONLY | AttributeFlags.REQUIRED | 
                         AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0))
            {
                if(handler.isComposite())
                {
                    handler.clearResourceReferences(resource.get(attr));
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

    /**
     * Instantiate an implementation object.
     *
     * @param rClass the resource class to be instantiated
     * @return implementation object.
     * @throws BackendException if failed to instantiate. 
     */
    protected AbstractResource instantiate(ResourceClass rClass)
        throws BackendException
    {
        AbstractResource res;
        try
        {
            res = (AbstractResource)instantiator.newInstance(rClass.getJavaClass());
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
            throw new BackendException("failed to instantiate "+
                                        rClass.getName(), e);
        }
        return res;
    }
    
    /**
     * Checks if the passed resource is really a {@link GenericResource}.
     *
     * @param resource the resource to check.
     */
    protected void checkResource(Resource resource)
    {
        if(!(resource instanceof AbstractResource))
        {
            throw new ClassCastException("AbstractResourceHanler won't operate on "+
                                         resource.getClass().getName());
        }
    }

    /**
     * Chekcks if the passed delegate object specifies {@link GenericResource}
     * as the javaClass.
     *
     * @param delegate the delegate to check.
     */
    protected void checkDelegate(Resource delegate)
    {
        if((delegate.getResourceClass().getJavaClass().getModifiers() & Modifier.INTERFACE) != 0)
        {
            throw new ClassCastException(delegate.getResourceClass().getName()+" specifies "+
                delegate.getResourceClass().getJavaClass()+" as implementation class");
        }
        if(!AbstractResource.class.isAssignableFrom(delegate.getResourceClass().getJavaClass()))
        {
            throw new ClassCastException("AbstractResourceHandler won't operate on "+
                                         delegate.getResourceClass().getName());
        }
    }

    /**
     * Adds the loaded/created resource to the internal cache.
     *
     * @param res the resource to add to the cache.
     */
    private void addToCache(AbstractResource res)
    {
        if(!cached.containsKey(res))
        {
            addToCache0(res.getResourceClass(), res);
            for(ResourceClass parent : res.getResourceClass().getParentClasses())
            {
                addToCache0(parent, res);
            }
            cached.put(res, null);
        }
    }
    
    /**
     * Adds the loaded/created resource to the internal cache.
     * 
     * @param rc a ResourceClass to use for cache key.
     * @param res a Resource.
     */
    private void addToCache0(ResourceClass rc, AbstractResource res)
    {
        Map rset = cache.get(rc);
        if(rset == null)
        {
            rset = new WeakHashMap();
            cache.put(rc, rset);
        }
        rset.put(res, null);
    }

    /**
     * Reverts all cached resources of the specific class.
     *
     * @param rc the resource class to revert.
     * @param conn the JDBC connection to use.
     * @throws SQLException if the database operation fails.
     */
    protected synchronized void revert(ResourceClass rc, Connection conn)
        throws SQLException
    {
        revert0(rc, conn);
        for(ResourceClass child : rc.getChildClasses())
        {
            revert0(child, conn);
        }
    }
    
    private void revert0(ResourceClass rc, Connection conn)
        throws SQLException
    {
        Map rset = cache.get(rc);
        if(rset != null)
        {
            // we'll get too much but this shouldn't be a problem.
            Object data = getData(rc, conn);
            Set<AbstractResource> orig = new HashSet<AbstractResource>(rset.keySet());
            for(AbstractResource r : orig)
            {
                r.revert(rc, conn, data);
            }
        }
    }
    
    /**
     * Retrieve attribute information for a specific resource.
     * 
     * @param delegate the security delegate object.
     * @param conn database connection.
     * @return opaque data object.
     * @throws SQLException if information retrieval fails.
     */
    protected abstract Object getData(Resource delegate, Connection conn) throws SQLException;

    /**
     * Retrieve attribute information for resources of a specific class.
     * 
     * @param rc the resource class.
     * @param conn database connection.
     * @return opaque data object.
     * @throws SQLException if information retrieval fails.
     */
    protected abstract Object getData(ResourceClass rc, Connection conn) throws SQLException;
    
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
