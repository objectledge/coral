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

import javax.sql.DataSource;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.maven.jelly.MavenJellyContext;
import org.apache.maven.jelly.tags.BaseTagSupport;
import org.apache.maven.plugin.UnknownPluginException;
import org.apache.maven.project.Project;
import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.container.LedgeContainer;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.filesystem.FileSystemProvider;
import org.picocontainer.MutablePicoContainer;

/**
 * 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: LedgeContainerTag.java,v 1.1 2004-07-16 23:22:10 pablo Exp $
 */
public class LedgeContainerTag
    extends BaseTagSupport
{
    private String var = "ledgeContainer";
    
    private Logger log = getLog(LedgeContainerTag.class);
    
    private DataSource dataSource;
    
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
     * @param dataSource The dataSource to set.
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
        
    /**
     * {@inheritDoc}
     */
    public void doTag(XMLOutput out) throws MissingAttributeException, JellyTagException
    {
        String pluginContextVariable = "coral.ledgeContainer";
        if(dataSource == null)
        {
            throw new MissingAttributeException("dataSource attribute undefined");
        }
        MavenJellyContext context = getPluginContext();
        MutablePicoContainer container = (MutablePicoContainer)context.
            getVariable(pluginContextVariable);
        if(container == null)
        {
            try
            {
                container = getLedgeContainer(ledgeBaseDir);
            }
            catch(Exception e)
            {
                throw new JellyTagException("failed to initialize Coral", e);
            }
            context.setVariable(pluginContextVariable, container);
        }
        getContext().setVariable(var, container);
    }

    private MavenJellyContext getPluginContext()
        throws JellyTagException
    {
        Project project = getMavenContext().getProject();
        String plugin = "coral-maven-plugin";
        try
        {
            MavenJellyContext context = project.getPluginContext(plugin);
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
     * Returns a session factory instance.
     * 
     * @param ledgeBaseDir the fs base dir path with composition file.
     * @return a session factory instance.
     * @throws Exception if the factory could not be initialized.
     */
    public MutablePicoContainer getLedgeContainer(String ledgeBaseDir)
        throws Exception
    {
        checkAttribute(dataSource, "dataSource");
        ClassLoader cl = getClassLoader();
        MutablePicoContainer container = null;
        Thread.currentThread().setContextClassLoader(cl);
        if(ledgeBaseDir != null && ledgeBaseDir.length()> 0)
        {
            FileSystemProvider lfs = new org.objectledge.filesystem.
                LocalFileSystemProvider("local", ledgeBaseDir);
            FileSystemProvider cfs = new org.objectledge.filesystem.
                ClasspathFileSystemProvider("classpath", cl);
            FileSystem fs = new FileSystem(new FileSystemProvider[] { lfs, cfs }, 4096, 65536);
            LedgeContainer ledgeContainer = 
                new LedgeContainer(fs, "config/", cl);
            container = ledgeContainer.getContainer();
        }
        else
        {
            throw new Exception("the ledge base dir cannot be empty");
        }
        container.registerComponentInstance(ClassLoader.class, cl);
        return container;
    }
    
    private Logger getLog(Class cl)
    {
        return new Log4JLogger(org.apache.log4j.Logger.getLogger(cl));
    }

    /**
     * Get the class loader.
     * 
     * @return the class loader.
     * @throws MalformedURLException if url is invalid.
     */
    public ClassLoader getClassLoader() 
        throws MalformedURLException
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
