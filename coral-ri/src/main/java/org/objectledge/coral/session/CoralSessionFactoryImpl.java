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
package org.objectledge.coral.session;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralConfig;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.security.Subject;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.statistics.AbstractMuninGraph;
import org.objectledge.statistics.MuninGraph;
import org.objectledge.statistics.StatisticsProvider;

/**
 * An implementation of the Coral session factory.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSessionFactoryImpl.java,v 1.9 2005-02-21 11:51:24 rafal Exp $
 */
public class CoralSessionFactoryImpl
    implements CoralSessionFactory, StatisticsProvider
{
    private KeyedObjectPool pool;
    
    private CoralCore coral;

    private final Logger log;

    /**
     * Constructs a session factory instance.
     * 
     * @param coral the Coral component hub.
     * @param log the Logger to use.
     */
    public CoralSessionFactoryImpl(CoralCore coral, FileSystem fileSystem, Logger log)
    {
        this.coral = coral;
        this.log = log;
        pool = new GenericKeyedObjectPool(new Factory(), poolConfig(coral.getConfig()));
        graphs = new MuninGraph[] { new SessionPoolGraph(fileSystem) };
    }

    private GenericKeyedObjectPool.Config poolConfig(CoralConfig config)
    {
        GenericKeyedObjectPool.Config poolConfig = new GenericKeyedObjectPool.Config();
        poolConfig.whenExhaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
        poolConfig.maxIdle = config.getSessionPoolSizePerUser();
        poolConfig.minEvictableIdleTimeMillis = config.getSessionEvictionThreashold() * 1000;
        poolConfig.timeBetweenEvictionRunsMillis = config.getSessionEvictionInterval() * 1000;
        poolConfig.numTestsPerEvictionRun = config.getSessionTestsPerEvictionRun();
        return poolConfig;
    }
    
    /** 
     * {@inheritDoc}
     */
    public CoralSession getSession(Principal user)
        throws EntityDoesNotExistException
    {
        try
        {
            return (CoralSession)pool.borrowObject(user);
        }
        catch(NoSuchElementException e)
        {
            // commons-pools does not set exception root cause
            if(e.getMessage().matches(".* subject .* does not exist"))
            {
                throw new EntityDoesNotExistException(e.getMessage());
            }
            else
            {
                throw new BackendException("unxpected exception for commons-pool", e);
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new BackendException("failed to open session", e);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSession getRootSession()
    {
        try
        {
            Subject subject = coral.getSecurity().getSubject(Subject.ROOT);
            return getSession(subject.getPrincipal());
        }
        catch(Exception e)
        {
            throw new BackendException("failed to open superuser session", e);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSession getAnonymousSession()
    {
        try
        {
            Subject subject = coral.getSecurity().getSubject(Subject.ANONYMOUS);
            return getSession(subject.getPrincipal());
        }
        catch(Exception e)
        {
            throw new BackendException("failed to open superuser session", e);
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    public CoralSession getCurrentSession()
        throws IllegalStateException
    {
        CoralSession session = coral.getCurrentSession();
        if(session == null)
        {
            throw new IllegalStateException("no session is associated with this thread");
        }
        return session;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<CoralSession> getAllSessions()
    {
        return coral.getAllSessions();
    }

    /**
     * Commons pool integration point.
     */
    private class Factory extends BaseKeyedPoolableObjectFactory
    {    
        /** 
         * {@inheritDoc}
         */
        public void activateObject(Object principalObject, Object sessionObject) throws Exception
        {
            CoralSessionImpl session = (CoralSessionImpl)sessionObject;
            Principal principal = (Principal)principalObject;
            Subject subject = coral.getSecurity().getSubject(principal.getName());
            session.open(principal, subject);
        }

        /** 
         * {@inheritDoc}
         */
        public Object makeObject(Object arg0) throws Exception
        {
            return new CoralSessionImpl(coral, pool, log);
        }
    }    

    @Override
    public MuninGraph[] getGraphs()
    {
        return graphs;
    }

    private final MuninGraph[] graphs;

    public class SessionPoolGraph
        extends AbstractMuninGraph
    {
        public SessionPoolGraph(FileSystem fs)
        {
            super(fs);
        }

        @Override
        public String getId()
        {
            return "coral_sessions";
        }

        public int getActive()
        {
            return pool.getNumActive();
        }

        public int getIdle()
        {
            return pool.getNumIdle();
        }
    }
}
