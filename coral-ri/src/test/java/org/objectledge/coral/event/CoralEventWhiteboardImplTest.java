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
package org.objectledge.coral.event;

import org.apache.log4j.BasicConfigurator;
import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.jmock.builder.Mock;
import org.objectledge.context.Context;
import org.objectledge.event.EventForwarder;
import org.objectledge.event.EventSystem;
import org.objectledge.threads.ThreadPool;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralEventWhiteboardImplTest.java,v 1.1 2004-03-01 11:41:26 fil Exp $
 */
public class CoralEventWhiteboardImplTest extends CoralEventTestCase
{
    private Mock mockEventWhiteboard;
    private EventForwarder eventWhiteboard;
    private CoralEventWhiteboard coralEventWhiteboard;
    private Object anchor = new Object();
    private ThreadPool threadPool;
    private CoralEventWhiteboard realCoralEventWhiteboard;
    
    public void setUp()
    {
        super.setUp();
        mockEventWhiteboard = new Mock(EventForwarder.class);
        eventWhiteboard = (EventForwarder)mockEventWhiteboard.proxy();
        coralEventWhiteboard = new CoralEventWhiteboardImpl(eventWhiteboard);

        Context context = new Context();
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger logger = new Log4JLogger(org.apache.log4j.Logger.getLogger(getClass()));
        threadPool = new ThreadPool(null, context, null, logger);
        EventSystem eventWhiteboardFactory = new EventSystem(null, logger, threadPool);
        EventForwarder realEventWhiteboard = eventWhiteboardFactory.getForwarder();
        realCoralEventWhiteboard = new CoralEventWhiteboardImpl(realEventWhiteboard);
    }
    
    public void tearDown()
    {
        threadPool.stop();
    }
    
    public void testCreation()
    {
    }
    
    public void testAddPermissionAssociationChangeListener()
    {
        mockEventWhiteboard.expect(once()).method("addListener").with(eq(PermissionAssociationChangeListener.class), 
            same(permissionAssoicationChangeListener), same(anchor));
        coralEventWhiteboard.addPermissionAssociationChangeListener(permissionAssoicationChangeListener, anchor);
    }

    public void testRemovePermissionAssociationChangeListener()
    {
        mockEventWhiteboard.expect(once()).method("removeListener").with(eq(PermissionAssociationChangeListener.class), 
            same(permissionAssoicationChangeListener), same(anchor));
        coralEventWhiteboard.removePermissionAssociationChangeListener(permissionAssoicationChangeListener, anchor);
    }
    
    public void testFirePermissionAssociationChangeListener()
    {
        realCoralEventWhiteboard.addPermissionAssociationChangeListener(permissionAssoicationChangeListener, permission);
        realCoralEventWhiteboard.addPermissionAssociationChangeListener(permissionAssoicationChangeListener, resourceClass);
        realCoralEventWhiteboard.addPermissionAssociationChangeListener(permissionAssoicationChangeListener, null);
        mockPermissionAssociation.expect(once()).method("getResourceClass").will(returnValue(resourceClass));
        mockPermissionAssociation.expect(once()).method("getPermission").will(returnValue(permission));
        mockPermissionAssociationChangeListener.expect(once()).method("permissionsChanged").with(same(permissionAssociation), eq(true));
        mockPermissionAssociationChangeListener.expect(once()).method("permissionsChanged").with(same(permissionAssociation), eq(true));
        mockPermissionAssociationChangeListener.expect(once()).method("permissionsChanged").with(same(permissionAssociation), eq(true));
        realCoralEventWhiteboard.firePermissionAssociationChangeEvent(permissionAssociation, true);
    }
}