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

import java.util.Date;

import org.jmock.Mock;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AbstractAssignmentTest.java,v 1.4 2004-05-28 10:04:12 fil Exp $
 */
public class AbstractAssignmentTest extends LedgeTestCase
{
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockInputRecord;
    private InputRecord inputRecord;
    private Mock mockOutputRecord;
    private OutputRecord outputRecord;
    private Mock mockCoralSecurity;
    private CoralSecurity coralSecurity;
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private RedBlueEntityFactory factory;    
    private Mock mockSubject;
    private Subject subject;
    private Mock mockOtherSubject;
    private Subject otherSubject;
    
    public void setUp()
    {
        mockPersistence = mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockInputRecord = mock(InputRecord.class);
        inputRecord = (InputRecord)mockInputRecord.proxy();
        mockOutputRecord = mock(OutputRecord.class);
        outputRecord = (OutputRecord)mockOutputRecord.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockCoralCore = mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stubs().method("getSecurity").will(returnValue(coralSecurity));
        factory = new RedBlueEntityFactory(persistence);
        mockSubject = mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        mockOtherSubject = mock(Subject.class, "mockOtherSubject");
        otherSubject = (Subject)mockSubject.proxy();
    }
    
    public void testCreation()
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue2 = factory.getBlue(2);
        RedBlueAssignment as = new RedBlueAssignment(coralCore, 
            persistence, factory, 
            red1, blue2, subject, new Date());
        assertEquals(1, as.getRed().getId());    
        assertEquals(2, as.getBlue().getId());        
    }
    
    public void testHashCode()
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue1 = factory.getBlue(1);
        BlueEntity blue2 = factory.getBlue(2);
        RedBlueAssignment red1blue1 = new RedBlueAssignment(coralCore, persistence, factory, 
            red1, blue1, subject, new Date());
        RedBlueAssignment red1blue2 = new RedBlueAssignment(coralCore, persistence, factory, 
            red1, blue2, subject, new Date());
        RedBlueAssignment red1blue1copy = new RedBlueAssignment(coralCore, persistence, factory, 
            red1, blue1, subject, new Date());
        assertFalse(red1blue1.hashCode() == red1blue2.hashCode());
        assertTrue(red1blue1.hashCode() == red1blue1copy.hashCode());                
    }
    
    public void testEquals()
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue1 = factory.getBlue(1);
        BlueEntity blue2 = factory.getBlue(2);
        RedBlueAssignment red1blue1 = new RedBlueAssignment(coralCore, persistence, factory, 
            red1, blue1, subject, new Date());
        RedBlueAssignment red1blue2 = new RedBlueAssignment(coralCore, persistence, factory, 
            red1, blue2, subject, new Date());
        RedBlueAssignment red1blue1copy = new RedBlueAssignment(coralCore, persistence, factory, 
            red1, blue1, subject, new Date());
        assertFalse(red1blue1.equals(red1blue2));
        assertTrue(red1blue1.equals(red1blue1copy));
        assertNotSame(red1blue1, red1blue1copy);
        assertFalse(red1blue1.equals(null));
    }
    
    public void testSetters()
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue2 = factory.getBlue(2);
        Date date = new Date();
        RedBlueAssignment red1blue1 = new RedBlueAssignment(coralCore, persistence, factory, 
            red1, blue2, subject, date);
        assertSame(subject, red1blue1.getGrantedBy());
        red1blue1.setGrantedBy(otherSubject);        
        assertSame(otherSubject, red1blue1.getGrantedBy());
        assertEquals(date, red1blue1.getGrantTime());
        date = new Date(date.getTime()+1000);
        red1blue1.setGrantTime(date);
        assertEquals(date, red1blue1.getGrantTime());
    }
    
    public void testStoring()
        throws Exception
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue2 = factory.getBlue(2);
        Date date = new Date();
        mockSubject.expects(once()).method("getId").will(returnValue(3L));
        mockOutputRecord.expects(once()).method("setLong").with(eq("red_id"), eq(1L));
        mockOutputRecord.expects(once()).method("setLong").with(eq("blue_id"), eq(2L));
        mockOutputRecord.expects(once()).method("setLong").with(eq("grantor"), eq(3L));
        mockOutputRecord.expects(once()).method("setDate").with(eq("grant_time"), eq(date));                
        RedBlueAssignment red1blue1 = new RedBlueAssignment(coralCore, persistence, factory, 
            red1, blue2, subject, date);
        red1blue1.getData(outputRecord);
        red1blue1.getTable();
        red1blue1.getKeyColumns();
    }
    
    public void testLoading()
        throws Exception
    {
        RedBlueAssignment rb = new RedBlueAssignment(coralCore, persistence, factory);
        Date date = new Date();
        mockInputRecord.expects(once()).method("getLong").with(eq("red_id")).will(returnValue(1L));
        mockInputRecord.expects(once()).method("getLong").with(eq("blue_id")).will(returnValue(2L));
        mockInputRecord.expects(once()).method("getLong").with(eq("grantor")).will(returnValue(3L));
        mockInputRecord.expects(once()).method("getDate").with(eq("grant_time")).will(returnValue(date));                
        mockCoralSecurity.expects(once()).method("getSubject").with(eq(3L)).will(returnValue(subject));
        rb.setData(inputRecord);
        assertEquals(1L, rb.getRed().getId());
        assertEquals(2L, rb.getBlue().getId());
        assertSame(subject, rb.getGrantedBy());
        assertSame(date, rb.getGrantTime());
    }      

    public void testLoadingMissingSubject()
        throws Exception
    {
        RedBlueAssignment rb = new RedBlueAssignment(coralCore, persistence, factory);
        mockInputRecord.expects(once()).method("getLong").with(eq("grantor")).will(returnValue(3L));
        mockCoralSecurity.expects(once()).method("getSubject").with(eq(3L)).will(throwException(new EntityDoesNotExistException("subject not found")));
        try
        {
            rb.setData(inputRecord);
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(PersistenceException.class, e.getClass());
            assertEquals("Failed to load RedBlueAssignment", e.getMessage());
            assertEquals(EntityDoesNotExistException.class, e.getCause().getClass());
            assertEquals("subject not found", e.getCause().getMessage());
        }
    }      
}
