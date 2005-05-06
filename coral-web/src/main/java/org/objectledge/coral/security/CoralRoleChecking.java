// 
// Copyright (c) 2003-2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral.security;

import java.security.Principal;

import org.objectledge.authentication.UserUnknownException;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.security.RoleChecking;

/**
 * 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralRoleChecking.java,v 1.1 2005-05-06 09:32:58 rafal Exp $
 */
public class CoralRoleChecking
    implements RoleChecking
{
    private final Context context;

    /**
     * Creates new CoralRoleChecking instance.
     * 
     * @param contextArg the request context.
     */
    public CoralRoleChecking(Context contextArg)
    {
        this.context = contextArg;
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getRoles(Principal user)
        throws UserUnknownException
    {
        CoralSession current = (CoralSession)context.getAttribute(CoralSession.class);
        if(current == null)
        {
            throw new IllegalStateException("no Coral session is active. "+
                "CoralSessionInitValve must precede PolicyCheckingValve in the pipeline");
        }
        Subject subject;
        try
        {
            subject = current.getSecurity().getSubject(user.getName());
        }
        catch(EntityDoesNotExistException e)
        {
            throw new UserUnknownException("unknown user "+user.getName());
        }
        Role[] roles = subject.getRoles();
        String[] result = new String[roles.length];
        int i = 0;
        for(Role r : roles)
        {
            result[i++] = r.getName();
        }
        return result;
    }
}
