// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.entity.CoralRegistryImpl;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventHubImpl;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.CoralRelationManagerImpl;
import org.objectledge.coral.relation.CoralRelationQuery;
import org.objectledge.coral.relation.query.CoralRelationQueryImpl;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.CoralSchemaImpl;
import org.objectledge.coral.script.parser.RMLConstants;
import org.objectledge.coral.script.parser.RMLParserFactory;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.CoralSecurityImpl;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.CoralStoreImpl;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.event.EventWhiteboardFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralCoreImpl.java,v 1.9 2004-03-18 15:22:51 fil Exp $
 */
public class CoralCoreImpl
    implements CoralCore
{
    private CoralRegistry coralRegistry;
    private CoralSchema coralSchema;
    private CoralSecurity coralSecurity;
    private CoralStore coralStore;
    private CoralEventWhiteboard coralEventWhiteboard;
    private CoralRelationManager coralRelationManager;
    private CoralRelationQuery coralRelationQuery;
    private Instantiator instantiator;
    private RMLParserFactory rmlParserFactory;
    
    private MutablePicoContainer container;
    
    private ThreadLocal currentSession = new ThreadLocal();

    /**
     * Constructs a Coral instance.
     * 
     * @param parentContainer the container where Coral is deployed.
     * @param persistence the persitence subsystem.
     * @param cacheFactory the cache factory.
     * @param eventWhiteboardFactory the event whiteboard factory.
     * @param log the logger.
     */
    public CoralCoreImpl(PicoContainer parentContainer, Persistence persistence, 
        CacheFactory cacheFactory, EventWhiteboardFactory eventWhiteboardFactory, Logger log)
    {
        container = new DefaultPicoContainer(parentContainer);
        // register global dependencies
        container.registerComponentInstance(Persistence.class, persistence);
        container.registerComponentInstance(Database.class, persistence.getDatabase());
        container.registerComponentInstance(CacheFactory.class, cacheFactory);
        container.registerComponentInstance(EventWhiteboardFactory.class, eventWhiteboardFactory);
        // TODO multiple/polymorphic loggers?
        container.registerComponentInstance(Logger.class, log);
        // register self       
        container.registerComponentInstance(CoralCore.class, this);
        // events
        // TODO bridge support
        CoralEventHub coralEventHub = new CoralEventHubImpl(eventWhiteboardFactory, null);
        container.registerComponentInstance(CoralEventHub.class, coralEventHub);
        coralEventWhiteboard = coralEventHub.getGlobal();
        // instantiator
        instantiator = new PicoInstantiator(container);
        container.registerComponentInstance(Instantiator.class, instantiator);
        // RML parsers
        if(parentContainer.getComponentAdapterOfType(RMLParserFactory.class) == null)
        {
            rmlParserFactory = new RMLParserFactory();
            container.registerComponentInstance(RMLParserFactory.class, rmlParserFactory);
        }
        else
        {
            rmlParserFactory = (RMLParserFactory)parentContainer.
                getComponentInstanceOfType(RMLParserFactory.class);
        }
        // component implementations
        container.registerComponentImplementation(CoralRegistry.class, CoralRegistryImpl.class);
        container.registerComponentImplementation(CoralSchema.class, CoralSchemaImpl.class);
        container.registerComponentImplementation(CoralSecurity.class, CoralSecurityImpl.class);
        container.registerComponentImplementation(CoralStore.class, CoralStoreImpl.class);
        container.registerComponentImplementation(CoralRelationManager.class, 
            CoralRelationManagerImpl.class);
        container.registerComponentImplementation(CoralRelationQuery.class, 
            CoralRelationQueryImpl.class);
        // up it goes...
        coralRegistry = (CoralRegistry)container.getComponentInstance(CoralRegistry.class);
        coralSchema = (CoralSchema)container.getComponentInstance(CoralSchema.class);
        coralSecurity = (CoralSecurity)container.getComponentInstance(CoralSecurity.class);
        coralStore = (CoralStore)container.getComponentInstance(CoralStore.class);
        coralRelationManager = (CoralRelationManager)container.
            getComponentInstance(CoralRelationManager.class);
        coralRelationQuery = (CoralRelationQuery)container.
            getComponentInstance(CoralRelationQuery.class);
    }

    /** 
     * {@inheritDoc}
     */
    public CoralRegistry getRegistry()
    {
        return coralRegistry;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSchema getSchema()
    {
        return coralSchema;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSecurity getSecurity()
    {
        return coralSecurity;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralStore getStore()
    {
        return coralStore;
    }
    
    /** 
     * {@inheritDoc}
     */
    public CoralEventWhiteboard getEventWhiteboard()
    {
        return coralEventWhiteboard;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralQuery getQuery()
    {
        // TODO query support
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralRelationManager getRelationManager()
    {
        return coralRelationManager;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralRelationQuery getRelationQuery()
    {
        return coralRelationQuery;
    }
    
    /**
     * {@inheritDoc}
     */
    public Instantiator getInstantiator()
    {
        return instantiator;
    }
    
    /**
     * {@inheritDoc}
     */
    public RMLParserFactory getRMLParserFactory()
    {
        return rmlParserFactory;
    }
    
    /** 
     * {@inheritDoc}
     */
    public CoralSession getCurrentSession()
    {
        return (CoralSession)currentSession.get();
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSession setCurrentSession(CoralSession session)
    {
        CoralSession previous = (CoralSession)currentSession.get();
        currentSession.set(session);
        return previous;
    }
    
    /** 
     * {@inheritDoc}
     */
    public Subject getCurrentSubject() throws IllegalStateException
    {
        CoralSession session = getCurrentSession();
        if(session == null)
        {
            throw new IllegalStateException("thread is not associated with a Subject");
        }
        else
        {
            return session.getUserSubject();
        }
    }
}
