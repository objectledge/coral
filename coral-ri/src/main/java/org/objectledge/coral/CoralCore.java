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

import java.util.List;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.CoralRelationQuery;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.script.parser.RMLParserFactory;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;

/**
 * A bridge between interdependent Coral componentes.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralCore.java,v 1.10 2008-01-02 00:29:24 rafal Exp $
 */
public interface CoralCore
{
    // configuration ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Checks if the given optional feature is enabled.
     * 
     * @param feature feature.
     * @return <code>true</code> if the feature is enabled.
     */
    public boolean isEnabled(Feature feature);
    
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
    
    /**
     * Returns a CoralRelationManager.
     *
     * @return a CoralRelationManager.
     */
    public CoralRelationManager getRelationManager();
    
    /**
     * Returns a CoralRelationQuery.
     *
     * @return a CoralRelationQuery.
     */
    public CoralRelationQuery getRelationQuery();
    
    /**
     * Returns the object instantiator.
     * 
     * @return an Instnatiator.
     */
    public Instantiator getInstantiator();
    
    /**
     * Returns the cache factory.
     * 
     * @return the cache factory.
     */
    public CacheFactory getCacheFactory();

    /**
     * Returns the RMLParserFactory.
     * 
     * @return am RMLParserFactory.
     */
    public RMLParserFactory getRMLParserFactory();
    
    // session //////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * Pushes a session on a Thread's session stack.
     * 
     * @param session the session.
     */
    public void pushSession(CoralSession session);
    
    /**
     * Returns the topmost session on a Thread's session stack.
     * 
     * @return the topmost session on a Thread's session stack, or <code>null</code> if none are 
     * present.
     */
    public CoralSession peekSession();
    
    /**
     * Removes a session from the Thread's session stack.
     * 
     * @param session the session to remove.
     * @throws IllegalArgumentException if session is no present in the stack.
     */
    public void removeSession(CoralSession session)
        throws IllegalArgumentException;
    
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
    
    /**
     * Returns all sessions associated with the calling thread.
     * 
     * @return all sessions associated with the calling thread as an immutable list.
     */
    public List<CoralSession> getAllSessions();
    
    /**
     * Returns the Subject assocaited with the calling thread.
     *
     * @return the Subject assocaited with the calling thread.
     * @throws IllegalStateException if the thread is not assocaited with a Subject.
     */
    public Subject getCurrentSubject()
        throws IllegalStateException;
    
    // utility //////////////////////////////////////////////////////////////
    
    /**
     * Returns a logger other Coral RI components may use.
     */
    public Logger getLog();
}
