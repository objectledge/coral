// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.session;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.CoralSession;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.SecurityException;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * A session local CoralStore wrapper.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralSecurity.java,v 1.1 2004-03-05 15:05:55 fil Exp $
 */
public class SessionCoralSecurity implements CoralSecurity
{
    private CoralCore coral;
    private CoralSession session;

    /**
     * Creates a session local CoralStore wrapper.
     * 
     * @param coral the coral component hub.
     * @param coralSession the coral session.
     */
    SessionCoralSecurity(CoralCore coral, CoralSession session)
    {
        this.coral = coral;
        this.session = session;
    }

    /** 
     * {@inheritDoc}
     */
    public Subject[] getSubject()
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getSubject();
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Subject getSubject(long id) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getSubject(id);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Subject getSubject(String name) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getSubject(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Subject createSubject(String name) throws EntityExistsException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().createSubject(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteSubject(Subject subject) throws EntityInUseException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().deleteSubject(subject);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Subject subject, String name) throws EntityExistsException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().setName(subject, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Role[] getRole()
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getRole();
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Role getRole(long id) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getRole(id);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Role[] getRole(String name)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getRole(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Role getUniqueRole(String name) throws IllegalStateException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getUniqueRole(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Role createRole(String name)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().createRole(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteRole(Role role) throws EntityInUseException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().deleteRole(role);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Role role, String name)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().setName(role, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void addSubRole(Role superRole, Role subRole) throws CircularDependencyException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().addSubRole(superRole, subRole);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteSubRole(Role superRole, Role subRole) throws IllegalArgumentException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().deleteSubRole(superRole, subRole);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void grant(Role role, Subject subject, boolean grantingAllowed, Subject grantor)
        throws SecurityException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().grant(role, subject, grantingAllowed, grantor);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void revoke(Role role, Subject subject, Subject revoker)
        throws IllegalArgumentException, SecurityException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().revoke(role, subject, revoker);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Permission[] getPermission()
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getPermission();
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Permission getPermission(long id) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getPermission(id);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Permission[] getPermission(String name)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getPermission(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Permission getUniquePermission(String name) throws IllegalStateException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().getUniquePermission(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Permission createPermission(String name)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSecurity().createPermission(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deletePermission(Permission permission) throws EntityInUseException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().deletePermission(permission);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Permission permission, String name)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().setName(permission, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void addPermission(ResourceClass resourceClass, Permission permission)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().addPermission(resourceClass, permission);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deletePermission(ResourceClass resourceClass, Permission permission)
        throws IllegalArgumentException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().deletePermission(resourceClass, permission);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void grant(
        Resource resource,
        Role role,
        Permission permission,
        boolean inherited,
        Subject grantor)
        throws SecurityException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().grant(resource, role, permission, inherited, grantor);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void revoke(Resource resource, Role role, Permission permission, Subject revoker)
        throws IllegalArgumentException, SecurityException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSecurity().revoke(resource, role, permission, revoker);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }
}
