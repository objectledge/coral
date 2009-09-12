package org.objectledge.coral.store;

import java.util.HashMap;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistentFactory;
import org.objectledge.test.LedgeTestCase;

public class CoralStoreImplTest
    extends LedgeTestCase
{
    
    private Mock mockCacheFactory;
    private CacheFactory cacheFactory;
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockCoralEventHub;
    private CoralEventHub coralEventHub;
    private Mock mockPersistentFactory;
    private PersistentFactory persistentFactory;
    private Mock mockInstantiator;
    private Instantiator instantiator;
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private Mock mockLogger;
    private Logger logger;
    
    private CoralStoreImpl coralStore;
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        mockCacheFactory = mock(CacheFactory.class);
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();
        mockPersistence = mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockCoralEventHub = mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockPersistentFactory = mock(PersistentFactory.class);
        persistentFactory = (PersistentFactory)mockPersistentFactory.proxy();
        mockInstantiator = mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        mockCoralCore = mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockLogger = mock(Logger.class);
        logger = (Logger)mockLogger.proxy();

        mockCacheFactory.stubs().method("getInstance").will(returnValue(new HashMap()));
        mockCacheFactory.stubs().method("registerForPeriodicExpunge").isVoid();
        mockInstantiator.stubs().method("getPersistentFactory").with(same(ResourceImpl.class))
            .will(returnValue(persistentFactory));
        
        coralStore = new CoralStoreImpl(cacheFactory, persistence, coralEventHub, instantiator,
            coralCore, logger);
    }
    
    public void testInvalidChracters()
    {
        assertTrue(coralStore.isValidResourceName("abc"));
        assertFalse(coralStore.isValidResourceName("a/bc"));
        assertEquals("",coralStore.getInvalidResourceNameCharacters("abc"));
        assertEquals("/",coralStore.getInvalidResourceNameCharacters("a/bc"));
    }    
}
