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
package org.objectledge.coral.script;

import java.io.OutputStreamWriter;
import java.io.StringReader;

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.script.parser.ASTscript;
import org.objectledge.coral.script.parser.ParseException;
import org.objectledge.coral.script.parser.RMLParser;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.CoralStore;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ParserAndExecutorTest.java,v 1.1 2004-03-18 17:32:55 fil Exp $
 */
public class ParserAndExecutorTest extends MockObjectTestCase
{
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockCoralSecurity;
    private CoralSecurity coralSecurity;
    private Mock mockCoralStore;
    private CoralStore coralStore;
    private Mock mockCoralQuery;
    private CoralQuery coralQuery;
    private Mock mockCoralSession;
    private CoralSession coralSession;
    private Mock mockCoralSessionFactory;
    private CoralSessionFactory coralSessionFactory;
    
    private RMLExecutor executor;
    
    public void setUp()
        throws Exception
    {
        mockCoralSchema = new Mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = new Mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockCoralStore = new Mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralQuery = new Mock(CoralQuery.class);
        coralQuery = (CoralQuery)mockCoralQuery.proxy();
        mockCoralSession = new Mock(CoralSession.class);
        coralSession = (CoralSession)mockCoralSession.proxy();
        mockCoralSession.stub().method("getSchema").will(returnValue(coralSchema));
        mockCoralSession.stub().method("getSecurity").will(returnValue(coralSecurity));
        mockCoralSession.stub().method("getStore").will(returnValue(coralStore));
        mockCoralSession.stub().method("getQuery").will(returnValue(coralQuery));
        mockCoralSessionFactory = new Mock(CoralSessionFactory.class);
        
        executor = new RMLExecutor(coralSession, new OutputStreamWriter(System.out), 
            coralSessionFactory);
    }
    
    private void execute(String script) 
        throws ParseException
    {
        RMLParser parser = new RMLParser(new StringReader(script));
        executor.visit(parser.script(), null);        
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    public void testCreateAttributeClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("createAttributeClass").with(eq("class"), 
            eq("java_class"), eq("handler_class"), eq("db_table")).will(returnValue(null));
        execute("CREATE ATTRIBUTE CLASS class JAVA CLASS java_class HANDLER CLASS handler_class "+
            "DB TABLE db_table;");
    }
}
