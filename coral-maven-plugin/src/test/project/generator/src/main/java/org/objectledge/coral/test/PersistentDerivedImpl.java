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
import org.objectledge.coral.datatypes.PersistentResource;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;

import org.jcontainer.dna.Logger;

/**
 * An implementation of <code>coral.test.PersistentDerived</code> Coral resource class.
 *
 * @author Coral Maven plugin
 */
public class PersistentDerivedImpl
    extends PersistentResource
    implements PersistentDerived
{
    // instance variables ////////////////////////////////////////////////////

    /** The AttributeDefinition object for the <code>i1</code> attribute. */
    private AttributeDefinition i1Def;

    /** The AttributeDefinition object for the <code>s1</code> attribute. */
    private AttributeDefinition s1Def;

    // initialization /////////////////////////////////////////////////////////

    /**
     * Creates a blank <code>coral.test.PersistentDerived</code> resource wrapper.
     *
     * <p>This constructor should be used by the handler class only. Use 
     * <code>load()</code> and <code>create()</code> methods to create
     * instances of the wrapper in your application code.</p>
     *
     * @param schema the CoralSchema.
     * @param database the Database.
     * @param logger the Logger.
     */
    public PersistentDerivedImpl(CoralSchema schema, Database database, Logger logger)
    {
        super(schema, database, logger);
        try
        {
            ResourceClass rc = schema.getResourceClass("coral.test.PersistentDerived");
            i1Def = rc.getAttribute("i1");
            s1Def = rc.getAttribute("s1");
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("incompatible schema change", e);
        }
    }

    // static methods ////////////////////////////////////////////////////////

    /**
     * Retrieves a <code>coral.test.PersistentDerived</code> resource instance from the store.
     *
     * <p>This is a simple wrapper of StoreService.getResource() method plus
     * the typecast.</p>
     *
     * @param session the CoralSession
     * @param id the id of the object to be retrieved
     * @return a resource instance.
     * @throws EntityDoesNotExistException if the resource with the given id does not exist.
     */
    public static PersistentDerived getPersistentDerived(CoralSession session, long id)
        throws EntityDoesNotExistException
    {
        Resource res = session.getStore().getResource(id);
        if(!(res instanceof PersistentDerived))
        {
            throw new IllegalArgumentException("resource #"+id+" is "+
                                               res.getResourceClass().getName()+
                                               " not coral.test.PersistentDerived");
        }
        return (PersistentDerived)res;
    }

    /**
     * Creates a new <code>coral.test.PersistentDerived</code> resource instance.
     *
     * @param session the CoralSession
     * @param name the name of the new resource
     * @param parent the parent resource.
     * @param i1 the i1 attribute
     * @param s1 the s1 attribute
     * @return a new PersistentDerived instance.
     * @throws ValueRequiredException if one of the required attribues is undefined.
     */
    public static PersistentDerived createPersistentDerived(CoralSession session, String name, Resource parent, 
        int i1, String s1)
        throws ValueRequiredException
    {
        try
        {
            ResourceClass rc = session.getSchema().getResourceClass("coral.test.PersistentDerived");
            Map attrs = new HashMap();
            attrs.put(rc.getAttribute("i1"), new Integer(i1));
            attrs.put(rc.getAttribute("s1"), s1);
            Resource res = session.getStore().createResource(name, parent, rc, attrs);
            if(!(res instanceof PersistentDerived))
            {
                throw new BackendException("incosistent schema: created object is "+
                                           res.getClass().getName());
            }
            return (PersistentDerived)res;
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
    {
        if(isDefined(i1Def))
        {
            return ((Integer)get(i1Def)).intValue();
        }
        else
        {
            throw new BackendException("incompatible schema change");
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
     * Returns the value of the <code>s1</code> attribute.
     *
     * @return the value of the <code>s1</code> attribute.
     */
    public String getS1()
    {
        return (String)get(s1Def);
    }
 
    /**
     * Sets the value of the <code>s1</code> attribute.
     *
     * @param value the value of the <code>s1</code> attribute.
     * @throws ValueRequiredException if you attempt to set a <code>null</code> 
     *         value.
     */
    public void setS1(String value)
        throws ValueRequiredException
    {
        try
        {
            if(value != null)
            {
                set(s1Def, value);
            }
            else
            {
                throw new ValueRequiredException("attribute s1 "+
                                                 "is declared as REQUIRED");
            }
        }
        catch(ModificationNotPermitedException e)
        {
            throw new BackendException("incompatible schema change",e);
        }
    }
     
    // @custom methods ///////////////////////////////////////////////////////
}
