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

import java.lang.ref.WeakReference;
import java.security.Principal;

import org.apache.commons.pool.KeyedObjectPool;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.CoralRelationQuery;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.script.CoralScript;
import org.objectledge.coral.script.CoralScriptImpl;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;

/**
 * A coral session implementation.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSessionImpl.java,v 1.9 2004-06-28 07:55:55 fil Exp $
 */
public class CoralSessionImpl
    implements CoralSession
{
    private CoralCore coral;
    private CoralSchema schema;
    private CoralSecurity security;
    private CoralStore store;
    private CoralEventWhiteboard eventWhiteboard;
    private CoralScript script;    
    private CoralQuery query;
    private CoralRelationManager relationManager;
    private CoralRelationQuery relationQuery;
    
    private Subject subject;
    private Principal principal;
    
    private KeyedObjectPool pool;
    private boolean open;
    private WeakReference ownerThread;

    CoralSessionImpl(CoralCore coral, KeyedObjectPool pool)
    {
        this.coral = coral;
        this.pool = pool;
                
        schema = new SessionCoralSchema(coral, this);
        security = new SessionCoralSecurity(coral, this);
        store = new SessionCoralStore(coral, this);
        eventWhiteboard = new SessionCoralEventWhiteboard(coral, this);
        script = new CoralScriptImpl(this, coral.getInstantiator(), coral.getRMLParserFactory());
        relationManager = new SessionCoralRelationManager(coral, this);
        relationQuery = new SessionCoralRelationQuery(coral, this);
    }

    void open(Principal principal, Subject subject)
    {
        this.subject = subject;
        this.principal = principal;
        ownerThread = new WeakReference(Thread.currentThread());
        open = true;
        coral.setCurrentSession(this);
    }
    
    void verify()
    {
        if(!open)
        {
            throw new IllegalStateException("session is closed");
        }
        Thread owner = (Thread)ownerThread.get();
        if(owner == null || !Thread.currentThread().equals(owner))
        {
            throw new IllegalStateException("attempted to use session from wrong thread");     
        }
        if(!this.equals(coral.getCurrentSession()))
        {
            throw new IllegalStateException("another session is active for this thread."+
                " Use makeCurrent() to switch");
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void close()
    {
        CoralSession current = coral.getCurrentSession();
        this.makeCurrent();
        verify();
        open = false;
        ownerThread = null;
        if(current == this)
        {
            coral.setCurrentSession(null);
        }
        else
        {
            coral.setCurrentSession(current);
        }
        try
        {
            pool.returnObject(principal, this);
        }
        catch(Exception e)
        {
            throw new BackendException("failed to recycle session object");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void makeCurrent()
    {
        coral.setCurrentSession(this);
    }

    /** 
     * {@inheritDoc}
     */
    public CoralScript getScript()
    {
        return script;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralEventWhiteboard getEvent()
    {
        return eventWhiteboard;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSchema getSchema()
    {
        return schema;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSecurity getSecurity()
    {
        return security;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralStore getStore()
    {
        return store;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralQuery getQuery()
    {
        return query;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralRelationManager getRelationManager()
    {
        return relationManager;
    }

    /** 
     * {@inheritDoc}
     */
    public CoralRelationQuery getRelationQuery()
    {
        return relationQuery;
    }
    
    /** 
     * {@inheritDoc}
     */
    public Principal getUserPrincipal()
    {
        return principal;
    }

    /** 
     * {@inheritDoc}
     */
    public Subject getUserSubject()
    {
        return subject;
    }
}
