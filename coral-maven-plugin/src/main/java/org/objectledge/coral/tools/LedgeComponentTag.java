// 
// Copyright (c) 2003-2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
//   
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//   
// * Redistributions of source code must retain the above copyright notice,  
// this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
// this list of conditions and the following disclaimer in the documentation  
// and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
// nor the names of its contributors may be used to endorse or promote products  
// derived from this software without specific prior written permission. 
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

package org.objectledge.coral.tools;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.maven.jelly.MavenJellyContext;
import org.picocontainer.MutablePicoContainer;

/**
 * Retrieves a ledge component from the container and stores it's reference into a Jelly variable.
 * <p>
 * use key or className attributes to select component. Use variable and scope (optional) properties
 * to select Jelly variable.
 * </p>
 * 
 * <p>
 * class and var parameters would be more appropriate but Jelly seems to intercept them for some
 * reason.
 * </p>
 * 
 * @author <a href="rafal@caltha.pl">Rafał Krzewski</a>
 * @version $Id: LedgeComponentTag.java,v 1.1 2005-10-31 08:48:59 rafal Exp $
 */
public class LedgeComponentTag extends CoralPluginTag
{   
    private String key;
    
    private String className;

    private String variable;
    
    private String scope;

    /**
     * Set the key property.
     * 
     * @param key the key property.
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * Set the class property.
     * 
     * @param className the class property.
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /**
     * Set the var property.
     *
     * @param var the var property.
     */
    public void setVariable(String var)
    {
        this.variable = var;
    }

    /**
     * Set the scope property.
     * 
     * @param scope the scope property.
     */
    public void setScope(String scope)
    {
        this.scope = scope;
    }

    /**
     * {@inheritDoc}
     */
    public void doTag(XMLOutput out) throws MissingAttributeException, JellyTagException
    {
        String pluginContextVariable = "coral.ledgeContainer";
        MavenJellyContext pluginContext = getPluginContext();
        MutablePicoContainer container = (MutablePicoContainer)pluginContext.
            getVariable(pluginContextVariable);        
        if(container == null) {
            throw new JellyTagException("ledge container not initialized");
        }
        if(key == null && className == null || key !=null && className != null) {
            throw new MissingAttributeException("eiter key or class attribute required");
        }
        Object component;
        if(key != null) {
            component = container.getComponentInstance(key);
        } else {
            try {
            Class componentClass = getClassLoader().loadClass(className);
            component = container.getComponentInstance(componentClass);
            } catch(Exception e) {
                throw new JellyTagException("failed to load component", e);
            }
        }
        if ( scope != null ) {
            context.setVariable(variable, scope, component);
        }
        else {
            context.setVariable(variable, component);
        }        
    }        
}
