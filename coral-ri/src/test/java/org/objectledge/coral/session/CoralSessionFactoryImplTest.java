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

import org.jmock.Mock;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSessionFactoryImplTest.java,v 1.8 2005-01-25 06:31:40 rafal Exp $
 */
public class CoralSessionFactoryImplTest extends LedgeTestCase
{
    private Mock mockCoralCore;
    private CoralCore coralCore;

    private CoralSessionFactoryImpl coralSessionFactoryImpl;

    private Mock mockPrincipal;
    private Principal principal;
    private Mock mockCoralSecurity;
    private CoralSecurity coralSecurity;
    private Mock mockSubject;
    private Subject subject;
    private Mock mockRootSubject;
    private Subject rootSubject;
    private Mock mockRootPrincipal;
    private Principal rootPrincipal;
    private Mock mockAnonymousSubject;
    private Subject anonymousSubject;
    private Mock mockAnonymousPrincipal;
    private Principal anonymousPrincipal;

    public void setUp()
    {
        mockCoralCore = mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stubs().method("getInstantiator").will(returnValue(null));
        mockCoralCore.stubs().method("getRMLParserFactory").will(returnValue(null));        
        
        coralSessionFactoryImpl = new CoralSessionFactoryImpl(coralCore, null);
        
        mockPrincipal = mock(Principal.class);
        principal = (Principal)mockPrincipal.proxy();
        mockPrincipal.stubs().method("getName").will(returnValue("<user>"));
        mockCoralSecurity = mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockCoralCore.stubs().method("getSecurity").will(returnValue(coralSecurity));
        mockSubject = mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        mockCoralSecurity.stubs().method("getSubject").with(eq("<user>")).will(returnValue(subject));
        mockRootSubject = mock(Subject.class);
        rootSubject = (Subject)mockRootSubject.proxy();
        mockRootPrincipal = mock(Principal.class, "rootPrincipal");
        rootPrincipal = (Principal)mockRootPrincipal.proxy();
        mockRootPrincipal.stubs().method("getName").will(returnValue("<root>"));
        mockRootSubject.stubs().method("getPrincipal").will(returnValue(rootPrincipal));
        mockCoralSecurity.stubs().method("getSubject").with(eq(Subject.ROOT)).will(returnValue(rootSubject));
        mockCoralSecurity.stubs().method("getSubject").with(eq("<root>")).will(returnValue(rootSubject));
        mockAnonymousSubject = mock(Subject.class);
        anonymousSubject = (Subject)mockAnonymousSubject.proxy();
        mockAnonymousPrincipal = mock(Principal.class, "anonymousPrincipal");
        anonymousPrincipal = (Principal)mockAnonymousPrincipal.proxy();
        mockAnonymousPrincipal.stubs().method("getName").will(returnValue("<anonymous>"));
        mockAnonymousSubject.stubs().method("getPrincipal").will(returnValue(anonymousPrincipal));
        mockCoralSecurity.stubs().method("getSubject").with(eq(Subject.ANONYMOUS)).will(returnValue(anonymousSubject));
        mockCoralSecurity.stubs().method("getSubject").with(eq("<anonymous>")).will(returnValue(anonymousSubject));
    }
    
    public void testCreation()
    {
    }
    
    public void testNormalSession()
        throws Exception
    {
        mockCoralCore.expects(once()).method("setCurrentSession").with(isA(CoralSession.class)).isVoid();
        CoralSession session = coralSessionFactoryImpl.getSession(principal);
        mockCoralCore.stubs().method("getCurrentSession").will(returnValue(session));
        
        assertSame(subject, session.getUserSubject());
        assertSame(principal, session.getUserPrincipal());         
        
        session.getStore();
        
        mockCoralCore.expects(once()).method("setCurrentSession").with(same(session)).isVoid();
        mockCoralCore.expects(once()).method("setCurrentSession").with(NULL).isVoid();
        session.close();
    }
    
    public void testRootSession()
    {
        mockCoralCore.expects(once()).method("setCurrentSession").with(isA(CoralSession.class)).isVoid();
        CoralSession session = coralSessionFactoryImpl.getRootSession();
        assertSame(rootSubject, session.getUserSubject()); 
    }

    public void testAnonymousSession()
    {
        mockCoralCore.expects(once()).method("setCurrentSession").with(isA(CoralSession.class)).isVoid();
        CoralSession session = coralSessionFactoryImpl.getAnonymousSession();
        assertSame(anonymousSubject, session.getUserSubject()); 
    }
}
