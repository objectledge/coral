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

import org.objectledge.authentication.UserAlreadyExistsException;
import org.objectledge.authentication.UserInUseException;
import org.objectledge.authentication.UserManagementParticipant;
import org.objectledge.authentication.UserUnknownException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;

/**
 * Coral based implementation of user management participant.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: CoralUserManagementParticipant.java,v 1.1 2005-05-18 06:53:04 pablo Exp $
 */
public class CoralUserManagementParticipant
    implements UserManagementParticipant
{
    private final CoralSessionFactory coralSessionFactory;

    /**
     * Creates new CoralRoleChecking instance.
     * 
     * @param coralSessionFactory the coral session factory.
     */
    public CoralUserManagementParticipant(CoralSessionFactory coralSessionFactory)
    {
        this.coralSessionFactory = coralSessionFactory;
    }
    
    /**
     * {@inheritDoc}
     */
	public boolean supportsRemoval()
	{
		return true;
	}

    /**
     * {@inheritDoc}
     */
	public void createAccount(Principal user)
		throws UserAlreadyExistsException
	{
		CoralSession coralSession = coralSessionFactory.getCurrentSession();
		try
		{
			coralSession.getSecurity().createSubject(user.getName());
		}
		catch(EntityExistsException e)
		{
			throw new UserAlreadyExistsException("Failed to create coral subject", e);
		}
	}
	
    /**
     * {@inheritDoc}
     */
	public void removeAccount(Principal user)
		throws UserUnknownException, UserInUseException
	{
		CoralSession coralSession = coralSessionFactory.getCurrentSession();
		try
		{
			Subject subject = coralSession.getSecurity().getSubject(user.getName());
			coralSession.getSecurity().deleteSubject(subject);
		}
		catch(EntityDoesNotExistException e)
		{
            // principal does not have an associated Coral subject - nothing to clean up here.
		}
		catch(EntityInUseException e)
		{
			throw new UserInUseException("Failed to delete coral subject", e);	
		}
	}
}
