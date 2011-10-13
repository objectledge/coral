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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.jelly.JellyTagException;
import org.apache.maven.jelly.MavenJellyContext;
import org.apache.maven.jelly.tags.BaseTagSupport;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.UnknownPluginException;
import org.apache.maven.project.Project;
import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;

/**
 * Common base class for Jelly tags used by the Coral Maven plugin.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralPluginTag.java,v 1.2 2004-12-27 02:34:09 rafal Exp $
 */
public abstract class CoralPluginTag extends BaseTagSupport
{
    /** The logger to use. */
    protected Logger log = getLog(LedgeContainerTag.class);

    /**
     * Retrieve MavenJellyContext of the coral-maven-plugin.
     * 
     * @return the MavenJellyContext.
     * @throws JellyTagException if the context could not be retrieved.
     */
    protected MavenJellyContext getPluginContext() throws JellyTagException
    {
        Project project = getMavenContext().getProject();
        String plugin = "coral-maven-plugin";
        try
        {
            PluginManager pluginManager = project.getContext().getMavenSession().getPluginManager();
            MavenJellyContext context = pluginManager.getPluginContext( plugin );
            if(context == null)
            {
                throw new JellyTagException("context for plugin '" + plugin + "' in project '" + 
                    project + "' is null" );
            }
            return context;
        }
        catch(UnknownPluginException e)
        {
            throw new JellyTagException("Plugin '" + plugin + "' in project '" + project + 
                "' is not available" );
        }
        catch(Exception e)
        {
            throw new JellyTagException("Error loading plugin", e);
        }
    
    }

    /**
     * Get the logger for the class.
     * 
     * @param cl the class.
     * @return the Logger.
     */
    protected Logger getLog(Class cl)
    {
        return new Log4JLogger(org.apache.log4j.Logger.getLogger(cl));
    }

    /**
     * Get the class loader.
     * 
     * @return the class loader.
     * @throws MalformedURLException if url is invalid.
     */
    protected ClassLoader getClassLoader() throws MalformedURLException
    {
        if(getMavenContext() != null)
        {
            String dependencyClasspath = getMavenContext().getProject().getDependencyClasspath();
            
            String buildDest = (String)getMavenContext().getVariable("maven.build.dest");
            StringTokenizer st = new StringTokenizer(dependencyClasspath, 
                System.getProperty("path.separator"));
            List temp = new ArrayList(st.countTokens()+1);
            while(st.hasMoreTokens())
            {
                temp.add(st.nextToken());
            }
            temp.add(buildDest+"/");
            URL[] urls = new URL[temp.size()];
            for(int i = 0; i<temp.size(); i++)
            {
                String element = (String)temp.get(i);
                urls[i] = new URL("file://"+element);
            }
            return new URLClassLoader(urls, getClass().getClassLoader());
        }
        else
        {
            return getClass().getClassLoader();
        }
    }
}
