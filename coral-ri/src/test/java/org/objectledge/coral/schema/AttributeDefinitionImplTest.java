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
package org.objectledge.coral.schema;

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeDefinitionImplTest.java,v 1.3 2004-03-09 15:46:46 fil Exp $
 */
public class AttributeDefinitionImplTest extends MockObjectTestCase
{
    private Mock mockPersistence;   
    private Persistence persistence;
    private Mock mockInputRecord;
    private InputRecord inputRecord;
    private Mock mockOutputRecord;
    private OutputRecord outputRecord;
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockAttributeClass;
    private AttributeClass attributeClass;
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    private Mock mockCoralEventHub;
    private CoralEventHub coralEventHub;
    private Mock mockCoralEventWhiteboard;
    private CoralEventWhiteboard coralEventWhiteboard;
    private Mock mockCoralCore;
    private CoralCore coralCore;

    public void setUp()
    {
        mockPersistence = new Mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockInputRecord = new Mock(InputRecord.class);
        inputRecord = (InputRecord)mockInputRecord.proxy();
        mockOutputRecord = new Mock(OutputRecord.class);
        outputRecord = (OutputRecord)mockOutputRecord.proxy();
        mockCoralSchema = new Mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockAttributeClass = new Mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockResourceClass = new Mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        mockCoralEventHub = new Mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockCoralEventWhiteboard = new Mock(CoralEventWhiteboard.class);
        coralEventWhiteboard = (CoralEventWhiteboard)mockCoralEventWhiteboard.proxy();
        mockCoralCore = new Mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stub().method("getSchema").will(returnValue(coralSchema));
    }
    
    public void testCreation()
    {
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore, 
            "<attribute>", attributeClass, "<domain>", 303);
        def.setDeclaringClass(resourceClass);
        assertEquals(-1L, def.getId());
        assertEquals("<attribute>", def.getName());
        assertSame(attributeClass, def.getAttributeClass());
        assertEquals("<domain>", def.getDomain());
        assertEquals(303, def.getFlags());
        assertEquals(resourceClass, def.getDeclaringClass());
    }
    
    public void testStoring()
        throws Exception
    {
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore, 
            "<attribute>", attributeClass, "<domain>", 303);
        def.setDeclaringClass(resourceClass);
        mockOutputRecord.expect(once()).method("setLong").with(eq("attribute_definition_id"), eq(-1L));
        mockOutputRecord.expect(once()).method("setString").with(eq("name"), eq("<attribute>"));
        mockAttributeClass.expect(once()).method("getId").will(returnValue(1L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("attribute_class_id"), eq(1L));
        mockResourceClass.expect(once()).method("getId").will(returnValue(2L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("resource_class_id"), eq(2L));
        mockOutputRecord.expect(once()).method("setString").with(eq("domain"), eq("<domain>"));
        mockOutputRecord.expect(once()).method("setInteger").with(eq("flags"), eq(303));
        def.getData(outputRecord);
        assertEquals("coral_attribute_definition", def.getTable());
    }

    public void testStoringNullDomain()
        throws Exception
    {
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore, 
            "<attribute>", attributeClass, null, 303);
        def.setDeclaringClass(resourceClass);
        mockOutputRecord.expect(once()).method("setLong").with(eq("attribute_definition_id"), eq(-1L));
        mockOutputRecord.expect(once()).method("setString").with(eq("name"), eq("<attribute>"));
        mockAttributeClass.expect(once()).method("getId").will(returnValue(1L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("attribute_class_id"), eq(1L));
        mockResourceClass.expect(once()).method("getId").will(returnValue(2L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("resource_class_id"), eq(2L));
        mockOutputRecord.expect(once()).method("setNull").with(eq("domain"));
        mockOutputRecord.expect(once()).method("setInteger").with(eq("flags"), eq(303));
        def.getData(outputRecord);
        assertEquals("coral_attribute_definition", def.getTable());
    }
    
    public void testLoading()
        throws Exception
    {
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore);

        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_definition_id")).will(returnValue(-1L));
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<attribute>"));
        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_class_id")).will(returnValue(1L));
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).will(returnValue(attributeClass));
        mockInputRecord.expect(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(2L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(2L)).will(returnValue(resourceClass));
        mockInputRecord.expect(once()).method("isNull").with(eq("domain")).will(returnValue(false));
        mockInputRecord.expect(once()).method("getString").with(eq("domain")).will(returnValue("<domain>"));
        mockInputRecord.expect(once()).method("getInteger").with(eq("flags")).will(returnValue(303));
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        def.setData(inputRecord);        
        assertEquals(-1L, def.getId());
        assertEquals("<attribute>", def.getName());
        assertSame(attributeClass, def.getAttributeClass());
        assertEquals("<domain>", def.getDomain());
        assertEquals(303, def.getFlags());
        assertEquals(resourceClass, def.getDeclaringClass());
    }

    public void testLoadingNullDomain()
        throws Exception
    {
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore);

        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_definition_id")).will(returnValue(-1L));
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<attribute>"));
        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_class_id")).will(returnValue(1L));
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).will(returnValue(attributeClass));
        mockInputRecord.expect(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(2L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(2L)).will(returnValue(resourceClass));
        mockInputRecord.expect(once()).method("isNull").with(eq("domain")).will(returnValue(true));
        mockInputRecord.expect(once()).method("getInteger").with(eq("flags")).will(returnValue(303));
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        def.setData(inputRecord);        
        assertEquals(-1L, def.getId());
        assertEquals("<attribute>", def.getName());
        assertSame(attributeClass, def.getAttributeClass());
        assertNull(def.getDomain());
        assertEquals(303, def.getFlags());
        assertEquals(resourceClass, def.getDeclaringClass());
    }

    public void testLoadingMissingAttributeClass()
        throws Exception
    {
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore);

        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_definition_id")).will(returnValue(-1L));
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<attribute>"));
        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_class_id")).will(returnValue(1L));
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).will(throwException(new EntityDoesNotExistException("missing attribute class")));
        try
        {
            def.setData(inputRecord);        
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(PersistenceException.class, e.getClass());
            assertEquals(EntityDoesNotExistException.class, e.getCause().getClass());
            assertEquals("missing attribute class", e.getCause().getMessage());
        }
    }

    public void testLoadingMissingResourceClass()
        throws Exception
    {
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore);

        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_definition_id")).will(returnValue(-1L));
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<attribute>"));
        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_class_id")).will(returnValue(1L));
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).will(returnValue(attributeClass));
        mockInputRecord.expect(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(2L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(2L)).will(throwException(new EntityDoesNotExistException("missing resource class")));
        try
        {
            def.setData(inputRecord);        
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(PersistenceException.class, e.getClass());
            assertEquals(EntityDoesNotExistException.class, e.getCause().getClass());
            assertEquals("missing resource class", e.getCause().getMessage());
        }
    }
    
    public void testAttributeDefinitionChanged()
    {
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore, 
            "<attribute>", attributeClass, "<domain>", 303);
        mockPersistence.expect(once()).method("revert").with(same(def));
        def.attributeDefinitionChanged(def);
    }

    public void testAttributeDefinitionChangedOther()
    {
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore, 
            "<attribute>", attributeClass, "<domain>", 303);
        def.attributeDefinitionChanged(null);
    }

    public void testAttributeDefinitionChangedRevertFailed()
    {
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore, 
            "<attribute>", attributeClass, "<domain>", 303);
        mockPersistence.expect(once()).method("revert").with(same(def)).will(throwException(new PersistenceException("revert failed")));
        try
        {
            def.attributeDefinitionChanged(def);
            fail("exception expected");
        }
        catch(RuntimeException e)
        {
            assertEquals(BackendException.class, e.getClass());
            assertEquals(PersistenceException.class, e.getCause().getClass());
            assertEquals("revert failed", e.getCause().getMessage());
        }
    }
    
    public void testSetters()
    {
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener");
        AttributeDefinitionImpl def = new AttributeDefinitionImpl(persistence, coralEventHub, coralCore, 
            "<attribute>", attributeClass, "<domain>", 303);
        assertEquals(303, def.getFlags());
        def.setFlags(909);
        assertEquals(909, def.getFlags());
        assertEquals("<attribute>", def.getName());
        def.setName("<other attribute>");
        assertEquals("<other attribute>", def.getName());
        assertEquals("<domain>", def.getDomain());
        def.setDomain(null);
        assertNull(def.getDomain());    
    }
}
