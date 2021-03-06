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
import org.objectledge.coral.entity.EntityExistsException;

/**
 * Represents a Coral Schema.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: Schema.java,v 1.7 2004-05-07 10:07:21 fil Exp $
 */
public class Schema
{
    private SortedMap<String, AttributeClass> attributeClasses = new TreeMap<String, AttributeClass>();
    private SortedMap<String, ResourceClass> resourceClasses = new TreeMap<String, ResourceClass>();

    // attribute classes ////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns all the defined attribute classes.
     * 
     * @return all the defined attribute classes.
     */
    public List<AttributeClass> getAttributeClasses()
    {
        return new ArrayList<AttributeClass>(attributeClasses.values());
    }
    
    /**
     * Returns an attribute class with the specified name.
     * 
     * @param name the name of the attribute class.
     * @return the attribute class.
     * @throws EntityDoesNotExistException if the attribute class does not exist.
     */
    public AttributeClass getAttributeClass(String name) 
        throws EntityDoesNotExistException
    {
        AttributeClass result = attributeClasses.get(name);
        if(result == null)
        {
            throw new EntityDoesNotExistException("attribute class "+name+" not found"); 
        }
        return result;
    }
    
    /**
     * Adds an attribute class to the model.
     * 
     * @param attributeClass the attribute class.
     * @throws EntityExistsException if the schema already contains a resource class with the same
     *         name as the class being added.
     */
    public void addAttributeClass(AttributeClass attributeClass)
        throws EntityExistsException
    {
        if(attributeClasses.containsKey(attributeClass.getName()))
        {
            throw new EntityExistsException("attribute class "+attributeClass.getName()+
                " already exists");
        }
        attributeClasses.put(attributeClass.getName(), attributeClass);
    }
    
    /**
     * Deletes the attribute class from the model.
     * 
     * @param attributeClass the attribute class.
     * @throws EntityDoesNotExistException if the attribute class is not present in the schema.
     */
    public void deleteAttributeClass(AttributeClass attributeClass)
        throws EntityDoesNotExistException
    {
        if(!attributeClasses.values().contains(attributeClass))
        {
            throw new EntityDoesNotExistException("attribute class "+attributeClass.getName()+
                " is not present in the schema");
        }
        attributeClasses.values().remove(attributeClass);
    }
    
    // resource classes /////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns all the defined resource classes.
     * 
     * @return all the defined resource classes.
     */
    public List<ResourceClass> getResourceClasses()
    {
        return new ArrayList<ResourceClass>(resourceClasses.values());
    }
    
    /**
     * Returns an resource class with the specified name.
     * 
     * @param name the name of the resource class.
     * @return the resource class.
     * @throws EntityDoesNotExistException if the attribute class does not exist.
     */
    public ResourceClass getResourceClass(String name) 
        throws EntityDoesNotExistException
    {
        ResourceClass result = resourceClasses.get(name); 
        if(result == null)
        {
            throw new EntityDoesNotExistException("resource class "+name+" not found"); 
        }
        return result;
    }
    
    /**
     * Adds a resource class to the model.
     * 
     * @param resourceClass the resource class.
     * @throws EntityExistsException if the schema already contains a resource class with the same
     *         name as the class being added.
     */
    public void addResourceClass(ResourceClass resourceClass)
        throws EntityExistsException
    {
        if(resourceClasses.containsKey(resourceClass.getName()))
        {
            throw new EntityExistsException("resource class "+resourceClass.getName()+
                " already exists");
        }
        resourceClasses.put(resourceClass.getName(), resourceClass);
    }

    /**
     * Deletes the resource class from the model.
     * 
     * @param resourceClass the resource class.
     * @throws EntityDoesNotExistException if the attribute class is not present in the schema.
     */
    public void deleteResourceClass(ResourceClass resourceClass)
        throws EntityDoesNotExistException
    {
        if(!resourceClasses.values().contains(resourceClass))
        {
            throw new EntityDoesNotExistException("resource class "+resourceClass.getName()+
                " is not present in the schema");
        }
        resourceClasses.values().remove(resourceClass);
    }
}
