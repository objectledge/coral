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

import java.util.Map;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.InvalidResourceNameException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Session local CoralStore wrapper.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralStore.java,v 1.7 2005-06-13 11:08:43 rafal Exp $
 */
public class SessionCoralStore implements CoralStore
{
    private CoralCore coral;
    private CoralSessionImpl session;

    /**
     * Creates a session local CoralStore wrapper.
     * 
     * @param coral the coral component hub.
     * @param session the coral session.
     */
    SessionCoralStore(CoralCore coral, CoralSessionImpl session)
    {
        this.coral = coral;
        this.session = session;
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResource()
    {
        session.verify();
        return coral.getStore().getResource();
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResource(Resource parent)
    {
        session.verify();
        return coral.getStore().getResource(parent);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getResource(long id) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getStore().getResource(id);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResource(String name)
    {
        session.verify();
        return coral.getStore().getResource(name);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getUniqueResource(String name) throws IllegalStateException
    {
        session.verify();
        return coral.getStore().getUniqueResource(name);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResource(Resource parent, String name)
    {
        session.verify();
        return coral.getStore().getResource(parent, name);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getUniqueResource(Resource parent, String name) throws IllegalStateException
    {
        session.verify();
        return coral.getStore().getUniqueResource(parent, name);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResourceByPath(String path)
    {
        session.verify();
        return coral.getStore().getResourceByPath(path);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getUniqueResourceByPath(String path)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        session.verify();
        return coral.getStore().getUniqueResourceByPath(path);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResourceByPath(Resource start, String path)
    {
        session.verify();
        return coral.getStore().getResourceByPath(start, path);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getUniqueResourceByPath(Resource start, String path)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        session.verify();
        return coral.getStore().getUniqueResourceByPath(start, path);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource createResource(
        String name,
        Resource parent,
        ResourceClass resourceClass,
        Map attributes)
        throws UnknownAttributeException, ValueRequiredException, InvalidResourceNameException
    {
        session.verify();
        return coral.getStore().createResource(name, parent, resourceClass, attributes);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteResource(Resource resource)
        throws EntityInUseException, IllegalArgumentException
    {
        session.verify();
        coral.getStore().deleteResource(resource);
    }

    /** 
     * {@inheritDoc}
     */
    public int deleteTree(Resource res) throws EntityInUseException
    {
        session.verify();
        return coral.getStore().deleteTree(res);
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Resource resource, String name)
        throws InvalidResourceNameException
    {
        session.verify();
        coral.getStore().setName(resource, name);
    }

    /** 
     * {@inheritDoc}
     */
    public void setParent(Resource child, Resource parent) throws CircularDependencyException
    {
        session.verify();
        coral.getStore().setParent(child, parent);
    }

    /** 
     * {@inheritDoc}
     */
    public void unsetParent(Resource child)
    {
        session.verify();
        coral.getStore().unsetParent(child);
    }

    /** 
     * {@inheritDoc}
     */
    public void setOwner(Resource resource, Subject owner)
    {
        session.verify();
        coral.getStore().setOwner(resource, owner);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource copyResource(
        Resource source,
        Resource destinationParent,
        String destinationName)
        throws InvalidResourceNameException
    {
        session.verify();
        return coral.getStore().copyResource(source, destinationParent, destinationName);
    }

    /** 
     * {@inheritDoc}
     */
    public void copyTree(
        Resource sourceRoot,
        Resource destinationParent,
        String destinationName)
        throws CircularDependencyException, InvalidResourceNameException
    {
        session.verify();
        coral.getStore().copyTree(sourceRoot, destinationParent, destinationName);
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean isAncestor(
        Resource ancestor,
        Resource descendant)
    {
        session.verify();
        return coral.getStore().isAncestor(ancestor, descendant);
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isValidResourceName(String name)
    {
        session.verify();
        return coral.getStore().isValidResourceName(name);
    }

    /** 
     * {@inheritDoc}
     */
    public String getInvalidResourceNameCharacters(String name)
    {
        session.verify();
        return coral.getStore().getInvalidResourceNameCharacters(name);
    }    
}
