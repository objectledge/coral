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

/**
 * Represents a Coral AttributeDefinition.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: Attribute.java,v 1.1 2004-03-22 11:54:49 fil Exp $
 */
public class Attribute
    extends Entity
{
    // variables /////////////////////////////////////////////////////////////////////////////////
    
    private AttributeClass attributeClass;
    private ResourceClass declaringClass;
    private String domain;
    
    /**
     * Creates an Attribute instance.
     * 
     * @param name the name of the attribute.
     * @param attributeClass the class of the attribute.
     * @param domain the attribute domain.
     * @param flags the flags of the attribute.
     */
    public Attribute(String name, AttributeClass attributeClass, 
        String domain, int flags)
    {
        super(name, flags);
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
