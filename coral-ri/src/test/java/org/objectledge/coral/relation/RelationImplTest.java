//
// Copyright (c) 2003, 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.
// nor the names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
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
package org.objectledge.coral.relation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jmock.Mock;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityFactory;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.test.LedgeTestCase;

import bak.pcj.set.LongSet;

/**
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationImplTest.java,v 1.16 2009-01-30 13:43:56 rafal Exp $
 */
public class RelationImplTest extends LedgeTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockPersistence;
    private Persistence persistence;

    private CoralStore coralStore;

    private Mock mockInputRecord;
    private InputRecord inputRecord;
    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private ResultSet resultSet;

    private RelationImpl relation;

	private Mock mockResource1 = mock(Resource.class);
	private Resource resource1 = (Resource) mockResource1.proxy();
	private Mock mockResource2 = mock(Resource.class);
	private Resource resource2 = (Resource) mockResource2.proxy();
    
    private Mock mockCoralRelationManager = mock(CoralRelationManager.class);
    private CoralRelationManager coralRelationManager = 
        (CoralRelationManager)mockCoralRelationManager.proxy();

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RelationImplTest.class);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        coralStore = new MockCoralStore();

        mockInputRecord = mock(InputRecord.class);
        mockInputRecord.stubs().method("getLong").will(returnValue(10L));
        mockInputRecord.stubs().method("getString").will(returnValue("relation name2"));
        inputRecord = (InputRecord)mockInputRecord.proxy();

        mockStatement = mock(Statement.class);
        statement = (Statement)mockStatement.proxy();

        mockConnection = mock(Connection.class);
        mockConnection.stubs().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();

        mockDatabase = mock(Database.class);
        mockDatabase.stubs().method("getNextId").will(returnValue(1L));
        mockDatabase.stubs().method("getConnection").will(returnValue(connection));
        database = (Database)mockDatabase.proxy();

        relation = new RelationImpl(persistence, coralStore, coralRelationManager, "relation name");
    }


    public void testRelationImpl()
    {
        assertEquals(relation.getId(), -1L);
        assertEquals(relation.getName(), "relation name");

        mockCoralRelationManager.expects(once()).method("getRelationDefinition").with(eq(relation)).will(returnValue(RELATION_DEFINITION));
        try
        {
            relation.setData(inputRecord);
        }
        catch (PersistenceException e)
        {
            fail("Peristence exception occured");
        }

		assertEquals(relation.getId(), 10L);
		assertEquals(relation.getName(), "relation name2");

		assertFalse(relation.isInverted());
		
		LongSet relatedTo1 = relation.get(1L);
		assertEquals(relatedTo1.size(), 3);
		assertTrue(relatedTo1.contains(4L));
		assertTrue(relatedTo1.contains(5L));
		assertTrue(relatedTo1.contains(6L));

		LongSet relatedTo100 = relation.get(100L);
		assertEquals(relatedTo100.size(), 0);

		assertTrue(relation.hasRef(1L, 4L));
		assertTrue(relation.hasRef(1L, 5L));
		assertTrue(relation.hasRef(1L, 6L));
		assertTrue(relation.hasRef(2L, 5L));
		assertTrue(relation.hasRef(2L, 6L));
		assertTrue(relation.hasRef(3L, 6L));
		
		assertFalse(relation.hasRef(1L, 1L));
		assertFalse(relation.hasRef(8L, 1L));
		
		assertEquals("avg mapping size", relation.getAvgMappingSize(), 2F, 0.5F);

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(new Long(1L)));
		Resource[] resRelatedTo1 = relation.get(resource1);
		assertEquals(resRelatedTo1.length, 3);
		if(resRelatedTo1[0].getId() == 4L)
		{
			if(resRelatedTo1[1].getId() == 5L)
            {
                assertEquals(resRelatedTo1[2].getId(), 6L);
            }
			if(resRelatedTo1[2].getId() == 5L)
            {
                assertEquals(resRelatedTo1[1].getId(), 6L);
            }
		} 
		if(resRelatedTo1[0].getId() == 5L)
		{
			if(resRelatedTo1[1].getId() == 4L) 
            {
                assertEquals(resRelatedTo1[2].getId(), 6L);
            }
			if(resRelatedTo1[2].getId() == 4L) 
            {
                assertEquals(resRelatedTo1[1].getId(), 6L);
            }
		} 
		if(resRelatedTo1[0].getId() == 6L)
		{
			if(resRelatedTo1[1].getId() == 4L)
            {
                assertEquals(resRelatedTo1[2].getId(), 5L);
            }
			if(resRelatedTo1[2].getId() == 4L) 
            {
                assertEquals(resRelatedTo1[1].getId(), 5L);
            }
		} 

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(new Long(100L)));
		Resource[] resRelatedTo100 = relation.get(resource1);
		assertEquals(resRelatedTo100.length, 0);

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(1L));
		mockResource2.expects(atLeastOnce()).method("getId").will(returnValue(4L));
		assertTrue(relation.hasRef(resource1, resource2));

		assertFalse(relation.hasRef(resource1, resource1));
		//mockResource1.expects(atLeastOnce()).method("getIdObject").will(returnValue(new Long(8L)));
		assertFalse(relation.hasRef(resource1, resource1));

		// inverted
		Relation invRelation = relation.getInverted();

		assertEquals(relation.getInverted(), invRelation);
		assertEquals(invRelation.getInverted(), relation);

		assertEquals(invRelation.getId(), 10L);
		assertEquals(invRelation.getName(), "relation name2");

		assertTrue(invRelation.isInverted());

		LongSet relatedTo6 = invRelation.get(6L);
		assertEquals(relatedTo6.size(), 3);
		assertTrue(relatedTo6.contains(1L));
		assertTrue(relatedTo6.contains(2L));
		assertTrue(relatedTo6.contains(3L));

		relatedTo100 = invRelation.get(100L);
		assertEquals(relatedTo100.size(), 0);

		assertTrue(invRelation.hasRef(6L, 1L));
		assertTrue(invRelation.hasRef(6L, 2L));
		assertTrue(invRelation.hasRef(6L, 3L));
		assertTrue(invRelation.hasRef(5L, 1L));
		assertTrue(invRelation.hasRef(5L, 2L));
		assertTrue(invRelation.hasRef(4L, 1L));

		assertFalse(invRelation.hasRef(6L, 6L));
		assertFalse(invRelation.hasRef(1L, 1L));

		assertEquals("avg mapping size", invRelation.getAvgMappingSize(), 2F, 0.5F);

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(new Long(6L)));
		Resource[] resRelatedTo6 = invRelation.get(resource1);
		assertEquals(resRelatedTo6.length, 3);
		if(resRelatedTo6[0].getId() == 1L)
		{
			if(resRelatedTo6[1].getId() == 2L) 
            {
                assertEquals(resRelatedTo6[2].getId(), 3L);
            }
			if(resRelatedTo6[2].getId() == 2L)
            {
                assertEquals(resRelatedTo6[1].getId(), 3L);
            }
		} 
		if(resRelatedTo6[0].getId() == 2L)
        {
            if(resRelatedTo6[1].getId() == 1L)
            {
                assertEquals(resRelatedTo6[2].getId(), 3L);
            }
            if(resRelatedTo6[2].getId() == 1L)
            {
                assertEquals(resRelatedTo6[1].getId(), 3L);
            }
        }
        if(resRelatedTo6[0].getId() == 3L)
        {
            if(resRelatedTo6[1].getId() == 2L)
            {
                assertEquals(resRelatedTo6[2].getId(), 1L);
            }
            if(resRelatedTo6[2].getId() == 2L)
            {
                assertEquals(resRelatedTo6[1].getId(), 1L);
            }
        } 

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(new Long(100L)));
		resRelatedTo100 = invRelation.get(resource1);
		assertEquals(resRelatedTo100.length, 0);

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(4L));
		mockResource2.expects(atLeastOnce()).method("getId").will(returnValue(1L));
		assertTrue(invRelation.hasRef(resource1, resource2));

		assertFalse(invRelation.hasRef(resource1, resource1));
		//mockResource1.expects(atLeastOnce()).method("getIdObject").will(returnValue(new Long(8L)));
		assertFalse(invRelation.hasRef(resource1, resource1));
		
		// modifications --------------------------------------------------------------------------
		
		// add
		// add to one that has rels
		relation.add(1L, 8L);
		assertTrue(relation.hasRef(1L, 8L));
		assertTrue(relation.getInverted().hasRef(8L, 1L));

		assertEquals("avg mapping size", relation.getAvgMappingSize(), 7F/3F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 7F/4F, 0.01F);

		// add non existant rel
		relation.add(5L, 8L);
		assertTrue(relation.hasRef(5L, 8L));
		assertTrue(relation.getInverted().hasRef(8L, 5L));

		assertEquals("avg mapping size", relation.getAvgMappingSize(), 8F/4F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 8F/4F, 0.01F);

		// add already existant rel
		relation.add(5L, 8L);
		assertTrue(relation.hasRef(5L, 8L));
		assertTrue(relation.getInverted().hasRef(8L, 5L));

		assertEquals("avg mapping size", relation.getAvgMappingSize(), 8F/4F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 8F/4F, 0.01F);

		// add non existant rel
		relation.add(9L, 11L);
		assertTrue(relation.hasRef(9L, 11L));
		assertTrue(relation.getInverted().hasRef(11L, 9L));

		assertEquals("avg mapping size", relation.getAvgMappingSize(), 9F/5F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 9F/5F, 0.01F);

		assertFalse(relation.hasRef(9L, 1L));
		assertFalse(relation.getInverted().hasRef(1L, 9L));

		//remove
		// remove from one that has many rels
		relation.remove(1L, 8L);
		assertFalse(relation.hasRef(1L, 8L));
		assertFalse(relation.getInverted().hasRef(8L, 1L));

		assertEquals("avg mapping size", relation.getAvgMappingSize(), 8F/5F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 8F/5F, 0.01F);

		// remove one to one rel
		relation.remove(3L, 6L);
		assertFalse(relation.hasRef(3L, 6L));
		assertFalse(relation.getInverted().hasRef(6L, 3L));

		assertEquals("avg mapping size", relation.getAvgMappingSize(), 7F/4F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 7F/5F, 0.01F);

		// remove not existing rel
		assertFalse(relation.hasRef(15L, 12L));
		assertFalse(relation.getInverted().hasRef(12L, 15L));
		relation.remove(15L, 12L);
		assertFalse(relation.hasRef(15L, 12L));
		assertFalse(relation.getInverted().hasRef(12L, 15L));

		assertEquals("avg mapping size", relation.getAvgMappingSize(), 7F/4F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 7F/5F, 0.01F);

		// remove one to one rel
		relation.remove(9L, 11L);
		assertFalse(relation.hasRef(9L, 11L));
		assertFalse(relation.getInverted().hasRef(11L, 9L));

		assertEquals("avg mapping size", relation.getAvgMappingSize(), 6F/3F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 6F/4F, 0.01F);
		
		// clear
		relation.clear();
		assertEquals("avg mapping size", relation.getAvgMappingSize(), 0F, 0.01F);
		assertEquals("avg mapping size", relation.getInverted().getAvgMappingSize(), 0F, 0.01F);
		assertFalse(relation.hasRef(1L, 4L));
		assertFalse(relation.getInverted().hasRef(4L, 1L));
    }

    private static final long[] RELATION_DEFINITION = {
        1L, 4L, 1L, 5L, 1L, 6L,
        2L, 5L, 2L, 6L,
        3L, 6L
    };
    
    
    private class MockCoralStore implements CoralStore
    {
    	private Map resById = new HashMap();
    	
		/**
		 * {@inheritDoc}
		 */
		public Resource getResource(long id) throws EntityDoesNotExistException
		{
			Long idKey = new Long(id);
			Resource resource = (Resource) resById.get(idKey);
			if(resource == null)
			{
				resource = new MockResource(id);
				resById.put(idKey, resource);
			}
			return resource;
		}

		// uimplemented methods -------------------------------------------------------------------

        /**
         * {@inheritDoc}
         */
        public Resource[] getResource()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResource(Resource parent)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResource(String name)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getUniqueResource(String name) throws IllegalStateException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResource(Resource parent, String name)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getUniqueResource(Resource parent, String name) throws IllegalStateException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResourceByPath(String path)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getUniqueResourceByPath(String path) throws EntityDoesNotExistException, AmbigousEntityNameException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResourceByPath(Resource start, String path)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getUniqueResourceByPath(Resource start, String path) throws EntityDoesNotExistException, AmbigousEntityNameException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource createResource(String name, Resource parent, ResourceClass resourceClass, Map attributes) throws UnknownAttributeException, ValueRequiredException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void deleteResource(Resource resource) throws EntityInUseException, IllegalArgumentException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int deleteTree(Resource res) throws EntityInUseException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setName(Resource resource, String name)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setParent(Resource child, Resource parent) throws CircularDependencyException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void unsetParent(Resource child)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setOwner(Resource resource, Subject owner)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource copyResource(Resource source, Resource destinationParent, String destinationName)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void copyTree(Resource sourceRoot, Resource destinationParent, String destinationName)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isAncestor(Resource ancestor, Resource descendant)
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isValidResourceName(String name)
        {
            throw new UnsupportedOperationException();
        }
        
        /**
         * {@inheritDoc}
         */
        public String getInvalidResourceNameCharacters(String name)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public EntityFactory<Resource> getResourceFactory()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    private class MockResource implements Resource
    {
    	private long id;
    	
    	public MockResource(long id)
    	{
    		this.id = id;
    	}
    	
		/**
		 * {@inheritDoc}
		 */
		public long getId()
		{
			return id;
		}

        /**
         * Returns the numerical identifier of the entity as a Java object.
         * 
         * @return the numerical identifier of the entity as a Java object.
         */
        public Long getIdObject()
        {
            return new Long(id);
        }

        /**
         * Returns the numerical identifier of the entity as a string.
         * 
         * @return the numerical identifier of the entity as a string.
         */
        public String getIdString()
        {
            return Long.toString(id);
        }
        
		// uimplemented methods -------------------------------------------------------------------

        /**
         * {@inheritDoc}
         */
        public String getPath()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public ResourceClass getResourceClass()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Subject getCreatedBy()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public java.util.Date getCreationTime()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Subject getModifiedBy()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public java.util.Date getModificationTime()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Subject getOwner()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public PermissionAssignment[] getPermissionAssignments()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public PermissionAssignment[] getPermissionAssignments(Role role)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getParent()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public long getParentId()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getChildren()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isDefined(AttributeDefinition attribute) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Object get(AttributeDefinition attribute) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }
        
        /**
         * {@inheritDoc}
         */
        public Object get(AttributeDefinition attribute, Object defaultValue) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void set(AttributeDefinition attribute, Object value) throws UnknownAttributeException, ModificationNotPermitedException, ValueRequiredException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void unset(AttributeDefinition attribute) throws ValueRequiredException, UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setModified(AttributeDefinition attribute) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isModified(AttributeDefinition attribute) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void update()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void revert()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getDelegate()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getName()
        {
			throw new UnsupportedOperationException();
        }
    }
}
