package org.objectledge.coral.datatypes;

import java.util.ArrayList;
import java.util.Arrays;

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

/**
 * The base class for resource handlers.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceHandlerBase.java,v 1.5 2004-05-06 10:55:25 pablo Exp $
 */
public abstract class ResourceHandlerBase 
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
    
    /**
     * The base constructor.
     * 
     * @param coralSchema the coral schema.
     * @param coralSecurity the coral security.
     * @param instantiator the instantiator.
     * @param resourceClass the resource class.
     */
    public ResourceHandlerBase(CoralSchema coralSchema, CoralSecurity coralSecurity,
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
    protected GenericResource instantiate(ResourceClass rClass)
        throws BackendException
    {
        GenericResource res;
        try
        {
            res = (GenericResource)instantiator.newInstance(rClass.getJavaClass());
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
}
