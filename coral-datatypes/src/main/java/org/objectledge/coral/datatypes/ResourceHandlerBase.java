package org.objectledge.coral.datatypes;

import java.util.ArrayList;
import java.util.Arrays;

import org.objectledge.ComponentInitializationError;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * The base class for resource handlers.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceHandlerBase.java,v 1.1 2004-03-02 09:51:01 pablo Exp $
 */
public abstract class ResourceHandlerBase 
    implements ResourceHandler
{
    /** The root subject. */
    Subject rootSubject;

    /** The resource class this handler is responsible for. */
    protected ResourceClass resourceClass;

    /** The security */
    protected CoralSecurity coralSecurity;
    
    /** The schema */
    protected CoralSchema coralSchema;
    
    /**
     * The base constructor.
     * 
     * @param coralSecurity
     * @param resourceClass
     */
    public ResourceHandlerBase(CoralSchema coralSchema, CoralSecurity coralSecurity,
                                ResourceClass resourceClass)
    {
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
        try
        {
            rootSubject = coralSecurity.getSubject(Subject.ROOT);
        }
        catch (EntityDoesNotExistException e)
        {
            throw new ComponentInitializationError("cannot lookup root subject", e);
        }
        this.resourceClass = resourceClass;
    }

    /* 
     * (overriden)
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
    
    /* 
     * (overriden)
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
        resource.update(rootSubject);
    }
}
