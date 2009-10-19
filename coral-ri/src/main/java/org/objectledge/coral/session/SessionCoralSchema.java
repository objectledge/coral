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
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityFactory;
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
 * @version $Id: SessionCoralSchema.java,v 1.5 2009-01-30 13:43:58 rafal Exp $
 */
public class SessionCoralSchema implements CoralSchema
{
    private CoralCore coral;
    private CoralSessionImpl session;

    /**
     * Creates a session local CoralStore wrapper.
     * 
     * @param coral the coral component hub.
     * @param session the coral session.
     */
    SessionCoralSchema(CoralCore coral, CoralSessionImpl session)
    {
        this.coral = coral;
        this.session = session;
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeClass[] getAttributeClass()
    {
        session.verify();
        return coral.getSchema().getAttributeClass();
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeClass getAttributeClass(long id) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getSchema().getAttributeClass(id);
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeClass getAttributeClass(String name) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getSchema().getAttributeClass(name);
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
        session.verify();
        return coral.getSchema().createAttributeClass(name, javaClass, handlerClass, dbTable);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteAttributeClass(AttributeClass attributeClass) throws EntityInUseException
    {
        session.verify();
        coral.getSchema().deleteAttributeClass(attributeClass);
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(AttributeClass attributeClass, String name) throws EntityExistsException
    {
        session.verify();
        coral.getSchema().setName(attributeClass, name);
    }

    /** 
     * {@inheritDoc}
     */
    public void setJavaClass(AttributeClass attributeClass, String javaClass)
        throws JavaClassException
    {
        session.verify();
        coral.getSchema().setJavaClass(attributeClass, javaClass);
    }

    /** 
     * {@inheritDoc}
     */
    public void setHandlerClass(AttributeClass attributeClass, String handlerClass)
        throws JavaClassException
    {
        session.verify();
        coral.getSchema().setHandlerClass(attributeClass, handlerClass);
    }

    /** 
     * {@inheritDoc}
     */
    public void setDbTable(AttributeClass attributeClass, String dbTable)
    {
        session.verify();
        coral.getSchema().setDbTable(attributeClass, dbTable);
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
        session.verify();
        return coral.getSchema().createAttribute(name, attributeClass, domain, flags);
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeDefinition[] getAttribute()
    {
        session.verify();
        return coral.getSchema().getAttribute();
    }

    /** 
     * {@inheritDoc}
     */
    public AttributeDefinition getAttribute(long id) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getSchema().getAttribute(id);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteAttribute(ResourceClass resourceClass, AttributeDefinition attribute)
        throws IllegalArgumentException
    {
        session.verify();
        coral.getSchema().deleteAttribute(resourceClass, attribute);
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(AttributeDefinition attribute, String name) throws SchemaIntegrityException
    {
        session.verify();
        coral.getSchema().setName(attribute, name);
    }

    /** 
     * {@inheritDoc}
     */
    public void setDomain(AttributeDefinition attribute, String domain)
    {
        session.verify();
        coral.getSchema().setDomain(attribute, domain);
    }

    /** 
     * {@inheritDoc}
     */
    public void setFlags(AttributeDefinition attribute, int flags)
    {
        session.verify();
        coral.getSchema().setFlags(attribute, flags);
    }

    /** 
     * {@inheritDoc}
     */
    public ResourceClass[] getResourceClass()
    {
        session.verify();
        return coral.getSchema().getResourceClass();
    }

    /** 
     * {@inheritDoc}
     */
    public ResourceClass getResourceClass(long id) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getSchema().getResourceClass(id);
    }

    /** 
     * {@inheritDoc}
     */
    public ResourceClass getResourceClass(String name) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getSchema().getResourceClass(name);
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
        session.verify();
        return coral.getSchema().createResourceClass(name, javaClass, handlerClass, 
                dbTable, flags);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteResourceClass(ResourceClass resourceClass) throws EntityInUseException
    {
        session.verify();
        coral.getSchema().deleteResourceClass(resourceClass);
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(ResourceClass resourceClass, String name) throws EntityExistsException
    {
        session.verify();
        coral.getSchema().setName(resourceClass, name);
    }

    /** 
     * {@inheritDoc}
     */
    public void setFlags(ResourceClass resourceClass, int flags)
    {
        session.verify();
        coral.getSchema().setFlags(resourceClass, flags);
    }

    /** 
     * {@inheritDoc}
     */
    public void setJavaClass(ResourceClass resourceClass, String javaClass)
        throws JavaClassException
    {
        session.verify();
        coral.getSchema().setJavaClass(resourceClass, javaClass);
    }

    /** 
     * {@inheritDoc}
     */
    public void setHandlerClass(ResourceClass resourceClass, String handlerClass)
        throws JavaClassException
    {
        session.verify();
        coral.getSchema().setHandlerClass(resourceClass, handlerClass);
    }

    /** 
     * {@inheritDoc}
     */
    public void setDbTable(ResourceClass resourceClass, String dbTable)
    {
        session.verify();
        coral.getSchema().setDbTable(resourceClass, dbTable);
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
        session.verify();
        coral.getSchema().addAttribute(resourceClass, attribute, value);
    }

    /** 
     * {@inheritDoc}
     */
    public void addParentClass(ResourceClass child, ResourceClass parent, Map attributes)
        throws CircularDependencyException, SchemaIntegrityException, ValueRequiredException
    {
        session.verify();
        coral.getSchema().addParentClass(child, parent, attributes);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteParentClass(ResourceClass child, ResourceClass parent)
        throws IllegalArgumentException
    {
        session.verify();
        coral.getSchema().deleteParentClass(child, parent);
    }

    @Override
    public EntityFactory<AttributeClass<?>> getAttributeClassFactory()
    {
        session.verify();
        return coral.getSchema().getAttributeClassFactory();
    }

    @Override
    public EntityFactory<AttributeDefinition<?>> getAttributeDefinitionFactory()
    {
        session.verify();
        return coral.getSchema().getAttributeDefinitionFactory();
    }

    @Override
    public EntityFactory<ResourceClass> getResourceClassFactory()
    {
        session.verify();
        return coral.getSchema().getResourceClassFactory();
    }
}
