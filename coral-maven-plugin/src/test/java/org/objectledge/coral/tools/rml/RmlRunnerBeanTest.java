// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral.tools.rml;

import org.hsqldb.jdbc.jdbcDataSource;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.tools.SessionFactoryTag;
import org.objectledge.coral.tools.init.InitBean;
import org.objectledge.test.LedgeTestCase;

/**
 * An integration test for the {@link InitBean}, {@link SessionFactoryTag} and 
 * {@link RmlRunnerBean}.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: RmlRunnerBeanTest.java,v 1.6 2005-02-16 16:21:08 rafal Exp $
 */
public class RmlRunnerBeanTest 
    extends LedgeTestCase
{
    public void testRmlRunnerBean()
        throws Exception
    {
        jdbcDataSource dataSource = new jdbcDataSource();
        dataSource.setDatabase("jdbc:hsqldb:.");
        dataSource.setUser("sa");
        
        InitBean initBean = new InitBean();
        initBean.setDataSource(dataSource);
        initBean.run();
        
        SessionFactoryTag sessionFactoryTag = new SessionFactoryTag();
        sessionFactoryTag.setDataSource(dataSource);
        CoralSessionFactory coralSessionFactory = sessionFactoryTag.getSessionFactory();
        CoralSession coralSession = coralSessionFactory.getRootSession();
        
        RmlRunnerBean rmlRunnerBean = new RmlRunnerBean();
        rmlRunnerBean.setSession(coralSession);
        rmlRunnerBean.setBaseDir("src/test/project/rml");
        rmlRunnerBean.setSourcesList("src/main/rmlSources.lst");
        rmlRunnerBean.setFileEncoding("UTF-8");
        rmlRunnerBean.run();
        
        assertNotNull(coralSession.getSecurity().getRole("coral.test.FooRole"));
        assertNotNull(coralSession.getSchema().getResourceClass("coral.test.Foo"));
        assertNotNull(coralSession.getStore().getUniqueResourceByPath("/fred"));
    }
}
