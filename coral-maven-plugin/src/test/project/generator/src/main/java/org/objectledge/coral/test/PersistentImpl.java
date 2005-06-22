// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
// 
// * Redistributions of source code must retain the above copyright notice,  
//       this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//       this list of conditions and the following disclaimer in the documentation  
//       and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//       nor the names of its contributors may be used to endorse or promote products  
//       derived from this software without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
// OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
// POSSIBILITY OF SUCH DAMAGE. 
// 
 
package org.objectledge.coral.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.datatypes.PersistentResource;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.InvalidResourceNameException;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.parameters.Parameters;

/**
 * An implementation of <code>coral.test.Persistent</code> Coral resource class.
 *
 * @author Coral Maven plugin
 */
public class PersistentImpl
    extends PersistentResource
    implements Persistent
{
    // class variables /////////////////////////////////////////////////////////

    /** Class variables initialization status. */
    private static boolean definitionsInitialized;
	
    /** The AttributeDefinition object for the <code>d1</code> attribute. */
    private static AttributeDefinition d1Def;

    /** The AttributeDefinition object for the <code>p1</code> attribute. */
    private static AttributeDefinition p1Def;

    /** The AttributeDefinition object for the <code>res1</code> attribute. */
    private static AttributeDefinition res1Def;

    // initialization /////////////////////////////////////////////////////////

    /**
     * Creates a blank <code>coral.test.Persistent</code> resource wrapper.
     *
     * <p>This constructor should be used by the handler class only. Use 
     * <code>load()</code> and <code>create()</code> methods to create
     * instances of the wrapper in your application code.</p>
     *
     */
    public PersistentImpl()
    {
    }

    // static methods ////////////////////////////////////////////////////////

    /**
     * Retrieves a <code>coral.test.Persistent</code> resource instance from the store.
     *
     * <p>This is a simple wrapper of StoreService.getResource() method plus
     * the typecast.</p>
     *
     * @param session the CoralSession
     * @param id the id of the object to be retrieved
     * @return a resource instance.
     * @throws EntityDoesNotExistException if the resource with the given id does not exist.
     */
    public static Persistent getPersistent(CoralSession session, long id)
        throws EntityDoesNotExistException
    {
        Resource res = session.getStore().getResource(id);
        if(!(res instanceof Persistent))
        {
            throw new IllegalArgumentException("resource #"+id+" is "+
                                               res.getResourceClass().getName()+
                                               " not coral.test.Persistent");
        }
        return (Persistent)res;
    }

    /**
     * Creates a new <code>coral.test.Persistent</code> resource instance.
     *
     * @param session the CoralSession
     * @param name the name of the new resource
     * @param parent the parent resource.
     * @return a new Persistent instance.
     * @throws InvalidResourceNameException if the name argument contains illegal characters.
     */
    public static Persistent createPersistent(CoralSession session, String name, Resource
        parent)
        throws InvalidResourceNameException
    {
        try
        {
            ResourceClass rc = session.getSchema().getResourceClass("coral.test.Persistent");
            Map attrs = new HashMap();
            Resource res = session.getStore().createResource(name, parent, rc, attrs);
            if(!(res instanceof Persistent))
            {
                throw new BackendException("incosistent schema: created object is "+
                                           res.getClass().getName());
            }
            return (Persistent)res;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("incompatible schema change", e);
        }
        catch(ValueRequiredException e)
        {
            throw new BackendException("incompatible schema change", e);
        }
    }

    // public interface //////////////////////////////////////////////////////
 
    /**
     * Returns the value of the <code>d1</code> attribute.
     *
     * @return the value of the <code>d1</code> attribute.
     */
    public Date getD1()
    {
        return (Date)get(d1Def);
    }
    
    /**
     * Returns the value of the <code>d1</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>d1</code> attribute.
     */
    public Date getD1(Date defaultValue)
    {
        if(isDefined(d1Def))
        {
            return (Date)get(d1Def);
        }
        else
        {
            return defaultValue;
        }
    }    

    /**
     * Sets the value of the <code>d1</code> attribute.
     *
     * @param value the value of the <code>d1</code> attribute,
     *        or <code>null</code> to remove value.
     */
    public void setD1(Date value)
    {
        try
        {
            if(value != null)
            {
                set(d1Def, value);
            }
            else
            {
                unset(d1Def);
            }
        }
        catch(ModificationNotPermitedException e)
        {
            throw new BackendException("incompatible schema change",e);
        }
        catch(ValueRequiredException e)
        {
            throw new BackendException("incompatible schema change",e);
        }
    }
   
	/**
	 * Checks if the value of the <code>d1</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>d1</code> attribute is defined.
	 */
    public boolean isD1Defined()
	{
	    return isDefined(d1Def);
	}
 
    /**
     * Returns the value of the <code>p1</code> attribute.
     *
     * @return the value of the <code>p1</code> attribute.
     */
    public Parameters getP1()
    {
        return (Parameters)get(p1Def);
    }
    
    /**
     * Returns the value of the <code>p1</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>p1</code> attribute.
     */
    public Parameters getP1(Parameters defaultValue)
    {
        if(isDefined(p1Def))
        {
            return (Parameters)get(p1Def);
        }
        else
        {
            return defaultValue;
        }
    }    

    /**
     * Sets the value of the <code>p1</code> attribute.
     *
     * @param value the value of the <code>p1</code> attribute,
     *        or <code>null</code> to remove value.
     */
    public void setP1(Parameters value)
    {
        try
        {
            if(value != null)
            {
                set(p1Def, value);
            }
            else
            {
                unset(p1Def);
            }
        }
        catch(ModificationNotPermitedException e)
        {
            throw new BackendException("incompatible schema change",e);
        }
        catch(ValueRequiredException e)
        {
            throw new BackendException("incompatible schema change",e);
        }
    }
   
	/**
	 * Checks if the value of the <code>p1</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>p1</code> attribute is defined.
	 */
    public boolean isP1Defined()
	{
	    return isDefined(p1Def);
	}
 
    /**
     * Returns the value of the <code>res1</code> attribute.
     *
     * @return the value of the <code>res1</code> attribute.
     */
    public Resource getRes1()
    {
        return (Resource)get(res1Def);
    }
    
    /**
     * Returns the value of the <code>res1</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>res1</code> attribute.
     */
    public Resource getRes1(Resource defaultValue)
    {
        if(isDefined(res1Def))
        {
            return (Resource)get(res1Def);
        }
        else
        {
            return defaultValue;
        }
    }    

    /**
     * Sets the value of the <code>res1</code> attribute.
     *
     * @param value the value of the <code>res1</code> attribute,
     *        or <code>null</code> to remove value.
     */
    public void setRes1(Resource value)
    {
        try
        {
            if(value != null)
            {
                set(res1Def, value);
            }
            else
            {
                unset(res1Def);
            }
        }
        catch(ModificationNotPermitedException e)
        {
            throw new BackendException("incompatible schema change",e);
        }
        catch(ValueRequiredException e)
        {
            throw new BackendException("incompatible schema change",e);
        }
    }
   
	/**
	 * Checks if the value of the <code>res1</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>res1</code> attribute is defined.
	 */
    public boolean isRes1Defined()
	{
	    return isDefined(res1Def);
	}
  
    // @custom methods ///////////////////////////////////////////////////////
}
