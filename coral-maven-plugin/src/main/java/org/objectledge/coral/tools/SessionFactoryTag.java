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

import javax.sql.DataSource;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.maven.jelly.MavenJellyContext;
import org.jcontainer.dna.impl.DefaultConfiguration;
import org.objectledge.cache.CacheFactory;
import org.objectledge.cache.DefaultCacheFactory;
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
import org.objectledge.parameters.db.DBParametersManager;
import org.objectledge.parameters.db.DefaultDBParametersManager;
import org.objectledge.threads.ThreadPool;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * A tag for opening Coral sessions from within a Jelly script. 
 * 
 * <p> If a Pico container is bound under
 * <code>coral.ledgeCongtainer</code> variable, it is used for getting the session factory.
 * Otherwise, the session factory is provided by a Coral instance built on-the-fly. The session
 * factory is bound in the context under <code>coral.sessionFactory</code> variable for future use
 * (will be returned from consequtive invocations). </p>
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski </a>
 * @version $Id: SessionFactoryTag.java,v 1.15 2008-01-20 15:44:10 rafal Exp $
 */
public class SessionFactoryTag
    extends CoralPluginTag
{
    private String variable = "coralSessionFactory";
    
    private DataSource dataSource;
    
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
        MavenJellyContext context = getPluginContext();
        CoralSessionFactory factory = (CoralSessionFactory)context.
            getVariable(pluginContextVariable);
        if(factory == null)
        {
            MutablePicoContainer container = (MutablePicoContainer)context.
                getVariable("coral.ledgeContainer");
            if(container == null)
            {
                try
                {
                    factory = getSessionFactory();
                }
                catch(Exception e)
                {
                    throw new JellyTagException("failed to initialize Coral", e);
                }
            }
            else
            {
                factory = (CoralSessionFactory)container.getComponentInstance(
                    CoralSessionFactory.class);
            }
            context.setVariable(pluginContextVariable, factory);
        }
        getContext().setVariable(variable, factory);
    }

    /**
     * Returns a session factory from Coral insatnce composed by hand.
     * 
     * @return a session factory instance.
     * @throws Exception if the factory could not be initialized.
     */
    public CoralSessionFactory getSessionFactory()
        throws Exception
    {
        System.out.println("ledge.basedir not defined - using hardcoded Coral composition");
        checkAttribute(dataSource, "dataSource");
        MutablePicoContainer container = new DefaultPicoContainer();
        IdGenerator idGenerator = new IdGenerator(dataSource);
        Context context = new Context();
        ClassLoader cl = getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        container.registerComponentInstance(ClassLoader.class, cl);
        Transaction transaction = new JotmTransaction(0, 120, context, getLog(Transaction.class), null);
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
            threadPool, null, null, null);
        EventWhiteboardFactory eventWhiteboardFactory = new EventWhiteboardFactory(null, 
            getLog(EventWhiteboardFactory.class), threadPool, cacheFactory);
        DBParametersManager dbParameterManager = new DefaultDBParametersManager(database,
            getLog(DBParametersManager.class));
        container.registerComponentInstance(DBParametersManager.class, dbParameterManager);
        
        CoralCore coralCore = new CoralCoreImpl(container, persistence, cacheFactory, 
            eventWhiteboardFactory, getLog(CoralCore.class), false);
        CoralSessionFactory factory = new CoralSessionFactoryImpl(coralCore, 
            getLog(CoralCore.class));
        container.registerComponentInstance(CoralSessionFactory.class, factory);
        return factory;
    }
}
