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
package org.objectledge.coral;

import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;

/**
 * A bridge between interdependent Coral componentes.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralCore.java,v 1.3 2004-03-05 14:11:04 fil Exp $
 */
public interface CoralCore
{
    // facilities ///////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns a CoralSchema implementation.
     * 
     * @return a CoralSchema implementation.
     */
    public CoralSchema getSchema();
    
    /**
     * Returns a CoralSecurity implementation.
     * 
     * @return a CoralSecurity implementation.
     */
    public CoralSecurity getSecurity();
    
    /**
     * Returns a CoralStore implementation.
     * 
     * @return a CoralStore implementation.
     */
    public CoralStore getStore();
    
    /**
     * Returns a CoralRegistry implementation.
     * 
     * @return a CoralRegistry implementation.
     */
    public CoralRegistry getRegistry();
    
    /**
     * Returns a CoralEventWhiteboard implementation.
     * 
     * @return a CoralEventWhiteboard implementation.
     */
    public CoralEventWhiteboard getEventWhiteboard();
    
    /**
     * Returns a CoralQuery implemenation.
     * 
     * @return a CoralQuery implementaion.
     */
    public CoralQuery getQuery(); 
    
    // session //////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Associates a session with the calling thread.
     *
     * @param session the session to associate, may be <code>null</code>.
     * @return the previously associated session, may be <code>null</code>.
     */
    public CoralSession setCurrentSession(CoralSession session);
    
    /**
     * Returns the session associated with the calling thread.
     * 
     * @return the associated session, may be <code>null</code>.
     */
    public CoralSession getCurrentSession();
}
