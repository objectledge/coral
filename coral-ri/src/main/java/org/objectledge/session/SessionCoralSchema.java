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
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.JavaClassException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.SchemaIntegrityException;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Session local CoralSchema wrapper.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralSchema.java,v 1.1 2004-03-05 15:05:55 fil Exp $
 */
public class SessionCoralSchema implements CoralSchema
{
    private CoralCore coral;
    private CoralSession session;

    /**
     * Creates a session local CoralStore wrapper.
     * 
     * @param coral the coral component hub.
     * @param coralSession the coral session.
     */
    SessionCoralSchema(CoralCore coral, CoralSession session)
    {
        this.coral = coral;
        this.session = session;
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeClass[] getAttributeClass()
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().getAttributeClass();
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeClass getAttributeClass(long id) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().getAttributeClass(id);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeClass getAttributeClass(String name) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().getAttributeClass(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeClass createAttributeClass(
        String name,
        String javaClass,
        String handlerClass,
        String dbTable)
        throws EntityExistsException, JavaClassException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().createAttributeClass(name, javaClass, handlerClass, dbTable);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteAttributeClass(AttributeClass attributeClass) throws EntityInUseException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().deleteAttributeClass(attributeClass);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(AttributeClass attributeClass, String name) throws EntityExistsException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setName(attributeClass, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setJavaClass(AttributeClass attributeClass, String javaClass)
        throws JavaClassException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setJavaClass(attributeClass, javaClass);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setHandlerClass(AttributeClass attributeClass, String handlerClass)
        throws JavaClassException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setHandlerClass(attributeClass, handlerClass);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setDbTable(AttributeClass attributeClass, String dbTable)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setDbTable(attributeClass, dbTable);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeDefinition createAttribute(
        String name,
        AttributeClass attributeClass,
        String domain,
        int flags)
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().createAttribute(name, attributeClass, domain, flags);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeDefinition[] getAttribute()
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().getAttribute();
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeDefinition getAttribute(long id) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().getAttribute(id);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteAttribute(ResourceClass resourceClass, AttributeDefinition attribute)
        throws IllegalArgumentException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().deleteAttribute(resourceClass, attribute);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(AttributeDefinition attribute, String name) throws SchemaIntegrityException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setName(attribute, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setDomain(AttributeDefinition attribute, String domain)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setDomain(attribute, domain);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setFlags(AttributeDefinition attribute, int flags)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setFlags(attribute, flags);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public ResourceClass[] getResourceClass()
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().getResourceClass();
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public ResourceClass getResourceClass(long id) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().getResourceClass(id);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public ResourceClass getResourceClass(String name) throws EntityDoesNotExistException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().getResourceClass(name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public ResourceClass createResourceClass(
        String name,
        String javaClass,
        String handlerClass,
        String dbTable,
        int flags)
        throws EntityExistsException, JavaClassException
    {
        coral.setCurrentSession(session);
        try
        {
            return coral.getSchema().createResourceClass(name, javaClass, handlerClass, 
                dbTable, flags);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteResourceClass(ResourceClass resourceClass) throws EntityInUseException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().deleteResourceClass(resourceClass);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(ResourceClass resourceClass, String name) throws EntityExistsException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setName(resourceClass, name);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setFlags(ResourceClass resourceClass, int flags)
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setFlags(resourceClass, flags);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setJavaClass(ResourceClass resourceClass, String javaClass)
        throws JavaClassException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setJavaClass(resourceClass, javaClass);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void setHandlerClass(ResourceClass resourceClass, String handlerClass)
        throws JavaClassException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().setHandlerClass(resourceClass, handlerClass);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void addAttribute(
        ResourceClass resourceClass,
        AttributeDefinition attribute,
        Object value)
        throws SchemaIntegrityException, ValueRequiredException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().addAttribute(resourceClass, attribute, value);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void addParentClass(ResourceClass child, ResourceClass parent, Map attributes)
        throws CircularDependencyException, SchemaIntegrityException, ValueRequiredException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().addParentClass(child, parent, attributes);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteParentClass(ResourceClass child, ResourceClass parent)
        throws IllegalArgumentException
    {
        coral.setCurrentSession(session);
        try
        {
            coral.getSchema().deleteParentClass(child, parent);
        }
        finally
        {
            coral.setCurrentSession(null);
        }
    }
}
