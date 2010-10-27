// 
// Copyright (c) 2003-2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
//   
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//   
// * Redistributions of source code must retain the above copyright notice,  
// this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
// this list of conditions and the following disclaimer in the documentation  
// and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
// nor the names of its contributors may be used to endorse or promote products  
// derived from this software without specific prior written permission. 
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

package org.objectledge.coral.table.filter;

import java.security.Principal;

import javax.naming.directory.DirContext;

import org.objectledge.authentication.AuthenticationException;
import org.objectledge.authentication.UserManager;
import org.objectledge.coral.security.Subject;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.directory.DirectoryParameters;
import org.objectledge.table.TableFilter;

/**
 * Filters Subjects according to personal data.
 *
 * Will accept only the subjects for whom the pattern is found (using naive substring match) in 
 * any of the requested properties.
 *
 * @author <a href="rafal@caltha.pl">Rafał Krzewski</a>
 * @version $Id$
 */
public class PersonalDataFilter implements TableFilter<Subject>
{
    private final String[] properties;
    private final UserManager userManager;
    private final String pattern;

    /**
     * Creates a new PersonalDataFilter instance.
     *
     * @param properties properties to check
     * @param pattern pattern to look for
     * @param userManager user manager component.
     */
    public PersonalDataFilter(String[] properties, String pattern, UserManager userManager)
    {
        this.properties = properties;
        this.pattern = pattern;
        this.userManager = userManager;      
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(Subject s)
    {
        try
        {
           Principal p = userManager.getUserByName(s.getName());
           DirContext c = userManager.getPersonalData(p);
           Parameters d = new DirectoryParameters(c);
           for(String property : properties)
           {
               String v = d.get(property, "");
               if(v.contains(pattern))
               {
                   return true;
               }
           }    
           return false;
        }
        catch(AuthenticationException e)
        {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }
}
