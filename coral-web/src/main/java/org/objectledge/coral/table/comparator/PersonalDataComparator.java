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

package org.objectledge.coral.table.comparator;

import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;

import org.objectledge.authentication.AuthenticationException;
import org.objectledge.authentication.UserManager;
import org.objectledge.coral.security.Subject;
import org.objectledge.parameters.DefaultParameters;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.directory.DirectoryParameters;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * Compares subjects according to personal data properties
 *
 * @author <a href="rafal@caltha.pl">Rafał Krzewski</a>
 * @version $Id$
 */
public class PersonalDataComparator extends BaseStringComparator<Subject>
{
    private final UserManager userManager;
    private final String property;

    private final Map<Subject, Parameters> personalData = new HashMap<>();

    /**
     * Creates a new PersonalDataComparator instance.
     *
     * @param userManager the user manager - provider of personal data
     * @param property the property to compare on 
     * @param locale the locale to use in string comparisons
     */
    public PersonalDataComparator(UserManager userManager, String property, Locale locale)
    {
        super(locale);
        this.userManager = userManager;
        this.property = property;       
    }

    /**
     * {@inheritDoc}
     */
    public int compare(Subject s1, Subject s2)
    {
        try
        {
            Parameters d1 = getPersonalData(s1);
            Parameters d2 = getPersonalData(s2);
            String v1 = firstVal(d1, property, "");
            String v2 = firstVal(d2, property, "");
            return compareStrings(v1, v2);
        }
        catch(AuthenticationException | NamingException e)
        {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    private String firstVal(Parameters p, String property, String defVal)
    {
        String[] vals = p.getStrings(property);
        return vals.length > 0 ? vals[0] : defVal;
    }

    private Parameters getPersonalData(Subject s)
        throws AuthenticationException, NamingException
    {
        Parameters pd;
        pd = personalData.get(s);
        if(pd == null)
        {
            Principal p = userManager.getUserByName(s.getName());
            try(DirectoryParameters d = new DirectoryParameters(userManager.getPersonalData(p)))
            {
                pd = new DefaultParameters(d);
            }
            personalData.put(s, pd);
        }
        return pd;
    }
}
