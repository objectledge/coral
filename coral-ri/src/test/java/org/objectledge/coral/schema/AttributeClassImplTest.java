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

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.InstantiationException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.event.AttributeClassChangeListener;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.utils.LedgeTestCase;
import org.objectledge.utils.MapElement;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeClassImplTest.java,v 1.6 2004-03-24 14:40:10 fil Exp $
 */
public class AttributeClassImplTest extends LedgeTestCase
{
    private Mock mockPersistence;
   
    private Mock mockInputRecord;
    
    private Mock mockOutputRecord;
    
    private Mock mockInstantiator;
    
    private Mock mockCoralEventHub;
    
    private Mock mockAttributeHandler;
    
    private Mock mockCoralEventWhiteboard;

    public void setUp()
    {
        mockPersistence = mock(Persistence.class);
        mockInstantiator = mock(Instantiator.class);
        mockCoralEventHub = mock(CoralEventHub.class); 
        mockAttributeHandler = mock(AttributeHandler.class);
        mockInputRecord = mock(InputRecord.class);
        mockOutputRecord = mock(OutputRecord.class);
        mockCoralEventWhiteboard = mock(CoralEventWhiteboard.class);
    }
    
    public void testCreation()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockAttributeHandler.expect(once()).method("getComparator").will(returnValue(null));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(mockAttributeHandler.proxy()));
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(mockCoralEventWhiteboard.proxy()));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeClassChangeListener");
        AttributeClass ac = new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
            (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy(), 
            "<class name>", "<java class>", "<handler class>", "<db table>");
        assertEquals(-1L, ac.getId());
        assertEquals("<class name>", ac.getName());
        assertEquals("<db table>", ac.getDbTable());
        assertEquals(Object.class, ac.getJavaClass());
        ac.getHandler().getComparator();
    }
    
    public void testMissingJavaClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(throwException(new ClassNotFoundException("<java class>")));
        try
        {
            new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
                (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy(), 
                "<class name>", "<java class>", "<handler class>", "<db table>");
            fail("should throw an exception");        
        }
        catch(Exception e)
        {
            assertEquals(JavaClassException.class, e.getClass());
            assertEquals(ClassNotFoundException.class, e.getCause().getClass());
            assertEquals("<java class>", e.getMessage());
        }
    }

    public void testMissingHandlerClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(throwException(new ClassNotFoundException("<handler class>")));
        try
        {
            new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
                (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy(), 
                "<class name>", "<java class>", "<handler class>", "<db table>");
            fail("should throw an exception");        
        }
        catch(Exception e)
        {
            assertEquals(JavaClassException.class, e.getClass());
            assertEquals(ClassNotFoundException.class, e.getCause().getClass());
            assertEquals("<handler class>", e.getMessage());
        }
    }

    public void testUninstantiableHandlerClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(throwException(new InstantiationException("<handler class>", new Exception("unavailable"))));
        try
        {
            new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
                (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy(), 
                "<class name>", "<java class>", "<handler class>", "<db table>");
            fail("should throw an exception");        
        }
        catch(Exception e)
        {
            assertEquals(JavaClassException.class, e.getClass());
            assertEquals(Exception.class, e.getCause().getClass());
            assertEquals("unavailable", e.getCause().getMessage());
        }
    }
    
    public void testNotImplementingHandlerClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(new Object()));
        try
        {        
            new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
                (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy(), 
                "<class name>", "<java class>", "<handler class>", "<db table>");
        }
        catch(Exception e)
        {
            assertEquals(JavaClassException.class, e.getClass());
            assertEquals(ClassCastException.class, e.getCause().getClass());
            assertEquals("<handler class> does not implement AttributeHandler interface", e.getMessage());
        }
    }
    
    public void testStoring()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(mockAttributeHandler.proxy()));
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(mockCoralEventWhiteboard.proxy()));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeClassChangeListener");
        AttributeClass ac = new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
            (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy(), 
            "<class name>", "<java class>", "<handler class>", "<db table>");
        Persistent p = (Persistent)ac;    
        assertEquals("coral_attribute_class", p.getTable());
        mockOutputRecord.expect(once()).method("setLong").with(eq("attribute_class_id"), eq(-1L));
        mockOutputRecord.expect(once()).method("setString").with(eq("name"), eq("<class name>"));        
        mockOutputRecord.expect(once()).method("setString").with(eq("db_table_name"), eq("<db table>"));
        mockOutputRecord.expect(once()).method("setString").with(eq("java_class_name"), eq("<java class>"));
        mockOutputRecord.expect(once()).method("setString").with(eq("handler_class_name"), eq("<handler class>"));
        p.getData((OutputRecord)mockOutputRecord.proxy());
    }
    
    public void testLoading()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockAttributeHandler.expect(once()).method("getComparator").will(returnValue(null));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(mockAttributeHandler.proxy()));
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(mockCoralEventWhiteboard.proxy()));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeClassChangeListener");
        AttributeClass ac = new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
            (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy()); 
        Persistent p = (Persistent)ac;
        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_class_id")).will(returnValue(-1L));   
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<class name>"));        
        mockInputRecord.expect(once()).method("getString").with(eq("db_table_name")).will(returnValue("<db table>"));
        mockInputRecord.expect(once()).method("getString").with(eq("java_class_name")).will(returnValue("<java class>"));
        mockInputRecord.expect(once()).method("getString").with(eq("handler_class_name")).will(returnValue("<handler class>"));
        p.setData((InputRecord)mockInputRecord.proxy());
        assertEquals(-1L, ac.getId());
        assertEquals("<class name>", ac.getName());
        assertEquals("<db table>", ac.getDbTable());
        assertEquals(Object.class, ac.getJavaClass());
        ac.getHandler().getComparator();
    }

    public void testLoadingJavaClassException()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(throwException(new ClassNotFoundException("<java class>")));
        AttributeClass ac = new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
            (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy()); 
        Persistent p = (Persistent)ac;
        mockInputRecord.expect(once()).method("getLong").with(eq("attribute_class_id")).will(returnValue(-1L));   
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<class name>"));        
        mockInputRecord.expect(once()).method("getString").with(eq("db_table_name")).will(returnValue("<db table>"));
        mockInputRecord.expect(once()).method("getString").with(eq("java_class_name")).will(returnValue("<java class>"));
        try
        {
            p.setData((InputRecord)mockInputRecord.proxy());
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(BackendException.class, e.getClass());
            assertEquals(JavaClassException.class, e.getCause().getClass());
            assertEquals(ClassNotFoundException.class, e.getCause().getCause().getClass());
            assertEquals("<java class>", e.getCause().getMessage());
        }
    }
    
    public void testAttributeClassChanged()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")). will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(mockAttributeHandler.proxy()));
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(mockCoralEventWhiteboard.proxy()));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeClassChangeListener");
        AttributeClass ac = new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
            (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy(), 
            "<class name>", "<java class>", "<handler class>", "<db table>");
        mockPersistence.expect(once()).method("revert").with(eq(ac));
        AttributeClassChangeListener listener = (AttributeClassChangeListener)ac;
        listener.attributeClassChanged(ac);
        listener.attributeClassChanged(null);
    }

    public void testAttributeClassChangedFailedRevert()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")). will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(mockAttributeHandler.proxy()));
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(mockCoralEventWhiteboard.proxy()));
        mockCoralEventWhiteboard.expect(once()).method("addAttributeClassChangeListener");
        AttributeClass ac = new AttributeClassImpl((Persistence)mockPersistence.proxy(), 
            (Instantiator)mockInstantiator.proxy(), (CoralEventHub)mockCoralEventHub.proxy(), 
            "<class name>", "<java class>", "<handler class>", "<db table>");
        mockPersistence.expect(once()).method("revert").with(eq(ac)).will(throwException(new PersistenceException("revert failed")));
        AttributeClassChangeListener listener = (AttributeClassChangeListener)ac;
        try
        {
            listener.attributeClassChanged(ac);
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(BackendException.class, e.getClass());
            assertEquals(PersistenceException.class, e.getCause().getClass());
            assertEquals("revert failed", e.getCause().getMessage());
        }            
    }
}
