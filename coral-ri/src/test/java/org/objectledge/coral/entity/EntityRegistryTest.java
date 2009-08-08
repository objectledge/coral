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
package org.objectledge.coral.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.Instantiator;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistentFactory;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: EntityRegistryTest.java,v 1.6 2005-01-28 01:04:08 rafal Exp $
 */
public class EntityRegistryTest
    extends LedgeTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockCacheFactory;
    private CacheFactory cacheFactory;
    private Mock mockInstantiator;
    private Instantiator instantiator;
    private Mock mockRedEntityPersistentFactory;
    private PersistentFactory redEntityPersistentFactory;
    private Mock mockLogger;
    private Logger log;
    private RedEntity redEntity;
    
    public void setUp()
    {
        mockDatabase = mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockPersistence = mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockPersistence.stubs().method("getDatabase").will(returnValue(database));
        mockCacheFactory = mock(CacheFactory.class);
        mockCacheFactory.stubs().method("getInstance").will(returnValue(new HashMap()));
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();
        mockInstantiator = mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        mockRedEntityPersistentFactory = 
            mock(PersistentFactory.class, "mockRedEntityPersistentFactory");
        redEntityPersistentFactory = (PersistentFactory)mockRedEntityPersistentFactory.proxy();
        mockInstantiator.stubs().method("getPersistentFactory").with(eq(RedEntity.class)).
            will(returnValue(redEntityPersistentFactory));
        mockRedEntityPersistentFactory.stubs().method("newInstance").
            will(returnValue(new RedEntity(persistence)));
        mockLogger = mock(Logger.class);
        log = (Logger)mockLogger.proxy();
        redEntity = new RedEntity(persistence);
    }
    
    private EntityRegistry createRegistry()
        throws Exception
    {
        return new EntityRegistry(persistence, cacheFactory, instantiator, log, 
            "redEntity", RedEntity.class);
    }
    
    public void testCreation()
        throws Exception
    {
        createRegistry();
    }
    
    public void testGet()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        List list = new ArrayList(0);
        mockPersistence.expects(once()).method("load").with(NULL, ANYTHING).will(returnValue(list));
        assertEquals(0, reg.get().size());
        // no load this time
        reg.get();
    }
    
    public void testGetById()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        mockPersistence.expects(once()).method("load").with(eq(1L), ANYTHING).will(
            returnValue(redEntity));
        assertSame(redEntity, reg.get(1L));
        // no load this time
        assertSame(redEntity, reg.get(1L));
    }
    
    public void testGetByName()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        List list = new ArrayList(1);
        list.add(redEntity);
        mockPersistence.expects(once()).method("load").with(eq("name = 'fred'"), ANYTHING).will(
            returnValue(list));
        Set result = reg.get("fred");
        assertEquals(1, result.size());
        assertTrue(result.contains(redEntity));        
        // no load this time
        reg.get("fred");    
    }

    public void testGetUnique()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        List list = new ArrayList(1);
        list.add(redEntity);
        mockPersistence.expects(once()).method("load").with(eq("name = 'fred'"), ANYTHING).will(
            returnValue(list));
        assertSame(redEntity, reg.getUnique("fred"));
        // no load this time
        reg.getUnique("fred");
    }
    
    public void testAdd()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        redEntity.setId(1L);
        redEntity.setName("fred");
        mockPersistence.expects(once()).method("save").with(same(redEntity));
        reg.add(redEntity);
    }

    public void testAddUnique()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        redEntity.setId(1L);
        redEntity.setName("fred");
        mockDatabase.expects(once()).method("beginTransaction").will(returnValue(true));
        mockPersistence.expects(once()).method("exists").with(eq(redEntity.getTable()), 
            eq("name = 'fred'")).will(returnValue(false));
        mockPersistence.expects(once()).method("save").with(same(redEntity));
        mockDatabase.expects(once()).method("commitTransaction").with(eq(true)).isVoid();        
        reg.addUnique(redEntity);
    }
    
    public void testDelete()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        redEntity.setId(1L);
        mockPersistence.expects(once()).method("delete").with(same(redEntity));
        reg.delete(redEntity);
    }
    
    public void testRename()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        mockPersistence.expects(once()).method("save").with(same(redEntity));
        reg.rename(redEntity, "george");
    }

    public void testRenameUnique()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        mockDatabase.expects(once()).method("beginTransaction").will(returnValue(true));
        mockPersistence.expects(once()).method("exists").with(eq(redEntity.getTable()), 
            eq("name = 'george'")).will(returnValue(false));
        mockPersistence.expects(once()).method("save").with(same(redEntity));
        mockDatabase.expects(once()).method("commitTransaction").with(eq(true)).isVoid();        
        reg.renameUnique(redEntity, "george");
    }
    
    public void testResolve()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        redEntity.setId(1L);
        redEntity.setName("fred");
        mockPersistence.expects(once()).method("save").with(same(redEntity));
        reg.add(redEntity);
        RedEntity otherRedEntity = new RedEntity(persistence);
        otherRedEntity.setId(1L);
        List in = new ArrayList(1);
        Set out = new HashSet(1);
        in.add(otherRedEntity);
        reg.resolve(in, out);
        assertEquals(1, out.size());
        assertSame(redEntity, out.toArray()[0]); 
        assertNotSame(otherRedEntity, out.toArray()[0]); 
    }
}
