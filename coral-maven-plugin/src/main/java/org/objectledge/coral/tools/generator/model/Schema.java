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

/**
 * Represents a Coral Schema.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: Schema.java,v 1.2 2004-03-22 12:14:28 fil Exp $
 */
public class Schema
{
    private SortedMap attributeClasses;
    private SortedMap resourceClasses;

    // attribute classes ////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns all the defined attribute classes.
     * 
     * @return all the defined attribute classes.
     */
    public List getAttributeClasses()
    {
        return new ArrayList(attributeClasses.values());
    }
    
    /**
     * Returns an attribute class with the specified name.
     * 
     * @param name the name of the attribute class.
     * @return the attribute class.
     */
    public AttributeClass getAttributeClass(String name)
    {
        return (AttributeClass)attributeClasses.get(name); 
    }
    
    /**
     * Adds an attribute class to the model.
     * 
     * @param attributeClass the attribute class.
     */
    public void addAttributeClass(AttributeClass attributeClass)
    {
        attributeClasses.put(attributeClass.getName(), attributeClass);
    }
    
    // resource classes /////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns all the defined resource classes.
     * 
     * @return all the defined resource classes.
     */
    public List getResourceClasses()
    {
        return new ArrayList(resourceClasses.values());
    }
    
    /**
     * Returns an resource class with the specified name.
     * 
     * @param name the name of the resource class.
     * @return the resource class.
     */
    public ResourceClass getResourceClass(String name)
    {
        return (ResourceClass)resourceClasses.get(name); 
    }
    
    /**
     * Adds a resource class to the model.
     * 
     * @param resourceClass the resource class.
     */
    public void addResourceClass(ResourceClass resourceClass)
    {
        resourceClasses.put(resourceClass.getName(), resourceClass);
    }
}
