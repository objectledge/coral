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
package org.objectledge.coral.tools.arl;

import javax.sql.DataSource;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.maven.jelly.MavenJellyContext;
import org.objectledge.coral.tools.CoralPluginTag;

/**
 * A tag for creating arl importer component from within a Jelly script.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ARLImporterTag.java,v 1.1 2005-01-13 09:49:07 pablo Exp $
 */
public class ARLImporterTag
    extends CoralPluginTag
{
    private String var = "arlImporter";
    
    private DataSource sourceDataSource;
    
    private DataSource targetDataSource;
    
    private String ledgeBaseDir = "";
    
    /**
     * Sets the var.
     *
     * @param var The var to set.
     */
    public void setVariable(String var)
    {
        this.var = var;
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
     * Sets the dataSource.
     *
     * @param sourceDataSource The dataSource to set.
     */
    public void setSourceDataSource(DataSource sourceDataSource)
    {
        this.sourceDataSource = sourceDataSource;
    }

    /**
     * Sets the dataSource.
     *
     * @param sourceDataSource The dataSource to set.
     */
    public void setTargetDataSource(DataSource targetDataSource)
    {
        this.targetDataSource = targetDataSource;
    }    
    
    /**
     * {@inheritDoc}
     */
    public void doTag(XMLOutput out) throws MissingAttributeException, JellyTagException
    {
        String pluginContextVariable = "coral.arlImporter";
        if(sourceDataSource == null)
        {
            throw new MissingAttributeException("sourceDataSource attribute undefined");
        }
        if(targetDataSource == null)
        {
            throw new MissingAttributeException("targetDataSource attribute undefined");
        }
        MavenJellyContext context = getPluginContext();
        ARLImporterComponent component = (ARLImporterComponent)context.
            getVariable(pluginContextVariable);
        if(component == null)
        {
            try
            {
                component = new ARLImporterComponent(sourceDataSource, targetDataSource, ledgeBaseDir);
            }
            catch(Exception e)
            {
                throw new JellyTagException("Exception occured ",e);
            }
            context.setVariable(pluginContextVariable, component);
        }
        getContext().setVariable(var, component);
    }
}
