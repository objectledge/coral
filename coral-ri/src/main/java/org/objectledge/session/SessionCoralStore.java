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

import java.util.Map;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.CoralSession;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Session local CoralStore wrapper.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralStore.java,v 1.1 2004-03-05 15:05:55 fil Exp $
 */
public class SessionCoralStore implements CoralStore
{
    private CoralCore coral;
    private CoralSession session;

    /**
     * Creates a session local CoralStore wrapper.
     * 
     * @param coral the coral component hub.
     * @param coralSession the coral session.
     */
    SessionCoralStore(CoralCore coral, CoralSession session)
    {
        this.coral = coral;
        this.session = session;
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResource()
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getResource();
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResource(Resource parent)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getResource(parent);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getResource(long id) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getResource(id);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResource(String name)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getResource(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getUniqueResource(String name) throws IllegalStateException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getUniqueResource(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResource(Resource parent, String name)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getResource(parent, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getUniqueResource(Resource parent, String name) throws IllegalStateException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getUniqueResource(parent, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResourceByPath(String path)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getResourceByPath(path);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getUniqueResourceByPath(String path)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getUniqueResourceByPath(path);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] getResourceByPath(Resource start, String path)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getResourceByPath(start, path);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource getUniqueResourceByPath(Resource start, String path)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().getUniqueResourceByPath(start, path);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource createResource(
        String name,
        Resource parent,
        ResourceClass resourceClass,
        Map attributes,
        Subject creator)
        throws UnknownAttributeException, ValueRequiredException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().createResource(name, parent, resourceClass, attributes, 
                creator);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteResource(Resource resource)
        throws EntityInUseException, IllegalArgumentException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getStore().deleteResource(resource);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public int deleteTree(Resource res) throws EntityInUseException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().deleteTree(res);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Resource resource, String name)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getStore().setName(resource, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setParent(Resource child, Resource parent) throws CircularDependencyException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getStore().setParent(child, parent);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void unsetParent(Resource child)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getStore().unsetParent(child);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setOwner(Resource resource, Subject owner)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getStore().setOwner(resource, owner);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public Resource copyResource(
        Resource source,
        Resource destinationParent,
        String destinationName,
        Subject subject)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getStore().copyResource(source, destinationParent, destinationName, 
                subject);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void copyTree(
        Resource sourceRoot,
        Resource destinationParent,
        String destinationName,
        Subject subject)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getStore().copyTree(sourceRoot, destinationParent, destinationName, subject);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }
}
