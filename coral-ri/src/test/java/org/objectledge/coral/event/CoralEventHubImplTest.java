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

import java.lang.reflect.Method;

import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.jmock.Mock;
import org.objectledge.context.Context;
import org.objectledge.coral.security.PermissionAssociation;
import org.objectledge.event.DelegatingEventWhiteboard;
import org.objectledge.event.EventWhiteboard;
import org.objectledge.event.EventWhiteboardFactory;
import org.objectledge.threads.DefaultThreadPool;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralEventHubImplTest.java,v 1.7 2008-01-01 23:32:43 rafal Exp $
 */
public class CoralEventHubImplTest
    extends CoralEventTestCase
{
    private CoralEventHub coralEventHub;
    
    private DefaultThreadPool threadPool;
    
    private EventWhiteboard inboundBridge;
    
    private EventWhiteboard outboundBridge;
    
    private Method permissionAssociationsChanged;

    private Mock mockLocalPermissionAssociationChangeListener;
    private PermissionAssociationChangeListener localPermissionAssociationChangeListener; 
    private Mock mockGlobalPermissionAssociationChangeListener;
    private PermissionAssociationChangeListener globalPermissionAssociationChangeListener; 
    private Mock mockOutboundPermissionAssociationChangeListener;
    private PermissionAssociationChangeListener outboundPermissionAssociationChangeListener; 
    private Mock mockInboundPermissionAssociationChangeListener;
    private PermissionAssociationChangeListener inboundPermissionAssociationChangeListener; 
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        Logger log = new Log4JLogger(org.apache.log4j.Logger.getLogger(getClass()));
        threadPool = new DefaultThreadPool(null, new Context(), null, log);
        
        EventWhiteboardFactory eventWhiteboardFactory = new EventWhiteboardFactory(null, log,
             threadPool, cacheFactory);
        PassthroughEventBridge passthroughEventBridge = new PassthroughEventBridge();
        inboundBridge = passthroughEventBridge.getInbound();
        outboundBridge = passthroughEventBridge.getOutbound();
        coralEventHub = new CoralEventHubImpl(eventWhiteboardFactory, passthroughEventBridge);

        mockLocalPermissionAssociationChangeListener = 
            mock(PermissionAssociationChangeListener.class, 
                "localPermissionAssociationChangeListener");
        localPermissionAssociationChangeListener = 
            (PermissionAssociationChangeListener)mockLocalPermissionAssociationChangeListener.
                proxy(); 
        mockGlobalPermissionAssociationChangeListener = 
            mock(PermissionAssociationChangeListener.class, 
                "globalPermissionAssociationChangeListener");
        globalPermissionAssociationChangeListener = 
            (PermissionAssociationChangeListener)mockGlobalPermissionAssociationChangeListener.
                proxy(); 
        mockOutboundPermissionAssociationChangeListener = 
            mock(PermissionAssociationChangeListener.class, 
                "outboundPermissionAssociationChangeListener");
        outboundPermissionAssociationChangeListener = 
            (PermissionAssociationChangeListener)mockOutboundPermissionAssociationChangeListener.
                proxy(); 
        mockInboundPermissionAssociationChangeListener = 
            mock(PermissionAssociationChangeListener.class, 
                "inboundPermissionAssociationChangeListener");
        inboundPermissionAssociationChangeListener = 
            (PermissionAssociationChangeListener)mockInboundPermissionAssociationChangeListener.
                proxy(); 

        coralEventHub.getLocal().addPermissionAssociationChangeListener(
            localPermissionAssociationChangeListener, permission);
        coralEventHub.getGlobal().addPermissionAssociationChangeListener(
            globalPermissionAssociationChangeListener, permission);
        coralEventHub.getInbound().addPermissionAssociationChangeListener(
            localPermissionAssociationChangeListener, permission);
        outboundBridge.addListener(PermissionAssociationChangeListener.class,
            outboundPermissionAssociationChangeListener, permission);

        mockPermissionAssociation.stubs().method("getResourceClass").will(
            returnValue(resourceClass));
        mockPermissionAssociation.stubs().method("getPermission").will(returnValue(permission));
        

        permissionAssociationsChanged = PermissionAssociationChangeListener.class
            .getDeclaredMethod("permissionsChanged", new Class[] { PermissionAssociation.class,
                            Boolean.TYPE });

    }
    
    public void tearDown()
    {
        threadPool.stop();
    }
    
    private class PassthroughEventBridge
        implements CoralEventBridge
    {
        private DelegatingEventWhiteboard in;
        
        private DelegatingEventWhiteboard out;
        
        /**
         * Creates a passthrough bridge;
         */
        public PassthroughEventBridge()
        {
            in = new DelegatingEventWhiteboard(null);
            out = new DelegatingEventWhiteboard(null);
        }

        /**
         * Returns the inbound forwarder.
         * 
         * @return the inbound forwarder.
         */        
        public EventWhiteboard getInbound()
        {
            return in;
        }
        
        /**
         * Returns the outbound forwarder.
         * 
         * @return the outbound forwarder.
         */        
        public EventWhiteboard getOutbound()
        {
            return out;
        }
        
        /** 
         * {@inheritDoc}
         */
        public void attach(EventWhiteboard in, EventWhiteboard out)
        {
            this.in.swap(in);
            this.out.swap(out);
        }

        /** 
         * {@inheritDoc}
         */
        public void detach()
        {
            in.swap(null);
            out.swap(null);
        }
    }
    
    // tests ////////////////////////////////////////////////////////////////////////////////////
    
    public void testInbound()
    {
        mockLocalPermissionAssociationChangeListener.expects(once()).method("permissionsChanged")
            .with(same(permissionAssociation), eq(true));
        mockGlobalPermissionAssociationChangeListener.expects(once()).method("permissionsChanged")
            .with(same(permissionAssociation), eq(true));

        inboundBridge.fireEvent(permissionAssociationsChanged, new Object[] {
                        permissionAssociation, Boolean.TRUE }, permission);
        // inboundBridge -> eventHub.inbound, eventHub.global
    }
    
    public void testLocal()
    {
        mockLocalPermissionAssociationChangeListener.expects(once()).method("permissionsChanged")
            .with(same(permissionAssociation), eq(true));
        mockGlobalPermissionAssociationChangeListener.expects(once()).method("permissionsChanged")
            .with(same(permissionAssociation), eq(true));

        coralEventHub.getLocal().firePermissionAssociationChangeEvent(permissionAssociation, true);
        // eventHub.local -> eventHub.local, eventHub.global       
    }
    
    public void testGlobal()
    {
        mockLocalPermissionAssociationChangeListener.expects(once()).method("permissionsChanged")
            .with(same(permissionAssociation), eq(true));
        mockGlobalPermissionAssociationChangeListener.expects(once()).method("permissionsChanged")
            .with(same(permissionAssociation), eq(true));
        mockOutboundPermissionAssociationChangeListener.expects(once())
            .method("permissionsChanged").with(same(permissionAssociation), eq(true));

        coralEventHub.getGlobal().firePermissionAssociationChangeEvent(permissionAssociation, true);
        // eventHub.global -> eventHub.local, eventHub.global, outboundBridge               
    }
}
