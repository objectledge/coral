// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//  
// * Redistributions of source code must retain the above copyright notice,  
//	 this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//	 this list of conditions and the following disclaimer in the documentation  
//	 and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//	 nor the names of its contributors may be used to endorse or promote products  
//	 derived from this software without specific prior written permission. 
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
package org.objectledge.coral.tools.generator.model;

import java.lang.reflect.Field;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.store.Resource;

/**
 * Represents a Coral AttributeDefinition.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: Attribute.java,v 1.3 2004-03-25 13:32:16 fil Exp $
 */
public class Attribute
    extends Entity
{
    // variables /////////////////////////////////////////////////////////////////////////////////
    
    private Schema schema;
    private AttributeClass attributeClass;
    private ResourceClass declaringClass;
    private String domain;
    
    /**
     * Protected no-arg constructor, to allow mocking.
     */
    protected Attribute()
    {
    }
    
    /**
     * Creates an Attribute instance.
     * 
     * @param schema the schema this attribute belongs to.
     * @param name the name of the attribute.
     * @param attributeClass the class of the attribute.
     * @param domain the attribute domain.
     * @param flags the flags of the attribute.
     */
    public Attribute(Schema schema, String name, AttributeClass attributeClass, 
        String domain, int flags)
    {
        super(name, flags);
        this.schema = schema;
        this.attributeClass = attributeClass;
        setDomain(domain);
    }
        
    // accessors ////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the class of the attribute.
     * 
     * @return the class of the attribute.
     */    
    public AttributeClass getAttributeClass()
    {
        return attributeClass;
    }
    
    /**
     * Returns the resource class that declares this attribute.
     * 
     * @return the resource class that declares the attribute.
     */
    public ResourceClass getDeclaringClass()
    {
        return declaringClass;
    }
    
    /**
     * Returns the domain of the attribute.
     * 
     * @return the domain of the attribute.
     */
    public String getDomain()
    {
        return domain;
    }    
    
    /**
     * Checks if the attribute is represented by a primitive java type.
     * 
     * @return <code>true</code> if the attribute is represented by a primitive java type.
     */
    public boolean isPrimitive()
    {
        if(attributeClass.getJavaClassName().startsWith("java.lang"))
        {
            try
            {
                Field f = attributeClass.getJavaClass().getField("TYPE");
                return true;
            }
            catch(NoSuchFieldException e)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns fully qualified Java type name of the attribute.
     * 
     * @return fully qualified Java type name of the attribute.
     * @throws EntityDoesNotExistException if a resource class referenced by this attribute does 
     * not exist. 
     */
    public String getFQJavaType()
        throws EntityDoesNotExistException
    {
        String type = attributeClass.getJavaClassName();
        if(attributeClass.getJavaClassName().startsWith("java.lang"))
        {
            try
            {
                Field f = attributeClass.getJavaClass().getField("TYPE");
                type = ((Class)f.get(null)).getName();
            }
            catch(NoSuchFieldException e)
            {
                // non primitive
            }
            catch(Exception e)
            {
                throw (RuntimeException)new IllegalStateException("introspecting java.lang failed").
                    initCause(e);
            }
        }
        else
        {
            if(Resource.class.isAssignableFrom(attributeClass.getJavaClass())  && domain != null)
            {
                type = schema.getResourceClass(domain).getFQInterfaceClassName();
            }
        }
        return type;
    }

    /**
     * Returns non-qualified Java type name of the attribute.
     * 
     * @return non-qualified Java type name of the attribute.
     * @throws EntityDoesNotExistException if a resource class referenced by this attribute does 
     * not exist. 
     */
    public String getJavaType()
        throws EntityDoesNotExistException
    {
        String type = getFQJavaType();
        int lastDot = type.lastIndexOf('.');
        if(lastDot >= 0)
        {
            return type.substring(lastDot+1); 
        }
        else
        {
            return type;
        }
    }

    /**
     * Checks if this attribute is REQUIRED.
     * 
     * @return <code>true</code> if this attribuge is REQUIRED.
     */
    public boolean isRequired()
    {
        return hasFlags(AttributeFlags.REQUIRED);
    }

    /**
     * Checks if this attribute is REQUIRED.
     * 
     * @return <code>true</code> if this attribuge is REQUIRED.
     */
    public boolean isReadonly()
    {
        return hasFlags(AttributeFlags.READONLY);
    }
    
    /**
     * Checks if the attribute is neither BUILTIN nor SYNTHETIC.
     * 
     * @return <code>true</code> if the attribute is neither BUILTIN nor SYNTHETIC.
     */
    public boolean isConcrete()
    {
        return !(hasFlags(AttributeFlags.BUILTIN) || hasFlags(AttributeFlags.SYNTHETIC));
    }
    
    // mutators /////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the domain of the attribute.
     * 
     * @param domain the domain of the attribute.
     */    
    public void setDomain(String domain)
    {
        this.domain = domain;
    }
    
    /**
     * Sets the declaring class for the attribute.
     * 
     * @param declaringClass the class that declares the attribute.
     */
    public void setDeclaringClass(ResourceClass declaringClass)
    {
        this.declaringClass = declaringClass;
    }
}
