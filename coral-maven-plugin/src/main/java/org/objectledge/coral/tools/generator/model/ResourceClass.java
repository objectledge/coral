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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.objectledge.coral.entity.EntityDoesNotExistException;

/**
 * Represents a Coral ResourceClass.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClass.java,v 1.4 2004-03-23 09:56:57 fil Exp $
 */
public class ResourceClass
    extends Entity
{
    // variables ////////////////////////////////////////////////////////////////////////////////
    
    private String implClassName;
    private String interfaceClassName;
    private String dbTable;
    
    private SortedMap attributes = new TreeMap();
    private SortedMap parentClasses = new TreeMap();
    
    // constructors /////////////////////////////////////////////////////////////////////////////
    
    /**
     * Construcs an instance of the ResourceClass.
     * 
     * @param name the name of the resource class.
     * @param javaClassName the name of the Java class for this resource class.
     * @param dbTable the database table for this resource class.
     * @param flags the flags for this resource class.
     */
    public ResourceClass(String name, String javaClassName, String dbTable, int flags)
    {
        super(name, flags);
        setJavaClassName(javaClassName);
        setDbTable(dbTable);
    }
    
    // accessors ////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the name of the implementation Java class for this resource class.
     * 
     * @return the name of the implementation Java class for this resource class.
     */    
    public String getImplClassName()
    {
        return implClassName;
    }
    
    /**
     * Returns the name of the implementation Java class for this resource class.
     * 
     * @return the name of the implementation Java class for this resource class.
     */    
    public String getInterfaceClassName()
    {
        return interfaceClassName;
    }
    
    /**
     * Returns the database table for this resource class.
     * 
     * @return the database table for this resource class.
     */
    public String getDbTable()
    {
        return dbTable;
    }
    
    /**
     * Returns the attributes of this resource class.
     * 
     * @return the attributes of this resource class.
     */    
    public List getAttributes()
    {
        return new ArrayList(attributes.values());
    }


    /**
     * Returns the attribute with the given name.
     * 
     * @param attributeName the name of the attribute.
     * @return the attribute definition.
     * @throws EntityDoesNotExistException if the attribute does not exist.
     */
    public Attribute getAttribute(String attributeName)
        throws EntityDoesNotExistException
    {
        Attribute result = (Attribute)attributes.get(attributeName);
        if(result == null)
        {
            throw new EntityDoesNotExistException("attribute "+attributeName+" not found");
        }
        return result;
    }
    
    /**
     * Returns the parent classes of this resource class.
     * 
     * @return the parent classes of this resource class.
     */    
    public List getParentClasses()
    {
        return new ArrayList(parentClasses.values());
    }
    
    // mutators //////////////////////////////////////////////////////////////

    /**
     * Sets the name of the Java class for this resource class.
     * 
     * @param javaClassName the name of the Java class for this resource class.
     */    
    public void setJavaClassName(String javaClassName)
    {
        if(javaClassName.endsWith("Impl"))
        {
            implClassName = javaClassName;
            interfaceClassName = javaClassName.substring(0, javaClassName.length()-4);
        }
        else
        {
            interfaceClassName = javaClassName;
            implClassName = javaClassName+"Impl";
        }
    }
    
    /**
     * Sets the database table for this resource class.
     * 
     * @param dbTable the database table for this resource class.
     */
    public void setDbTable(String dbTable)
    {
        this.dbTable = dbTable;
    }

    /**
     * Adds an attribute to this resource class.
     * 
     * @param attr an attribute.
     */    
    public void addAttribute(Attribute attr)
    {
        attributes.put(attr.getName(), attr);
        attr.setDeclaringClass(this);
    }
    
    /**
     * Removes an attribute from this resource class.
     * 
     * @param attr an attribute.
     */
    public void deleteAttribute(Attribute attr)
    {
        attributes.remove(attr.getName());
        attr.setDeclaringClass(null);
    }
    
    /**
     * Adds an parent class to this resource class.
     * 
     * @param parentClass a parent class.
     */
    public void addParentClass(ResourceClass parentClass)
    {
        parentClasses.put(parentClass.getName(), parentClass);
    }
    
    /**
     * Removes a parent class to this resource class.
     * 
     * @param parentClass a parent class.
     */
    public void deleteParentClass(ResourceClass parentClass)
    {
        parentClasses.remove(parentClass.getName());
    }
}
