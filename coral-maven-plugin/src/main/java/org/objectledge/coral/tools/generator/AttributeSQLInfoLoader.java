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
import java.util.StringTokenizer;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.tools.generator.model.AttributeClass;
import org.objectledge.coral.tools.generator.model.AttributeSQLInfo;
import org.objectledge.coral.tools.generator.model.Schema;

/**
 * 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeSQLInfoLoader.java,v 1.1 2004-07-08 15:06:20 rafal Exp $
 */
public class AttributeSQLInfoLoader
{
    private final Schema schema;

    public AttributeSQLInfoLoader(Schema schema)
    {
        this.schema = schema;
    }
    
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
    
    public void execute(String line, int lineNum)
    	throws IOException
    {
        int eq = line.indexOf('=');
        if(eq < 0)
        {
            throw new IOException("missing = at line "+lineNum);
        }
        String attributeClassName = line.substring(0, eq);
        StringTokenizer st = new StringTokenizer(line.substring(eq+1),",");
        try
        {
            AttributeClass attributeClass = schema.getAttributeClass(attributeClassName);
            String internalType = st.nextToken();
            String externalTable = st.hasMoreTokens() ? st.nextToken() : null;
            String externalTableKey = st.hasMoreTokens() ? st.nextToken() : null;
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
