package org.objectledge.coral.datatypes;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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

/**
 * The base class for resource handlers.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AbstractResourceHandler.java,v 1.3 2004-07-01 11:27:22 fil Exp $
 */
public abstract class AbstractResourceHandler 
    implements ResourceHandler
{
    /** The resource class this handler is responsible for. */
    protected ResourceClass resourceClass;

    /** The security */
    protected CoralSecurity coralSecurity;
    
    /** The schema */
    protected CoralSchema coralSchema;
    
    /** The instnatiator */
    protected Instantiator instantiator;
    
    /** resource sets, keyed by resource class. Resources are kept through  weak  references. */
    private Map cache = new HashMap();
    
    /**
     * The base constructor.
     * 
     * @param coralSchema the coral schema.
     * @param coralSecurity the coral security.
     * @param instantiator the instantiator.
     * @param resourceClass the resource class.
     */
    public AbstractResourceHandler(CoralSchema coralSchema, CoralSecurity coralSecurity,
        Instantiator instantiator, ResourceClass resourceClass)
    {
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
        this.instantiator = instantiator;
        this.resourceClass = resourceClass;
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
        AttributeDefinition[] attrs = resourceClass.getAllAttributes();
        for(int i=0; i<attrs.length; i++)
        {
            AttributeDefinition attr = attrs[i];
            AttributeHandler handler = attr.getAttributeClass().getHandler();
            if(handler.containsResourceReferences() &&
               resource.isDefined(attrs[i]) && 
               (clearable || (!handler.isComposite() && 
                   (attr.getFlags() & (AttributeFlags.READONLY | AttributeFlags.REQUIRED)) != 0)) &&
               ((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0))
            {
                Resource[] refs = attrs[i].getAttributeClass().
                    getHandler().getResourceReferences(resource.get(attrs[i]));
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
        AttributeDefinition[] attrs = resourceClass.getAllAttributes();
        for(int i=0; i<attrs.length; i++)
        {
            AttributeDefinition attr = attrs[i];
            AttributeHandler handler = attr.getAttributeClass().getHandler();
            if(handler.containsResourceReferences() &&
               resource.isDefined(attrs[i]) && 
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
        if(!GenericResource.class.isAssignableFrom(delegate.getResourceClass().getJavaClass()))
        {
            throw new ClassCastException("GenericResourceHandler won't operate on "+
                                         delegate.getResourceClass().getName());
        }
    }

    /**
     * Adds the loaded/created resource to the internal cache.
     *
     * @param res the resource to add to the cache.
     */
    protected void addToCache(AbstractResource res)
    {
        // we use WeakHashMap to emulate WeakSet
        Map rset = (Map)cache.get(res.getFacetClass());
        if(rset == null)
        {
            rset = new WeakHashMap();
            cache.put(res.getResourceClass(), rset);
        }
        rset.put(res, null);
    }

    /**
     * Reverts all cached resources of the specific class.
     *
     * @param rc the resource class to revert.
     * @param conn the JDBC connection to use.
     */
    protected synchronized void revert(ResourceClass rc, Connection conn)
        throws SQLException
    {
        Map rset = (Map)cache.get(rc);
        if(rset != null)
        {
            // we'll get too much but this shouldn't be a problem.
            Object data = getData(rc, conn);
            Set orig = new HashSet(rset.keySet());
            Iterator i = orig.iterator();
            while(i.hasNext())
            {
                AbstractResource r = (AbstractResource)i.next();
                r.revert(r.getFacetClass(), conn, data);
            }
        }
    }
    
    protected abstract Object getData(Resource delegate, Connection conn) throws SQLException;

    protected abstract Object getData(ResourceClass rc, Connection conn) throws SQLException;
}
