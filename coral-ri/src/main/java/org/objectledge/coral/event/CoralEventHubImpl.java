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

import org.objectledge.event.EventForwarder;
import org.objectledge.event.EventSystem;
import org.objectledge.event.InboundEventForwarder;
import org.objectledge.event.OutboundEventForwarder;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralEventHubImpl.java,v 1.1 2004-03-01 13:19:25 fil Exp $
 */
public class CoralEventHubImpl
    implements CoralEventHub
{
    private CoralEventWhiteboard inboundEvents;

    private CoralEventWhiteboard outboundEvents;

    private CoralEventWhiteboard localEvents;

    private CoralEventWhiteboard globalEvents;

    /**
     * Creates an event hub.
     * 
     * @param eventWhiteboardFactory the event whiteboard factory.
     * @param bridge the event / notification bridge.
     */    
    public CoralEventHubImpl(EventSystem eventWhiteboardFactory, CoralEventBridge bridge)
    {
        EventForwarder inbound = eventWhiteboardFactory.getForwarder();
        inboundEvents = new CoralEventWhiteboardImpl(new InboundEventForwarder(inbound));
        EventForwarder outbound = eventWhiteboardFactory.getForwarder();
        if(bridge != null)
        {
            bridge.attach(inbound, outbound);
        }
        outboundEvents = new CoralEventWhiteboardImpl(new OutboundEventForwarder(outbound));
        EventForwarder local = eventWhiteboardFactory.getForwarder();
        localEvents = new CoralEventWhiteboardImpl(local);
        globalEvents = new CoralEventRedirector(inboundEvents, localEvents, outboundEvents);
    }    

    /** 
     * {@inheritDoc}
     */
    public CoralEventWhiteboard getInbound()
    {
        return inboundEvents;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralEventWhiteboard getOutbound()
    {
        return outboundEvents;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralEventWhiteboard getLocal()
    {
        return localEvents;
    }
    
    /** 
     * {@inheritDoc}
     */
    public CoralEventWhiteboard getGlobal()
    {
        return globalEvents;
    }
}
