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

import org.apache.commons.pool.KeyedObjectPool;
import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralEventWhiteboardTest.java,v 1.1 2004-03-08 11:34:06 fil Exp $
 */
public class SessionCoralEventWhiteboardTest extends MockObjectTestCase
{
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private Mock mockCoralEventWhiteboard;
    private CoralEventWhiteboard coralEventWhiteboard;
    private Mock mockKeyedObjectPool;
    private KeyedObjectPool keyedObjectPool;
    
    private CoralSessionImpl session;
    private SessionCoralEventWhiteboard sessionCoralEventWhiteboard;

    private Mock mockPermissionAssignmentChangeListener;
    private PermissionAssignmentChangeListener permissionAssignmentChangeListener;
    
    private Object anchor = new Object();
    
    public void setUp()
    {
        mockCoralCore = new Mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralEventWhiteboard = new Mock(CoralEventWhiteboard.class);
        coralEventWhiteboard = (CoralEventWhiteboard)mockCoralEventWhiteboard.proxy();
        mockCoralCore.stub().method("getEventWhiteboard").will(returnValue(coralEventWhiteboard));
        mockKeyedObjectPool = new Mock(KeyedObjectPool.class);
        keyedObjectPool = (KeyedObjectPool)mockKeyedObjectPool.proxy();

        session = new CoralSessionImpl(coralCore, keyedObjectPool);
        session.open(null, null);
        sessionCoralEventWhiteboard = new SessionCoralEventWhiteboard(coralCore, session);
        
        mockPermissionAssignmentChangeListener = new Mock(PermissionAssignmentChangeListener.class);
        permissionAssignmentChangeListener = (PermissionAssignmentChangeListener)mockPermissionAssignmentChangeListener.proxy();
    }
    
    public void testCreation()
    {
    }
    
    public void testAddPermissionAssignmentChangeListener()
    {
        mockCoralEventWhiteboard.expect(once()).method("addPermissionAssignmentChangeListener").with(same(permissionAssignmentChangeListener), same(anchor)).isVoid();
     
        sessionCoralEventWhiteboard.addPermissionAssignmentChangeListener(permissionAssignmentChangeListener, anchor);   
    }

    public void testRemovePermissionAssignmentChangeListener()
    {
        mockCoralEventWhiteboard.expect(once()).method("removePermissionAssignmentChangeListener").with(same(permissionAssignmentChangeListener), same(anchor)).isVoid();
     
        sessionCoralEventWhiteboard.removePermissionAssignmentChangeListener(permissionAssignmentChangeListener, anchor);   
    }
    
    public void testFirePermissionAssignmentChangeEvent()
    {
        try
        {
            sessionCoralEventWhiteboard.firePermissionAssignmentChangeEvent(null, true);
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(UnsupportedOperationException.class, e.getClass());
        }
    }
}
