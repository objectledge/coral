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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A tool for generating pretty-printed import lists in Java source files.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ImportTool.java,v 1.6 2005-02-21 15:44:54 zwierzem Exp $
 */
public class ImportTool
{
    private String packageName;
    private List<String> prefices;
    private Set<String> imports = new TreeSet<String>();

    /**
     * No arg constructor to allow mocking.
     */
    protected ImportTool()
    {
        // needed by jMock
    }
    
    /**
     * Creates an import tool instance.
     * 
     * @param packageName the name of this compilation unit's package.
     * @param prefices the package name prefeices to be used for grouping. 
     */
    public ImportTool(String packageName, List<String> prefices)
    {
        this.packageName = packageName;
        this.prefices = prefices;
    }
    
    /**
     * Adds a class to the import set.
     * 
     * @param fqClassName a fully qualified class name.
     */    
    public void add(String fqClassName)
    {
        if(!inPackage("java.lang.", fqClassName) && !inPackage(packageName, fqClassName))
        {
            imports.add(fqClassName);
        }
    }
    
    /**
     * Returns a formatted imports list.
     *  
     * @return a formatted imports list.
     */
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        List<List<String>> groups = new ArrayList<List<String>>(prefices.size()+1);
        for(int j = 0; j < prefices.size()+1; j++)
        {
            groups.add(new ArrayList<String>());        
        }
        for(Iterator<String> i = imports.iterator(); i.hasNext();)
        {
            String className = i.next();
            int matched;
            for(matched = 0; matched < prefices.size(); matched++)
            {
                String prefix = prefices.get(matched);
                if(className.startsWith(prefix))
                {
                    groups.get(matched).add(className);
                    break; 
                }
            }
            if(matched == prefices.size())
            {
                groups.get(matched).add(className);
            }
        }
        for(Iterator<List<String>> j = groups.iterator(); j.hasNext();)
        {
            List<String> group = j.next();
            if(group.isEmpty())
            {
                j.remove();
            }
            else
            {
                Collections.sort(group);
                for(Iterator<String> i = group.iterator(); i.hasNext();)
                {
                    String className = i.next();
                    buff.append("import ").append(className).append(";\n");
                }
                buff.append("\n");
            }
        }
        return buff.toString();
    }
    
    private boolean inPackage(String packageName, String className)
    {
        if(!className.startsWith(packageName))
        {
            return false;
        }
        int dot = className.indexOf('.', packageName.length());
        if(dot < 0)
        {
            return true;
        }
        if(dot < className.length() && Character.isUpperCase(className.charAt(dot+1)))
        {
            return true;
        }
        return false;
    }
}
