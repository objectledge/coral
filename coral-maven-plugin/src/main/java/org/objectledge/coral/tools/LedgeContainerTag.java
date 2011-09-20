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
package org.objectledge.coral.tools;


import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.maven.jelly.MavenJellyContext;
import org.picocontainer.MutablePicoContainer;

/**
 * A tag for instantiating a Ledge Container in a Jelly script.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: LedgeContainerTag.java,v 1.5 2005-02-16 16:20:50 rafal Exp $
 */
public class LedgeContainerTag
    extends CoralPluginTag
{
    private String variable = "ledgeContainer";
    
    private String ledgeBaseDir = "";
    
    private String ledgeConfig = "";
    
    /**
     * Sets the var.
     *
     * @param var The var to set.
     */
    public void setVariable(String var)
    {
        this.variable = var;
    }

    /**
     * Sets the ledge base dir.
     *
     * @param ledgeBaseDir the ledge base dir path to set.
     */
    public void setLedgeBaseDir(String ledgeBaseDir)
    {
        this.ledgeBaseDir = ledgeBaseDir;
    }
    
    /**
     * Sets ledge config directory relative to base directory.
     * 
     * @param ledgeConfig config directory relative to base directory.
     */
    public void setLedgeConfig(String ledgeConfig)
    {
        this.ledgeConfig = ledgeConfig;
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
        String effectiveLedgeBaseDir = this.ledgeBaseDir.length() > 0 ? this.ledgeBaseDir :
            (String)pluginContext.getVariable("ledge.basedir");
        String effectiveLedgeConfig = this.ledgeConfig.length() > 0 ? this.ledgeConfig :
            (String)pluginContext.getVariable("ledge.config");
        if(container == null)
        {
            try
            {
                container = LedgeContainerFactory.newLedgeContainer(effectiveLedgeBaseDir, effectiveLedgeConfig);
            }
            catch(Exception e)
            {
                throw new JellyTagException("failed to initialize ObjectLedge", e);
            }
            pluginContext.setVariable(pluginContextVariable, container);
        }
        getContext().setVariable(variable, container);
    }
}
