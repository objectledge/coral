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
import org.jcontainer.dna.impl.DefaultConfiguration;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.cache.DefaultCacheFactory;
import org.objectledge.container.LedgeContainer;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.CoralCoreImpl;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.session.CoralSessionFactoryImpl;
import org.objectledge.database.Database;
import org.objectledge.database.DefaultDatabase;
import org.objectledge.database.IdGenerator;
import org.objectledge.database.JotmTransaction;
import org.objectledge.database.Transaction;
import org.objectledge.database.persistence.DefaultPersistence;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.event.EventWhiteboardFactory;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.filesystem.FileSystemProvider;
import org.objectledge.threads.ThreadPool;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionFactoryTag.java,v 1.7 2004-06-25 11:21:53 fil Exp $
 */
public class SessionFactoryTag
    extends BaseTagSupport
{
    private String var = "coralSessionFactory";
    
    private Logger log = getLog(SessionFactoryTag.class);
    
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
        String pluginContextVariable = "coral.sessionFactory";
        if(dataSource == null)
        {
            throw new MissingAttributeException("dataSource attribute undefined");
        }
        MavenJellyContext context = getPluginContext();
        CoralSessionFactory factory = (CoralSessionFactory)context.
            getVariable(pluginContextVariable);
        if(factory == null)
        {
            try
            {
                factory = getSessionFactory(ledgeBaseDir);
            }
            catch(Exception e)
            {
                throw new JellyTagException("failed to initialize Coral", e);
            }
            context.setVariable(pluginContextVariable, factory);
        }
        getContext().setVariable(var, factory);
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
    public CoralSessionFactory getSessionFactory(String ledgeBaseDir)
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
            container = new DefaultPicoContainer();
        }
        container.registerComponentInstance(ClassLoader.class, cl);
        CoralSessionFactory factory = (CoralSessionFactory)container.
            getComponentInstance(CoralSessionFactory.class);
        if(factory != null)
        {
            return factory;
        }       
        IdGenerator idGenerator = new IdGenerator(dataSource);
        Context context = new Context();
        Transaction transaction = new JotmTransaction(0, context, getLog(Transaction.class), null);
        Database database = new DefaultDatabase(dataSource, idGenerator, transaction); 
        Persistence persistence = new DefaultPersistence(database, getLog(Persistence.class));
        ThreadPool threadPool = new ThreadPool(null, context, null, getLog(ThreadPool.class));

        DefaultConfiguration cacheConfig = new DefaultConfiguration("config", "", "");
        DefaultConfiguration cacheConfigByName = new DefaultConfiguration("alias", "", "config");
        cacheConfig.addChild(cacheConfigByName);
        cacheConfigByName.setAttribute("name", "coral.byName");
        DefaultConfiguration cacheConfigByNameValue = 
             new DefaultConfiguration("config", "", "config/coral.byName");
        cacheConfigByName.addChild(cacheConfigByNameValue);
        cacheConfigByNameValue.setValue("HashMap()");
        DefaultConfiguration cacheConfigById = new DefaultConfiguration("alias", "", "config");
        cacheConfig.addChild(cacheConfigById);
        cacheConfigById.setAttribute("name", "coral.byId");
        DefaultConfiguration cacheConfigByIdValue = 
            new DefaultConfiguration("config", "", "config/coral.byId");
        cacheConfigById.addChild(cacheConfigByIdValue);
        cacheConfigByIdValue.setValue("HashMap()");
        DefaultConfiguration cacheConfigAll = new DefaultConfiguration("alias", "", "config");
        cacheConfig.addChild(cacheConfigAll);
        cacheConfigAll.setAttribute("name", "coral.all");
        DefaultConfiguration cacheConfigAllValue = 
            new DefaultConfiguration("config", "", "config/coral.all");
        cacheConfigAll.addChild(cacheConfigAllValue);
        cacheConfigAllValue.setValue("HashMap()");

        CacheFactory cacheFactory = new DefaultCacheFactory(cacheConfig, getLog(CacheFactory.class),
            threadPool, null, null);
        EventWhiteboardFactory eventWhiteboardFactory = new EventWhiteboardFactory(null, 
            getLog(EventWhiteboardFactory.class), threadPool);
        CoralCore coralCore = new CoralCoreImpl(container, persistence, cacheFactory, 
            eventWhiteboardFactory, getLog(CoralCore.class));
        factory = new CoralSessionFactoryImpl(coralCore);
        container.registerComponentInstance(CoralSessionFactory.class, factory);
        return factory;
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
