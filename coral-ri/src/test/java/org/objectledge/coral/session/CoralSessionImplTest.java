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

import java.security.Principal;

import org.apache.commons.pool.KeyedObjectPool;
import org.jmock.Mock;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSessionImplTest.java,v 1.4 2004-03-24 14:40:11 fil Exp $
 */
public class CoralSessionImplTest extends LedgeTestCase
{
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private Mock mockKeyedObjectPool;
    private KeyedObjectPool keyedObjectPool;
    private Mock mockCoralStore;
    private CoralStore coralStore;
    
    private CoralSessionImpl coralSession;
    
    private Mock mockSubject;
    private Subject subject;
    private Mock mockPrincipal;
    private Principal principal;
    
    public void setUp()
    {
        mockCoralCore = mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralStore = mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralCore.stub().method("getStore").will(returnValue(coralStore));
        mockCoralCore.stub().method("getInstantiator").will(returnValue(null));
        mockCoralCore.stub().method("getRMLParserFactory").will(returnValue(null));        
        mockKeyedObjectPool = mock(KeyedObjectPool.class);
        keyedObjectPool = (KeyedObjectPool)mockKeyedObjectPool.proxy();
        
        coralSession = new CoralSessionImpl(coralCore, keyedObjectPool);
        
        mockSubject = mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        mockPrincipal = mock(Principal.class);
        principal = (Principal)mockPrincipal.proxy();
    }
    
    public void testBasicLifecycle()
    {
        mockCoralCore.expect(once()).method("setCurrentSession").with(same(coralSession)).isVoid();
        mockCoralCore.stub().method("getCurrentSession").will(returnValue(coralSession));
        coralSession.open(principal, subject);
        
        mockKeyedObjectPool.expect(once()).method("returnObject").with(same(coralSession), same(principal)).isVoid();
        assertEquals(principal, coralSession.getUserPrincipal());
        assertEquals(subject, coralSession.getUserSubject());

        coralSession.getEvent();
        
        coralSession.getQuery();
        
        coralSession.getSchema();
        
        coralSession.getSecurity();
        
        coralSession.getStore();

        mockCoralCore.expect(once()).method("setCurrentSession").with(NULL).isVoid();
        coralSession.close();
        
        try
        {
            coralSession.close();
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(IllegalStateException.class, e.getClass());
        }
    }
    
    public void testCrossThreadMischief()
    {
        mockCoralCore.expect(once()).method("setCurrentSession").with(same(coralSession)).isVoid();
        mockCoralCore.stub().method("getCurrentSession").will(returnValue(coralSession));
        coralSession.open(principal, subject);
        mockKeyedObjectPool.expect(once()).method("returnObject").with(same(coralSession), same(principal)).isVoid();
        mockCoralCore.expect(once()).method("setCurrentSession").with(NULL).isVoid();
        coralSession.close();
        // session is closed and returned to the pool
        
        final Object semaphore = new Object();
        Runnable runnable = new Runnable() {
            public void run()
            {
                // another thread opens session, it gets pre-owned session instance
                mockCoralCore.expect(once()).method("setCurrentSession").with(same(coralSession)).isVoid();
                coralSession.open(principal, subject);
                synchronized(semaphore)
                {
                    semaphore.notify();
                }
            }
        };
        Thread otherThread = new Thread(runnable);
        // make sure the other thread does not attempt to wake us up before we actually go to sleep
        synchronized(semaphore)
        {
            otherThread.start();
            try
            {
                semaphore.wait();
            }
            catch(InterruptedException e)
            {               
            }
        }
        // do evil - attempt to run an operation on a stale session reference that was possibly
        // reopened by another thread, with other user's credentials
        try
        {
            coralSession.getStore().deleteResource(null);
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(IllegalStateException.class, e.getClass());
            assertEquals("attempted to use session from wrong thread", e.getMessage());
        }
    }
    
    public void testCrossSessionMischief()
        throws Exception
    {
        mockCoralCore.expect(once()).method("setCurrentSession").with(same(coralSession)).isVoid();
        mockCoralCore.stub().method("getCurrentSession").will(returnValue(coralSession));
        coralSession.open(principal, subject);
        
        CoralSessionImpl coralSession2 = new CoralSessionImpl(coralCore, keyedObjectPool);
        mockCoralCore.expect(once()).method("setCurrentSession").with(same(coralSession2)).isVoid();
        mockCoralCore.stub().method("getCurrentSession").will(returnValue(coralSession2));
        coralSession2.open(principal, subject);
        
        try
        {
            // coralSession2 is now active, but we attempt an operation using old session
            coralSession.getStore().getResource();
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(IllegalStateException.class, e.getClass());
            assertEquals("another session is active for this thread."+
            " Use makeCurrent() to switch", e.getMessage());            
        }
        
        // switch explicitly        
        mockCoralCore.expect(once()).method("setCurrentSession").with(same(coralSession)).isVoid();
        mockCoralCore.stub().method("getCurrentSession").will(returnValue(coralSession));
        coralSession.makeCurrent();
        mockCoralStore.expect(once()).method("getResource").will(returnValue(null));
        coralSession.getStore().getResource();
    }
}
