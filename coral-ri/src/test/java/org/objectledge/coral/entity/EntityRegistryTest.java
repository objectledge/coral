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
import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.cache.CacheFactory;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.Persistence;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: EntityRegistryTest.java,v 1.1 2004-02-26 13:07:30 fil Exp $
 */
public class EntityRegistryTest
    extends MockObjectTestCase
{
    private Mock mockPersistence;
    private Persistence persistence;
    private MutablePicoContainer dependencies;
    private Mock mockCacheFactory;
    private CacheFactory cacheFactory;
    private Mock mockDatabase;
    private Database database;
    private Mock mockLogger;
    private Logger log;
    private RedEntity redEntity;
    
    public void setUp()
    {
        mockPersistence = new Mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        dependencies = new DefaultPicoContainer();
        dependencies.registerComponentInstance(Persistence.class, persistence);
        mockCacheFactory = new Mock(CacheFactory.class);
        mockCacheFactory.stub().method("getInstance").will(returnValue(new HashMap()));
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();
        mockDatabase = new Mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockLogger = new Mock(Logger.class);
        log = (Logger)mockLogger.proxy();
        redEntity = new RedEntity(persistence);
    }
    
    private EntityRegistry createRegistry()
        throws Exception
    {
        return new EntityRegistry(persistence, cacheFactory, database, dependencies, log, 
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
        mockPersistence.expect(once()).method("load").with(NULL, ANYTHING).will(returnValue(list));        
        assertEquals(0, reg.get().size());
        // no load this time
        reg.get();
    }
    
    public void testGetById()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        mockPersistence.expect(once()).method("load").with(eq(1L), ANYTHING).will(returnValue(redEntity));
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
        mockPersistence.expect(once()).method("load").with(eq("name = 'fred'"), ANYTHING).will(returnValue(list));
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
        mockPersistence.expect(once()).method("load").with(eq("name = 'fred'"), ANYTHING).will(returnValue(list));
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
        mockPersistence.expect(once()).method("save").with(same(redEntity));
        reg.add(redEntity);
    }

    public void testAddUnique()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        redEntity.setId(1L);
        redEntity.setName("fred");
        mockDatabase.expect(once()).method("beginTransaction").will(returnValue(true));
        mockPersistence.expect(once()).method("exists").with(eq(redEntity.getTable()), eq("name = 'fred'")).will(returnValue(false));
        mockPersistence.expect(once()).method("save").with(same(redEntity));
        mockDatabase.expect(once()).method("commitTransaction").with(eq(true)).will(returnValue(true));        
        reg.addUnique(redEntity);
    }
    
    public void testDelete()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        redEntity.setId(1L);
        mockPersistence.expect(once()).method("delete").with(same(redEntity));
        reg.delete(redEntity);
    }
    
    public void testRename()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        mockPersistence.expect(once()).method("save").with(same(redEntity));
        reg.rename(redEntity, "george");
    }

    public void testRenameUnique()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        mockDatabase.expect(once()).method("beginTransaction").will(returnValue(true));
        mockPersistence.expect(once()).method("exists").with(eq(redEntity.getTable()), eq("name = 'george'")).will(returnValue(false));
        mockPersistence.expect(once()).method("save").with(same(redEntity));
        mockDatabase.expect(once()).method("commitTransaction").with(eq(true)).will(returnValue(true));        
        reg.renameUnique(redEntity, "george");
    }
    
    public void testResolve()
        throws Exception
    {
        EntityRegistry reg = createRegistry();
        redEntity.setId(1L);
        redEntity.setName("fred");
        mockPersistence.expect(once()).method("save").with(same(redEntity));
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
