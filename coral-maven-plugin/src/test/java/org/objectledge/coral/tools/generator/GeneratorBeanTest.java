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

import org.apache.log4j.BasicConfigurator;
import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.templating.Templating;
import org.objectledge.utils.LedgeTestCase;

public class GeneratorBeanTest extends LedgeTestCase
{
    private GeneratorBean generatorBean;
    
    public void setUp()
    {
        generatorBean = new GeneratorBean();
    }
    
    public void testIntegartion()
        throws Exception
    {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        generatorBean.setBaseDir("src/test/project/generator");
        generatorBean.setFileEncoding("UTF-8");
        generatorBean.setHeaderFile("/LICENSE.txt");
        generatorBean.setImportGroups("java.,javax.,org.objectledge.");
        generatorBean.setPackageIncludes("org.objectledge.coral.test*");
        generatorBean.setPackageExcludes("org.objectledge.coral.test.excluded");        
        generatorBean.setSourceFiles("src/main/rmlSources.lst");
        generatorBean.setTargetDir("src/main/java");
        generatorBean.execute();
    }
}
