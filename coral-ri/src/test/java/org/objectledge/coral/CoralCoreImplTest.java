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
import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.cache.CacheFactory;
import org.objectledge.context.Context;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.event.EventWhiteboardFactory;
import org.objectledge.threads.ThreadPool;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralCoreImplTest.java,v 1.1 2004-03-05 13:00:52 fil Exp $
 */
public class CoralCoreImplTest extends MockObjectTestCase
{
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockCacheFactory;
    private CacheFactory cacheFactory;
    private ThreadPool threadPool;
    private EventWhiteboardFactory eventWhiteboardFactory;
    private Logger logger;

    public void setUp()
    {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        mockPersistence = new Mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockCacheFactory = new Mock(CacheFactory.class);
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();
        mockCacheFactory.stub().method("getInstance").will(returnValue(new HashMap()));
        logger = new Log4JLogger(org.apache.log4j.Logger.getLogger(getClass()));        
        threadPool = new ThreadPool(null, new Context(), null, logger);
        eventWhiteboardFactory = new EventWhiteboardFactory(null, logger, threadPool);
    }
    
    public void tearDown()
    {
        threadPool.stop();
    }
    
    public void testStartup()
    {
        CoralCore coral = new CoralCoreImpl(persistence, cacheFactory, eventWhiteboardFactory, logger);
        coral.getSchema();
    }
}
