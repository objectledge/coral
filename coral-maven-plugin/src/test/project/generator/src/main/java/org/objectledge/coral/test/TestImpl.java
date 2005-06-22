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
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.InvalidResourceNameException;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * An implementation of <code>coral.test.Test</code> Coral resource class.
 *
 * @author Coral Maven plugin
 */
public class TestImpl
    extends NodeImpl
    implements Test
{
    // class variables /////////////////////////////////////////////////////////

    /** Class variables initialization status. */
    private static boolean definitionsInitialized;
	
    /** The AttributeDefinition object for the <code>i1</code> attribute. */
    private static AttributeDefinition i1Def;

    /** The AttributeDefinition object for the <code>i2</code> attribute. */
    private static AttributeDefinition i2Def;

    /** The AttributeDefinition object for the <code>i3</code> attribute. */
    private static AttributeDefinition i3Def;

    /** The AttributeDefinition object for the <code>s1</code> attribute. */
    private static AttributeDefinition s1Def;

    /** The AttributeDefinition object for the <code>s2</code> attribute. */
    private static AttributeDefinition s2Def;

    /** The AttributeDefinition object for the <code>s3</code> attribute. */
    private static AttributeDefinition s3Def;

	// custom injected fields /////////////////////////////////////////////////
	
    /** The CoralStore. */
    protected CoralStore coralStore;

    // initialization /////////////////////////////////////////////////////////

    /**
     * Creates a blank <code>coral.test.Test</code> resource wrapper.
     *
     * <p>This constructor should be used by the handler class only. Use 
     * <code>load()</code> and <code>create()</code> methods to create
     * instances of the wrapper in your application code.</p>
     *
     * @param coralStore the CoralStore.
     */
    public TestImpl(CoralStore coralStore)
    {
        this.coralStore = coralStore;
    }

    // static methods ////////////////////////////////////////////////////////

    /**
     * Retrieves a <code>coral.test.Test</code> resource instance from the store.
     *
     * <p>This is a simple wrapper of StoreService.getResource() method plus
     * the typecast.</p>
     *
     * @param session the CoralSession
     * @param id the id of the object to be retrieved
     * @return a resource instance.
     * @throws EntityDoesNotExistException if the resource with the given id does not exist.
     */
    public static Test getTest(CoralSession session, long id)
        throws EntityDoesNotExistException
    {
        Resource res = session.getStore().getResource(id);
        if(!(res instanceof Test))
        {
            throw new IllegalArgumentException("resource #"+id+" is "+
                                               res.getResourceClass().getName()+
                                               " not coral.test.Test");
        }
        return (Test)res;
    }

    /**
     * Creates a new <code>coral.test.Test</code> resource instance.
     *
     * @param session the CoralSession
     * @param name the name of the new resource
     * @param parent the parent resource.
     * @param i2 the i2 attribute
     * @param s2 the s2 attribute
     * @return a new Test instance.
     * @throws ValueRequiredException if one of the required attribues is undefined.
     * @throws InvalidResourceNameException if the name argument contains illegal characters.
     */
    public static Test createTest(CoralSession session, String name, Resource parent, int i2,
        String s2)
        throws ValueRequiredException, InvalidResourceNameException
    {
        try
        {
            ResourceClass rc = session.getSchema().getResourceClass("coral.test.Test");
            Map attrs = new HashMap();
            attrs.put(rc.getAttribute("i2"), new Integer(i2));
            attrs.put(rc.getAttribute("s2"), s2);
            Resource res = session.getStore().createResource(name, parent, rc, attrs);
            if(!(res instanceof Test))
            {
                throw new BackendException("incosistent schema: created object is "+
                                           res.getClass().getName());
            }
            return (Test)res;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("incompatible schema change", e);
        }
    }

    // public interface //////////////////////////////////////////////////////

    /**
     * Returns the value of the <code>i1</code> attribute.
     *
     * @return the value of the <code>i1</code> attribute.
     * @throws IllegalStateException if the value of the attribute is 
     *         undefined.
     */
    public int getI1()
        throws IllegalStateException
    {
        if(isDefined(i1Def))
        {
            return ((Integer)get(i1Def)).intValue();
        }
        else
        {
            throw new IllegalStateException("value of attribute i1 is undefined"+
			    " for resource #"+getId());
        }
    }

    /**
     * Returns the value of the <code>i1</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>i1</code> attribute.
     */
    public int getI1(int defaultValue)
    {
        if(isDefined(i1Def))
        {
            return ((Integer)get(i1Def)).intValue();
        }
        else
        {
            return defaultValue;
        }
    }

    /**
     * Sets the value of the <code>i1</code> attribute.
     *
     * @param value the value of the <code>i1</code> attribute.
     */
    public void setI1(int value)
    {
        try
        {
            set(i1Def, new Integer(value));
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
     * Removes the value of the <code>i1</code> attribute.
     */
    public void unsetI1()
    {
        try
        {
            unset(i1Def);
        }
        catch(ValueRequiredException e)
        {
            throw new BackendException("incompatible schema change",e);
        }     
    } 
   
	/**
	 * Checks if the value of the <code>i1</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>i1</code> attribute is defined.
	 */
    public boolean isI1Defined()
	{
	    return isDefined(i1Def);
	}
 
    /**
     * Returns the value of the <code>i2</code> attribute.
     *
     * @return the value of the <code>i2</code> attribute.
     */
    public int getI2()
    {
        if(isDefined(i2Def))
        {
            return ((Integer)get(i2Def)).intValue();
        }
        else
        {
            throw new BackendException("incompatible schema change");
        }
    }    

    /**
     * Sets the value of the <code>i2</code> attribute.
     *
     * @param value the value of the <code>i2</code> attribute.
     */
    public void setI2(int value)
    {
        try
        {
            set(i2Def, new Integer(value));
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
     * Returns the value of the <code>i3</code> attribute.
     *
     * @return the value of the <code>i3</code> attribute.
     * @throws IllegalStateException if the value of the attribute is 
     *         undefined.
     */
    public int getI3()
        throws IllegalStateException
    {
        if(isDefined(i3Def))
        {
            return ((Integer)get(i3Def)).intValue();
        }
        else
        {
            throw new IllegalStateException("value of attribute i3 is undefined"+
			    " for resource #"+getId());
        }
    }

    /**
     * Returns the value of the <code>i3</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>i3</code> attribute.
     */
    public int getI3(int defaultValue)
    {
        if(isDefined(i3Def))
        {
            return ((Integer)get(i3Def)).intValue();
        }
        else
        {
            return defaultValue;
        }
    }
  
	/**
	 * Checks if the value of the <code>i3</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>i3</code> attribute is defined.
	 */
    public boolean isI3Defined()
	{
	    return isDefined(i3Def);
	}
 
    /**
     * Returns the value of the <code>s1</code> attribute.
     *
     * @return the value of the <code>s1</code> attribute.
     */
    public String getS1()
    {
        return (String)get(s1Def);
    }
    
    /**
     * Returns the value of the <code>s1</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>s1</code> attribute.
     */
    public String getS1(String defaultValue)
    {
        if(isDefined(s1Def))
        {
            return (String)get(s1Def);
        }
        else
        {
            return defaultValue;
        }
    }    

    /**
     * Sets the value of the <code>s1</code> attribute.
     *
     * @param value the value of the <code>s1</code> attribute,
     *        or <code>null</code> to remove value.
     */
    public void setS1(String value)
    {
        try
        {
            if(value != null)
            {
                set(s1Def, value);
            }
            else
            {
                unset(s1Def);
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
	 * Checks if the value of the <code>s1</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>s1</code> attribute is defined.
	 */
    public boolean isS1Defined()
	{
	    return isDefined(s1Def);
	}
 
    /**
     * Returns the value of the <code>s2</code> attribute.
     *
     * @return the value of the <code>s2</code> attribute.
     */
    public String getS2()
    {
        return (String)get(s2Def);
    }
 
    /**
     * Sets the value of the <code>s2</code> attribute.
     *
     * @param value the value of the <code>s2</code> attribute.
     * @throws ValueRequiredException if you attempt to set a <code>null</code> 
     *         value.
     */
    public void setS2(String value)
        throws ValueRequiredException
    {
        try
        {
            if(value != null)
            {
                set(s2Def, value);
            }
            else
            {
                throw new ValueRequiredException("attribute s2 "+
                                                 "is declared as REQUIRED");
            }
        }
        catch(ModificationNotPermitedException e)
        {
            throw new BackendException("incompatible schema change",e);
        }
    }
    
    /**
     * Returns the value of the <code>s3</code> attribute.
     *
     * @return the value of the <code>s3</code> attribute.
     */
    public String getS3()
    {
        return (String)get(s3Def);
    }
    
    /**
     * Returns the value of the <code>s3</code> attribute.
     *
     * @param defaultValue the value to return if the attribute is undefined.
     * @return the value of the <code>s3</code> attribute.
     */
    public String getS3(String defaultValue)
    {
        if(isDefined(s3Def))
        {
            return (String)get(s3Def);
        }
        else
        {
            return defaultValue;
        }
    }    
  
	/**
	 * Checks if the value of the <code>s3</code> attribute is defined.
	 *
	 * @return <code>true</code> if the value of the <code>s3</code> attribute is defined.
	 */
    public boolean isS3Defined()
	{
	    return isDefined(s3Def);
	}
  
    // @custom methods ///////////////////////////////////////////////////////
    
    // @import org.objectledge.coral.store.CoralStore
    // @field CoralStore coralStore
}
