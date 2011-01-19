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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jcontainer.dna.Configuration;
import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.Logger;
import org.objectledge.ComponentInitializationError;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.entity.CoralRegistryImpl;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventHubImpl;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.query.SQLCoralQueryImpl;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.CoralRelationManagerImpl;
import org.objectledge.coral.relation.CoralRelationQuery;
import org.objectledge.coral.relation.query.CoralRelationQueryImpl;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.CoralSchemaImpl;
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
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * Coral core component implemenation.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralCoreImpl.java,v 1.23 2008-01-02 00:29:24 rafal Exp $
 */
public class CoralCoreImpl
    implements CoralCore, Startable
{
    private CoralRegistry coralRegistry;
    private CoralSchema coralSchema;
    private CoralSecurity coralSecurity;
    private CoralStore coralStore;
    private CoralEventWhiteboard coralEventWhiteboard;
    private CoralRelationManager coralRelationManager;
    private CoralRelationQuery coralRelationQuery;
    private CoralQuery coralQuery;
    private Instantiator instantiator;
    private RMLParserFactory rmlParserFactory;
    
    private MutablePicoContainer container;
    
    private ThreadLocal<CoralSession> currentSession = new ThreadLocal<CoralSession>();
    private ThreadLocal<LinkedList<CoralSession>> currentSessionStack = 
        new ThreadLocal<LinkedList<CoralSession>>();
    private final Logger log;
    private final Set<Feature> features;
    private final CacheFactory cacheFactory;
    
    /**
     * Constructs a Coral instance.
     * @param parentContainer the container where Coral is deployed.
     * @param persistence the persitence subsystem.
     * @param cacheFactory the cache factory.
     * @param eventWhiteboardFactory the event whiteboard factory.
     * @param log the logger.
     * @param preload attempt to preload data from database.
     */
    public CoralCoreImpl(PicoContainer parentContainer, Persistence persistence, 
        CacheFactory cacheFactory, EventWhiteboardFactory eventWhiteboardFactory, Logger log, 
        boolean preload)
    {
        this(parentContainer, persistence, cacheFactory, eventWhiteboardFactory, log,
            (preload ? EnumSet.of(Feature.PRELOAD) : EnumSet.noneOf(Feature.class)));
    }

    /**
     * Constructs a Coral instance.
     * @param parentContainer the container where Coral is deployed.
     * @param persistence the persitence subsystem.
     * @param cacheFactory the cache factory.
     * @param eventWhiteboardFactory the event whiteboard factory.
     * @param log the logger.
     * @param config component configuration.
     * @throws ConfigurationException if the provided configuration is incorrect. 
     */
    public CoralCoreImpl(PicoContainer parentContainer, Persistence persistence, 
        CacheFactory cacheFactory, EventWhiteboardFactory eventWhiteboardFactory, Logger log, 
        Configuration config)
        throws ConfigurationException
    {
        this(parentContainer, persistence, cacheFactory, eventWhiteboardFactory, log,
            getFeatureSet(config));
    }

    /**
     * Build a set of Features from DNA Configuration object.
     * 
     * @param config configuration object.
     * @return set of Features.
     * @throws ConfigurationException if the provided configuration is incorrect. 
     */
    private static Set<Feature> getFeatureSet(Configuration config)
        throws ConfigurationException
    {
        Set<Feature> featureSet = EnumSet.noneOf(Feature.class);
        Configuration[] features = config.getChild("features").getChildren("feature");
        for(Configuration feature : features)
        {
            String featureName = feature.getValue();
            try
            {
                featureSet.add(Feature.valueOf(featureName));
            }
            catch(IllegalArgumentException e)
            {
                throw new ConfigurationException("unknown feature "+featureName, 
                    feature.getPath(), feature.getLocation());
            }
        }
        return featureSet;
    }
    
    /**
     * Constructs a Coral instance.
     * @param parentContainer the container where Coral is deployed.
     * @param persistence the persitence subsystem.
     * @param cacheFactory the cache factory.
     * @param eventWhiteboardFactory the event whiteboard factory.
     * @param log the logger.
     * @param features the optiona feature set.
     */
    public CoralCoreImpl(PicoContainer parentContainer, Persistence persistence, 
        CacheFactory cacheFactory, EventWhiteboardFactory eventWhiteboardFactory, Logger log, 
        Set<Feature> features)
    {
        this.log = log;
        container = new DefaultPicoContainer(parentContainer);
        // register global dependencies
        container.registerComponentInstance(Persistence.class, persistence);
        container.registerComponentInstance(Database.class, persistence.getDatabase());
        container.registerComponentInstance(CacheFactory.class, cacheFactory);
        container.registerComponentInstance(EventWhiteboardFactory.class, eventWhiteboardFactory);
        container.registerComponentInstance(Logger.class, log);
        // register self       
        container.registerComponentInstance(CoralCore.class, this);
        // events
        CoralEventHub coralEventHub = new CoralEventHubImpl(eventWhiteboardFactory, null);
        container.registerComponentInstance(CoralEventHub.class, coralEventHub);
        coralEventWhiteboard = coralEventHub.getGlobal();
        // instantiator
        instantiator = new PicoInstantiator(container);
        this.cacheFactory = cacheFactory;
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
        container.registerComponentImplementation(CoralQuery.class, SQLCoralQueryImpl.class);
        // up it goes...
        coralRegistry = (CoralRegistry)container.getComponentInstance(CoralRegistry.class);
        coralSchema = (CoralSchema)container.getComponentInstance(CoralSchema.class);
        coralSecurity = (CoralSecurity)container.getComponentInstance(CoralSecurity.class);
        coralStore = (CoralStore)container.getComponentInstance(CoralStore.class);
        coralRelationManager = (CoralRelationManager)container.
            getComponentInstance(CoralRelationManager.class);
        coralRelationQuery = (CoralRelationQuery)container.
            getComponentInstance(CoralRelationQuery.class);
        coralQuery = (CoralQuery)container.getComponentInstance(CoralQuery.class);
        
        this.features = features;
    }
    
    /**
     * {@inheritDoc}
     */
    public void start()
    {
        if(isEnabled(Feature.PRELOAD))
        {
            List startupAdapters = container.
                getComponentAdaptersOfType(PreloadingParticipant.class);
            Set<PreloadingParticipant> participants = 
                new HashSet<PreloadingParticipant>(startupAdapters.size());
            for(Object adapter : startupAdapters)
            {
                participants.add((PreloadingParticipant)((ComponentAdapter)adapter)
                    .getComponentInstance(container));
            }
            try
            {
                preloadData(participants);
            }
            catch(Exception e)
            {
                throw new ComponentInitializationError("startup failed", e);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void stop()
    {
        // I wish Startable iterface was split back into Startable/Stoppable        
    }

    // configuration ////////////////////////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Feature feature)
    {
        return features.contains(feature);
    }
    
    // facilities ///////////////////////////////////////////////////////////////////////////////
    
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
        return coralQuery;
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
    public CacheFactory getCacheFactory()
    {
        return cacheFactory;
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
    public void pushSession(CoralSession session)
    {
        LinkedList<CoralSession> sessionStack = currentSessionStack.get();
        if(sessionStack == null)
        {
            sessionStack = new LinkedList<CoralSession>();
            currentSessionStack.set(sessionStack);
        }
        sessionStack.add(session);
    }
    
    /**
     * {@inheritDoc}
     */
    public CoralSession peekSession()
    {
        LinkedList<CoralSession> sessionStack = currentSessionStack.get();
        if(sessionStack == null)
        {
            return null;
        }
        if(sessionStack.isEmpty())
        {
            return null;
        }
        return sessionStack.getLast();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<CoralSession> getAllSessions()
    {        
        LinkedList<CoralSession> sessionStack = currentSessionStack.get();
        if(sessionStack == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return new LinkedList<CoralSession>(sessionStack);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeSession(CoralSession session)
        throws IllegalArgumentException
    {
        LinkedList<CoralSession> sessionStack = currentSessionStack.get();
        if(sessionStack == null)
        {
            throw new IllegalArgumentException("no sessions are associated with the thread.");
        }
        if(!sessionStack.contains(session))
        {
            if(log != null && log.isDebugEnabled())
            {
                log.debug("session opened at", session.getOpeningStackTrace());
            }
            throw new IllegalArgumentException("session is not associated with this thread.");
        }
        sessionStack.remove(session);
    }
    
    /**
     * {@inheritDoc}
     */
    public CoralSession getCurrentSession()
    {
        return currentSession.get();
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSession setCurrentSession(CoralSession session)
    {
        CoralSession previous = currentSession.get();
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
    
    // startup //////////////////////////////////////////////////////////////////////////////////
    
    private void preloadData(Set<PreloadingParticipant> participants)
        throws Exception
    {
        SortedMap<Integer, PreloadingParticipant> order = new TreeMap();
        for (PreloadingParticipant participant : participants)
        {
            for (int phase : participant.getPhases())
            {
                order.put(phase, participant);
            }
        }
        for (int phase : order.keySet())
        {
            order.get(phase).preloadData(phase);
        }
    }
}
