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

import java.util.ArrayList;
import java.util.List;

import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ImportToolTest.java,v 1.1 2004-03-28 09:48:57 fil Exp $
 */
public class ImportToolTest extends LedgeTestCase
{
    private ImportTool importTool;
    
    public void setUp()
    {
        List prefices = new ArrayList();
        prefices.add("java");
        prefices.add("org.objectledge");
        importTool = new ImportTool("pl.caltha.test", prefices);
    }
    
    public void testNormal()
    {
        importTool.add("org.objectledge.coral.store.Resource");
        importTool.add("pl.caltha.test.TestResource");
        String out = importTool.toString();
        assertEquals(
            "org.objectledge.coral.store.Resource;\n" +            "\n", out);        
    }
    
    public void testSubPackage()
    {
        importTool.add("org.objectledge.coral.store.Resource");
        importTool.add("pl.caltha.test.TestResource");
        importTool.add("pl.caltha.test.other.OtherResource");
        String out = importTool.toString();
        assertEquals(
            "org.objectledge.coral.store.Resource;\n" +
            "\n" +            "pl.caltha.test.other.OtherResource;\n" +            "\n", out);        
    }
    
    public void testJavaLang()
    {
        importTool.add("org.objectledge.coral.store.Resource");
        importTool.add("pl.caltha.test.TestResource");
        importTool.add("java.lang.Integer");
        String out = importTool.toString();
        assertEquals(
            "org.objectledge.coral.store.Resource;\n" +
            "\n", out);        
    }

    public void testJavaUtil()
    {
        importTool.add("org.objectledge.coral.store.Resource");
        importTool.add("pl.caltha.test.TestResource");
        importTool.add("java.util.Date");
        String out = importTool.toString();
        assertEquals(
            "java.util.Date;\n" +            "\n" +            "org.objectledge.coral.store.Resource;\n" +
            "\n", out);        
    }
}
