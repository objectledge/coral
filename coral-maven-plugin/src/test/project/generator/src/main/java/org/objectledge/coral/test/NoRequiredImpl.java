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

import java.util.HashMap;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.datatypes.NodeImpl;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.InvalidResourceNameException;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * An implementation of <code>coral.test.NoRequired</code> Coral resource class.
 *
 * @author Coral Maven plugin
 */
public class NoRequiredImpl
    extends NodeImpl
    implements NoRequired
{
    // class variables /////////////////////////////////////////////////////////

    /** Class variables initialization status. */
    private static boolean definitionsInitialized;
	
    /** The AttributeDefinition object for the <code>i4</code> attribute. */
    private static AttributeDefinition i4Def;

    /** The AttributeDefinition object for the <code>s4</code> attribute. */
    private static AttributeDefinition s4Def;

    // initialization /////////////////////////////////////////////////////////

    /**
     * Creates a blank <code>coral.test.NoRequired</code> resource wrapper.
     *
     * <p>This constructor should be used by the handler class only. Use 
     * <code>load()</code> and <code>create()</code> methods to create
     * instances of the wrapper in your application code.</p>
     *
     */
    public NoRequiredImpl()
    {
    }

    // static methods ////////////////////////////////////////////////////////

    /**
     * Retrieves a <code>coral.test.NoRequired</code> resource instance from the store.
     *
     * <p>This is a simple wrapper of StoreService.getResource() method plus
     * the typecast.</p>
     *
     * @param session the CoralSession
     * @param id the id of the object to be retrieved
     * @return a resource instance.
     * @throws EntityDoesNotExistException if the resource with the given id does not exist.
     */
    public static NoRequired getNoRequired(CoralSession session, long id)
        throws EntityDoesNotExistException
    {
        Resource res = session.getStore().getResource(id);
        if(!(res instanceof NoRequired))
        {
            throw new IllegalArgumentException("resource #"+id+" is "+
                                               res.getResourceClass().getName()+
                                               " not coral.test.NoRequired");
        }
        return (NoRequired)res;
    }

    /**
     * Creates a new <code>coral.test.NoRequired</code> resource instance.
     *
     * @param session the CoralSession
     * @param name the name of the new resource
     * @param parent the parent resource.
     * @return a new NoRequired instance.
     * @throws InvalidResourceNameException if the name argument contains illegal characters.
     */
    public static NoRequired createNoRequired(CoralSession session, String name, Resource
        parent)
        throws InvalidResourceNameException
    {
        try
        {
            ResourceClass rc = session.getSchema().getResourceClass("coral.test.NoRequired");
            Map attrs = new HashMap();
            Resource res = session.getStore().createResource(name, parent, rc, attrs);
            if(!(res instanceof NoRequired))
            {
                throw new BackendException("incosistent schema: created object is "+
                                           res.getClass().getName());
            }
            return (NoRequired)res;
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
     * Returns the value of the <code>i4</code> attribute.
     *
     * @return the value of the <code>i4</code> attribute.
     * @throws IllegalStateException if the value of the attribute is 
     *         undefined.
     */
    public int getI4()
        throws IllegalStateException
    {
	    Integer value = (Integer)getInternal(i4Def, null);
        if(value != null)
        {
            return value.intValue();
        }
        else
        {
            throw new IllegalStateException("value of attribute i4 is undefined"+
			    " for resource #"+getId());
        }
    }

    /**
     * Returns the value of the <code>i4</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>i4</code> attribute.
     */
    public int getI4(int defaultValue)
    {
		return ((Integer)getInternal(i4Def, new Integer(defaultValue))).intValue();
	}

    /**
     * Sets the value of the <code>i4</code> attribute.
     *
     * @param value the value of the <code>i4</code> attribute.
     */
    public void setI4(int value)
    {
        try
        {
            set(i4Def, new Integer(value));
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
     * Removes the value of the <code>i4</code> attribute.
     */
    public void unsetI4()
    {
        try
        {
            unset(i4Def);
        }
        catch(ValueRequiredException e)
        {
            throw new BackendException("incompatible schema change",e);
        }     
    } 
   
	/**
	 * Checks if the value of the <code>i4</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>i4</code> attribute is defined.
	 */
    public boolean isI4Defined()
	{
	    return isDefined(i4Def);
	}
 
    /**
     * Returns the value of the <code>s4</code> attribute.
     *
     * @return the value of the <code>s4</code> attribute.
     */
    public String getS4()
    {
        return (String)getInternal(s4Def, null);
    }
    
    /**
     * Returns the value of the <code>s4</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>s4</code> attribute.
     */
    public String getS4(String defaultValue)
    {
        return (String)getInternal(s4Def, defaultValue);
    }    

    /**
     * Sets the value of the <code>s4</code> attribute.
     *
     * @param value the value of the <code>s4</code> attribute,
     *        or <code>null</code> to remove value.
     */
    public void setS4(String value)
    {
        try
        {
            if(value != null)
            {
                set(s4Def, value);
            }
            else
            {
                unset(s4Def);
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
	 * Checks if the value of the <code>s4</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>s4</code> attribute is defined.
	 */
    public boolean isS4Defined()
	{
	    return isDefined(s4Def);
	}
  
    // @custom methods ///////////////////////////////////////////////////////
}
