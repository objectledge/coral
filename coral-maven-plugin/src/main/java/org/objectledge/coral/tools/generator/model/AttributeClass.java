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
 * Represents a Coral AttributeClass
 *  
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeClass.java,v 1.2 2004-03-23 16:16:39 fil Exp $
 */
public class AttributeClass
    extends Entity
{
    // variables ////////////////////////////////////////////////////////////////////////////////
    
    /** The name of the Java class for this attribute. */
    private String javaClassName;
    
    /** The actual java class of the attribute. */
    private Class javaClass;
    
    // constructors /////////////////////////////////////////////////////////////////////////////
    
    /** Creates an attribute class instance. 
     *
     * @param name the name of the attribute class.
     * @param javaClassName the name of the Java class for this attribute.
     * @throws ClassNotFoundException if the specified Java class could not be loaded. 
     */
    public AttributeClass(String name, String javaClassName)
        throws ClassNotFoundException
    {
        super(name, 0);
        setJavaClassName(javaClassName);
    }
    
    // accessors ////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the name of the Java class for this attribute class.
     * 
     * @return the name of the Java class for this attribute class.
     */
    public String getJavaClassName()
    {
        return javaClassName;
    }
    
    /**
     * Returns the Java class for this attribute class.
     * 
     * @return the Java class for this attribute class.
     */
    public Class getJavaClass()
    {
        return javaClass;
    }
    
    // mutators /////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Sets the name of the Java class for this attribute class.
     * 
     * @param javaClassName the name of the Java class for this attribute.
     * @throws ClassNotFoundException if the specified Java class could not be loaded. 
     */
    public void setJavaClassName(String javaClassName)
        throws ClassNotFoundException
    {
        this.javaClassName = javaClassName; 
        javaClass = Class.forName(javaClassName);
    }
}
