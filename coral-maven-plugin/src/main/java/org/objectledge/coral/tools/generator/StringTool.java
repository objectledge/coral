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

import java.util.StringTokenizer;

import org.objectledge.utils.StringUtils;

/**
 * Simple string tool for usage inside class templates.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: StringTool.java,v 1.3 2005-02-21 15:44:54 zwierzem Exp $
 */
public class StringTool
{
    private int width;
 
    /**
     * Creates new StringTool instance.
     * 
     * @param width the source file wrapping margin.
     */
    public StringTool(int width)
    {
        this.width = width;
    }
    
    /** 
     * Wrap method signature appropriately.
     * 
     * @param s the method signature.
     * @return the wrapped method signature.
     */
    public String wrap(String s)
    {
        String w = StringUtils.wrap(s, width - 8);
        StringTokenizer wt = new StringTokenizer(w, "\n");
        StringBuilder buff = new StringBuilder(w.length()+wt.countTokens()*4+4);
        String l = wt.nextToken();
        buff.append("    ").append(l);
        if(wt.hasMoreTokens())
        {
            buff.append('\n');
        }
        while(wt.hasMoreTokens())
        {
            l = wt.nextToken();
            buff.append("        ").append(l);
            if(wt.hasMoreTokens())
            {
                buff.append('\n');
            }
        }
        return buff.toString();
    }
}
