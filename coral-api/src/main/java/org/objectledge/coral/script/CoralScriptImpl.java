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

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.objectledge.coral.InstantiationException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.script.parser.ASTscript;
import org.objectledge.coral.script.parser.RMLParser;
import org.objectledge.coral.script.parser.RMLParserFactory;
import org.objectledge.coral.session.CoralSession;

/**
 * An implementation of CoralScript interface using RML language.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralScriptImpl.java,v 1.2 2004-12-27 02:33:17 rafal Exp $
 */
public class CoralScriptImpl
    implements CoralScript
{
    // instance variables ////////////////////////////////////////////////////////////////////////
    
    /** the coral session to use. */
    private CoralSession session;

    /** the instnatiator, for producing RMLExecutor instnaces. */
    private Instantiator instantiator;
    
    /** the parser factory. */
    private RMLParserFactory parserFactory;

    // initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a CoralScript instance.
     * 
     * @param session the session to use.
     * @param instantiator the instantiator.
     * @param rmlParserFactory the factory of RML parsers.
     */    
    public CoralScriptImpl(CoralSession session, Instantiator instantiator, 
        RMLParserFactory rmlParserFactory)
    {
        this.session = session;
        this.instantiator = instantiator;
        this.parserFactory = rmlParserFactory;
    }
    
    /**
     * {@inheritDoc}
     */
    public String runScript(String in)
        throws Exception
    {
        StringReader sr = new StringReader(in);
        return runScript(sr);
    }

    /**
     * {@inheritDoc}
     */
    public String runScript(Reader in)
        throws Exception
    {
        StringWriter sw = new StringWriter();
        runScript(in, sw);
        return sw.getBuffer().toString();
    }

    /**
     * {@inheritDoc}
     */
    public void runScript(String in, Writer out)
        throws Exception
    {
        StringReader sr = new StringReader(in);
        runScript(sr, out);        
    }

    /**
     * {@inheritDoc}
     */
    public void runScript(Reader in, Writer out)
        throws Exception
    {
        RMLParser parser = null;
        try
        {
            RMLExecutor executor = getExecutor(session, out);
            parser = parserFactory.getParser(in);
            ASTscript script = parser.script();
            executor.visit(script, null);
        }
        finally
        {
            if(parser != null)
            {
                parserFactory.recycle(parser);
            }
        }
    }    

    // implementation ///////////////////////////////////////////////////////////////////////////

    private RMLExecutor getExecutor(CoralSession session, Writer out)
        throws InstantiationException
    {
        Map<Class<?>, Object> additional = new HashMap<Class<?>, Object>();
        additional.put(CoralSession.class, session);
        additional.put(Writer.class, out);
        return instantiator.newInstance(RMLExecutor.class, additional);
    }
}
