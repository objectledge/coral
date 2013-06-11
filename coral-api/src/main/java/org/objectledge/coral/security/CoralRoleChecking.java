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
import java.util.HashSet;
import java.util.Set;

import org.objectledge.authentication.UserUnknownException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.security.RoleChecking;

/**
 * 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralRoleChecking.java,v 1.1 2005-05-18 06:53:04 pablo Exp $
 */
public class CoralRoleChecking
    implements RoleChecking
{
    private final CoralSessionFactory coralSessionFactory;

    /**
     * Creates new CoralRoleChecking instance.
     * 
     * @param coralSessionFactory the coral session factory.
     */
    public CoralRoleChecking(CoralSessionFactory coralSessionFactory)
    {
        this.coralSessionFactory = coralSessionFactory;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<String> getRoles(Principal user)
        throws UserUnknownException
    {
        Subject subject = getSubject(user);
        Role[] roles = subject.getRoles();
        Set<String> result = new HashSet<>(roles.length);
        for(Role r : roles)
        {
            result.add(r.getName());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRole(Principal user, String role)
        throws UserUnknownException
    {
        Subject subject = getSubject(user);
        Role[] roles = subject.getRoles();
        for(Role r : roles)
        {
            if(r.getName().equals(role))
            {
                return true;
            }
        }
        return false;
    }

    private Subject getSubject(Principal user)
        throws UserUnknownException
    {
        CoralSession current = coralSessionFactory.getCurrentSession();
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
        return subject;
    }
}
