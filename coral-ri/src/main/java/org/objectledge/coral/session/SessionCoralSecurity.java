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
package org.objectledge.coral.session;

import org.objectledge.coral.CoralCore;
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
 * @version $Id: SessionCoralSecurity.java,v 1.3 2004-03-15 13:44:53 fil Exp $
 */
public class SessionCoralSecurity implements CoralSecurity
{
    private CoralCore coral;
    private CoralSessionImpl session;

    /**
     * Creates a session local CoralStore wrapper.
     * 
     * @param coral the coral component hub.
     * @param coralSession the coral session.
     */
    SessionCoralSecurity(CoralCore coral, CoralSessionImpl session)
    {
        this.coral = coral;
        this.session = session;
    }

    /** 
     * {@inheritDoc}
     */
    public Subject[] getSubject()
    {
        session.verify();
        return coral.getSecurity().getSubject();
    }

    /** 
     * {@inheritDoc}
     */
    public Subject getSubject(long id) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getSecurity().getSubject(id);
    }

    /** 
     * {@inheritDoc}
     */
    public Subject getSubject(String name) throws EntityDoesNotExistException
    {
        session.verify();
            return coral.getSecurity().getSubject(name);
    }

    /** 
     * {@inheritDoc}
     */
    public Subject createSubject(String name) throws EntityExistsException
    {
        session.verify();
            return coral.getSecurity().createSubject(name);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteSubject(Subject subject) throws EntityInUseException
    {
        session.verify();
        coral.getSecurity().deleteSubject(subject);
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Subject subject, String name) throws EntityExistsException
    {
        session.verify();
        coral.getSecurity().setName(subject, name);
    }

    /** 
     * {@inheritDoc}
     */
    public Role[] getRole()
    {
        session.verify();
        return coral.getSecurity().getRole();
    }

    /** 
     * {@inheritDoc}
     */
    public Role getRole(long id) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getSecurity().getRole(id);
    }

    /** 
     * {@inheritDoc}
     */
    public Role[] getRole(String name)
    {
        session.verify();
        return coral.getSecurity().getRole(name);
    }

    /** 
     * {@inheritDoc}
     */
    public Role getUniqueRole(String name) throws IllegalStateException
    {
        session.verify();
        return coral.getSecurity().getUniqueRole(name);
    }

    /** 
     * {@inheritDoc}
     */
    public Role createRole(String name)
    {
        session.verify();
        return coral.getSecurity().createRole(name);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteRole(Role role) throws EntityInUseException
    {
        session.verify();
        coral.getSecurity().deleteRole(role);
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Role role, String name)
    {
        session.verify();
        coral.getSecurity().setName(role, name);
    }

    /** 
     * {@inheritDoc}
     */
    public void addSubRole(Role superRole, Role subRole) throws CircularDependencyException
    {
        session.verify();
        coral.getSecurity().addSubRole(superRole, subRole);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteSubRole(Role superRole, Role subRole) throws IllegalArgumentException
    {
        session.verify();
        coral.getSecurity().deleteSubRole(superRole, subRole);
    }

    /** 
     * {@inheritDoc}
     */
    public void grant(Role role, Subject subject, boolean grantingAllowed)
        throws SecurityException
    {
        session.verify();
        coral.getSecurity().grant(role, subject, grantingAllowed);
    }

    /** 
     * {@inheritDoc}
     */
    public void revoke(Role role, Subject subject)
        throws IllegalArgumentException, SecurityException
    {
        session.verify();
        coral.getSecurity().revoke(role, subject);
    }

    /** 
     * {@inheritDoc}
     */
    public Permission[] getPermission()
    {
        session.verify();
        return coral.getSecurity().getPermission();
    }

    /** 
     * {@inheritDoc}
     */
    public Permission getPermission(long id) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getSecurity().getPermission(id);
    }

    /** 
     * {@inheritDoc}
     */
    public Permission[] getPermission(String name)
    {
        session.verify();
        return coral.getSecurity().getPermission(name);
    }

    /** 
     * {@inheritDoc}
     */
    public Permission getUniquePermission(String name) throws IllegalStateException
    {
        session.verify();
        return coral.getSecurity().getUniquePermission(name);
    }

    /** 
     * {@inheritDoc}
     */
    public Permission createPermission(String name)
    {
        session.verify();
        return coral.getSecurity().createPermission(name);
    }

    /** 
     * {@inheritDoc}
     */
    public void deletePermission(Permission permission) throws EntityInUseException
    {
        session.verify();
        coral.getSecurity().deletePermission(permission);
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Permission permission, String name)
    {
        session.verify();
        coral.getSecurity().setName(permission, name);
    }

    /** 
     * {@inheritDoc}
     */
    public void addPermission(ResourceClass resourceClass, Permission permission)
    {
        session.verify();
        coral.getSecurity().addPermission(resourceClass, permission);
    }

    /** 
     * {@inheritDoc}
     */
    public void deletePermission(ResourceClass resourceClass, Permission permission)
        throws IllegalArgumentException
    {
        session.verify();
        coral.getSecurity().deletePermission(resourceClass, permission);
    }

    /** 
     * {@inheritDoc}
     */
    public void grant(
        Resource resource,
        Role role,
        Permission permission,
        boolean inherited)
        throws SecurityException
    {
        session.verify();
        coral.getSecurity().grant(resource, role, permission, inherited);
    }

    /** 
     * {@inheritDoc}
     */
    public void revoke(Resource resource, Role role, Permission permission)
        throws IllegalArgumentException, SecurityException
    {
        session.verify();
        coral.getSecurity().revoke(resource, role, permission);
    }
}
