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
import org.jmock.Mock;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralSecurityTest.java,v 1.4 2004-03-24 14:40:11 fil Exp $
 */
public class SessionCoralSecurityTest extends LedgeTestCase
{
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private Mock mockCoralStore;
    private CoralStore coralStore;
    private Mock mockKeyedObjectPool;
    private KeyedObjectPool keyedObjectPool;
    
    private CoralSessionImpl session;
    private SessionCoralStore sessionCoralStore;
    
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

        session = new CoralSessionImpl(coralCore, keyedObjectPool);
        mockCoralCore.expect(once()).method("setCurrentSession").with(same(session)).isVoid();
        mockCoralCore.stub().method("getCurrentSession").will(returnValue(session));
        session.open(null, null);
        sessionCoralStore = new SessionCoralStore(coralCore, session);
    }
    
    public void testCreation()
    {
    }
    
    public void testGetResource()
    {
        mockCoralStore.expect(once()).method("getResource").will(returnValue(new Resource[0]));
        sessionCoralStore.getResource();
    }
}
