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
package org.objectledge.coral.script.parser;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: RMLParserFactory.java,v 1.1 2004-03-18 14:28:12 fil Exp $
 */
public class RMLParserFactory
{
    private ObjectPool pool;
    
    private Reader reader;
    
    /**
     * Creates a parser factory instance.
     */
    public RMLParserFactory()
    {
        pool = new GenericObjectPool(new ObjectFactory());
    }
    
    /**
     * Returns a new parser.
     * 
     * @param in the reader for reading the script.
     * @return the parser.
     */    
    public synchronized RMLParser getParser(Reader in)
    {
        try
        {
            reader = in;
            return (RMLParser)pool.borrowObject();
        }
        catch(Exception e)
        {
            throw (RuntimeException)new IllegalStateException("unable to create parser").
                initCause(e);            
        }
        finally
        {
            reader = null;
        }
    }

    /**
     * Recycles a parser.
     * 
     * @param parser the parser to recycle.
     */    
    public void recycle(RMLParser parser)
    {
        try
        {
            pool.returnObject(parser);
        }
        catch(Exception e)
        {
            throw (RuntimeException)new IllegalStateException("unable to recycle parser").
                initCause(e);            
        }
    }

    /**
     * Object pool factory for creating/reinitializing parsers.
     */    
    private class ObjectFactory
        extends BasePoolableObjectFactory
    {
        /** 
         * {@inheritDoc}
         */
        public Object makeObject() 
            throws Exception
        {
            return new RMLParser(reader);
        }
        
        /** 
         * {@inheritDoc}
         */
        public void activateObject(Object object) throws Exception
        {
            ((RMLParser)object).ReInit(reader);
        }
    }
}
