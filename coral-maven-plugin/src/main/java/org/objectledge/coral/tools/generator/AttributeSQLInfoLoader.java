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
package org.objectledge.coral.tools.generator;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.tools.generator.model.AttributeClass;
import org.objectledge.coral.tools.generator.model.AttributeSQLInfo;
import org.objectledge.coral.tools.generator.model.Schema;

/**
 * An utility class that loads Coral to SQL attribute binding information from a text file into the 
 * generator's schema.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeSQLInfoLoader.java,v 1.4 2004-12-27 03:06:28 rafal Exp $
 */
public class AttributeSQLInfoLoader
{
    private final Schema schema;

    /**
     * Creates new AttributeSQLInfoLoader instance.
     * 
     * @param schema the generator schema to load information into.
     */
    public AttributeSQLInfoLoader(Schema schema)
    {
        this.schema = schema;
    }
    
    /**
     * Loads the SQL attribute information from an open Reader.
     * 
     * @param reader the Reader to use.
     * @throws IOException in case of I/O failure.
     */
    public void load(Reader reader)
    	throws IOException
    {
        LineNumberReader lineReader = new LineNumberReader(reader);
        while(lineReader.ready())
        {
            String line = lineReader.readLine().trim();
            if(line.length() == 0 || line.charAt(0) == '#')
            {
                continue;
            }
            execute(line, lineReader.getLineNumber());
        }
    }
    
    /**
     * Load a single line of SQL attribute information.
     * 
     * @param line line contents.
     * @param lineNum line number, for error reporting.
     * @throws IOException in case of I/O failure.
     */
    public void execute(String line, int lineNum)
    	throws IOException
    {
        int eq = line.indexOf('=');
        if(eq < 0)
        {
            throw new IOException("missing = at line "+lineNum);
        }
        String attributeClassName = line.substring(0, eq);
        try
        {
            AttributeClass attributeClass = schema.getAttributeClass(attributeClassName);
            int i = eq;
            int j = line.indexOf(',', i+1);
            String internalType = j > i+1 ? line.substring(i+1, j) : null;
            i = j;
            j = line.indexOf(',', i+1);
            String externalTable = j > i+1 ? line.substring(i+1, j) : null;
            i = j;
            j = line.length();            
            String externalTableKey = j > i+1 ? line.substring(i+1, j) : null;
            AttributeSQLInfo sqlInfo = new AttributeSQLInfo(internalType, externalTable, 
                externalTableKey);
            attributeClass.setSQLInfo(sqlInfo);
        }
        catch(EntityDoesNotExistException e)
        {
            throw (IOException)new IOException("unknown attribute class at line "+lineNum).
            	initCause(e);
        }
    }
}
