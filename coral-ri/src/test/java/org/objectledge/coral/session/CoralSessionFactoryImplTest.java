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

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.CoralSession;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSessionFactoryImplTest.java,v 1.2 2004-03-15 13:44:54 fil Exp $
 */
public class CoralSessionFactoryImplTest extends MockObjectTestCase
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
    private Mock mockAnonymousSubject;
    private Subject anonymousSubject;

    public void setUp()
    {
        mockCoralCore = new Mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        
        coralSessionFactoryImpl = new CoralSessionFactoryImpl(coralCore);
        
        mockPrincipal = new Mock(Principal.class);
        principal = (Principal)mockPrincipal.proxy();
        mockPrincipal.stub().method("getName").will(returnValue("<user>"));
        mockCoralSecurity = new Mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockCoralCore.stub().method("getSecurity").will(returnValue(coralSecurity));
        mockSubject = new Mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        mockCoralSecurity.stub().method("getSubject").with(eq("<user>")).will(returnValue(subject));
        mockRootSubject = new Mock(Subject.class);
        rootSubject = (Subject)mockRootSubject.proxy();
        mockRootSubject.stub().method("getName").will(returnValue("<root>"));
        mockCoralSecurity.stub().method("getSubject").with(eq(Subject.ROOT)).will(returnValue(rootSubject));
        mockCoralSecurity.stub().method("getSubject").with(eq("<root>")).will(returnValue(rootSubject));
        mockAnonymousSubject = new Mock(Subject.class);
        anonymousSubject = (Subject)mockAnonymousSubject.proxy();
        mockAnonymousSubject.stub().method("getName").will(returnValue("<anonymous>"));
        mockCoralSecurity.stub().method("getSubject").with(eq(Subject.ANONYMOUS)).will(returnValue(anonymousSubject));
        mockCoralSecurity.stub().method("getSubject").with(eq("<anonymous>")).will(returnValue(anonymousSubject));
    }
    
    public void testCreation()
    {
    }
    
    public void testNormalSession()
        throws Exception
    {
        mockCoralCore.expect(once()).method("setCurrentSession").with(isA(CoralSession.class)).isVoid();
        CoralSession session = coralSessionFactoryImpl.getSession(principal);
        mockCoralCore.stub().method("getCurrentSession").will(returnValue(session));
        
        assertSame(subject, session.getUserSubject());
        assertSame(principal, session.getUserPrincipal());         
        
        session.getStore();
        
        mockCoralCore.expect(once()).method("setCurrentSession").with(NULL).isVoid();
        session.close();
    }
    
    public void testRootSession()
    {
        mockCoralCore.expect(once()).method("setCurrentSession").with(isA(CoralSession.class)).isVoid();
        CoralSession session = coralSessionFactoryImpl.getRootSession();
        assertSame(rootSubject, session.getUserSubject()); 
    }

    public void testAnonymousSession()
    {
        mockCoralCore.expect(once()).method("setCurrentSession").with(isA(CoralSession.class)).isVoid();
        CoralSession session = coralSessionFactoryImpl.getAnonymousSession();
        assertSame(anonymousSubject, session.getUserSubject()); 
    }
}
