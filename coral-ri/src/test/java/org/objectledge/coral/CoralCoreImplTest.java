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

import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.jmock.Mock;
import org.objectledge.cache.CacheFactory;
import org.objectledge.context.Context;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.event.EventWhiteboardFactory;
import org.objectledge.threads.ThreadPool;
import org.objectledge.utils.LedgeTestCase;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralCoreImplTest.java,v 1.9 2005-01-28 01:04:19 rafal Exp $
 */
public class CoralCoreImplTest extends LedgeTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockCacheFactory;
    private CacheFactory cacheFactory;
    private ThreadPool threadPool;
    private EventWhiteboardFactory eventWhiteboardFactory;
    private Logger logger;
    
    private CoralCore coralCore;
    
    private Mock mockCoralSession;
    private CoralSession coralSession;
    private Mock mockSubject;
    private Subject subject;

    public void setUp()
    {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        mockDatabase = mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockPersistence = mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockPersistence.stubs().method("getDatabase").will(returnValue(database));
        mockCacheFactory = mock(CacheFactory.class);
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();
        mockCacheFactory.stubs().method("getInstance").will(returnValue(new HashMap()));
        logger = new Log4JLogger(org.apache.log4j.Logger.getLogger(getClass()));        
        threadPool = new ThreadPool(null, new Context(), null, logger);
        eventWhiteboardFactory = new EventWhiteboardFactory(null, logger, threadPool);
        PicoContainer emptyContainer = new DefaultPicoContainer();
        
        coralCore = new CoralCoreImpl(emptyContainer, persistence, cacheFactory, 
            eventWhiteboardFactory, logger, false);

        mockCoralSession = mock(CoralSession.class);
        coralSession = (CoralSession)mockCoralSession.proxy();
        mockSubject = mock(Subject.class);
        subject = (Subject)mockSubject.proxy();               
    }
    
    public void tearDown()
    {
        threadPool.stop();
    }
    
    public void testBasics()
    {
        coralCore.getSchema();
        coralCore.getSecurity();
        coralCore.getStore();
        coralCore.getRegistry();
        coralCore.getEventWhiteboard();
        coralCore.getQuery();
    }
    
    public void testSessionTracking()
    {
        assertNull(coralCore.getCurrentSession());
        try
        {
            coralCore.getCurrentSubject();
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(IllegalStateException.class, e.getClass());
            assertEquals("thread is not associated with a Subject", e.getMessage());
        }
        
        coralCore.setCurrentSession(coralSession);
        assertSame(coralSession, coralCore.getCurrentSession());
        
        Runnable runnable = new Runnable() 
        {
            public void run()
            {
                assertNull(coralCore.getCurrentSession());
            }
        };
        new Thread(runnable).start();
    }
}
