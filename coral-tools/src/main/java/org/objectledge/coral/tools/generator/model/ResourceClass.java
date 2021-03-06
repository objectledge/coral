// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral.tools.generator.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.schema.SchemaIntegrityException;

/**
 * Represents a Coral ResourceClass.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClass.java,v 1.12 2005-02-10 17:46:42 rafal Exp $
 */
public class ResourceClass
    extends Entity
{
    // variables ////////////////////////////////////////////////////////////////////////////////
    
    private String packageName;
    private String implClassName;
    private String interfaceClassName;
    private String handlerClassName;
    
    private String dbTable;
    
    private SortedMap<String, Attribute> attributes = new TreeMap<String, Attribute>();
    private SortedMap<String, ResourceClass> parentClasses = new TreeMap<String, ResourceClass>();
    
    private ResourceClass implParentClass;
    private List<String> attributeOrder;
    
    // constructors /////////////////////////////////////////////////////////////////////////////
    
    /**
     * Protected no-arg constructor, to allow mocking.
     */
    protected ResourceClass()
    {
        // needed by jMock
    }
    
    /**
     * Construcs an instance of the ResourceClass.
     * 
     * @param name the name of the resource class.
     * @param javaClassName the name of the Java class for this resource class.
     * @param handlerClassName the name of the handler class for this resource class.
     * @param dbTable the database table for this resource class.
     * @param flags the flags for this resource class.
     */
    public ResourceClass(String name, String javaClassName, String handlerClassName, String dbTable,
        int flags)
    {
        super(name, flags);
        setJavaClassName(javaClassName);
        setHandlerClassName(handlerClassName);
        setDbTable(dbTable);
    }
    
    // accessors ////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the name of the package of the implementation Java types.
     * 
     * @return the name of the package of the implementation Java types.
     */
    public String getPackageName()
    {
        return packageName;
    }

    /**
     * Returns the name of the implementation Java class for this resource class.
     * 
     * @return the name of the implementation Java class for this resource class.
     */    
    public String getImplClassName()
    {
        return implClassName;
    }
    
    /**
     * Returns the fully qualified name of the implementation Java class for this resource class.
     * 
     * @return the fully qualified name of the implementation Java class for this resource class.
     */    
    public String getFQImplClassName()
    {
        if(packageName.length() > 0)
        {
            return packageName+"."+implClassName;
        }
        else
        {
            return implClassName;
        }
    }    
    
    /**
     * Returns the name of the implementation Java class for this resource class.
     * 
     * @return the name of the implementation Java class for this resource class.
     */    
    public String getInterfaceClassName()
    {
        return interfaceClassName;
    }

    /**
     * Returns the fully qualified name of the interface Java class for this resource class.
     * 
     * @return the fully qualified name of the interface Java class for this resource class.
     */    
    public String getFQInterfaceClassName()
    {
        if(packageName.length() > 0)
        {
            return packageName+"."+interfaceClassName;
        }
        else
        {
            return interfaceClassName;
        }
    }
    
    /**
     * Returns the database table for this resource class.
     * 
     * @return the database table for this resource class.
     */
    public String getDbTable()
    {
        return dbTable;
    }
    
    /**
     * Sets the handler class name for this resource class.
     * 
     * @return the handler class name.
     */
    public String getHandlerClassName()
    {
        return handlerClassName;
    }

    /**
     * Returns the attributes of this resource class.
     * 
     * @return the attributes of this resource class.
     */    
    public List<Attribute> getDeclaredAttributes()
    {
        return sortAttributes(new ArrayList<Attribute>(attributes.values()));
    }

    /**
     * Returns all declared and inherited attributes.
     * 
     * @return all declared and inherited attributes.
     */
    public List<Attribute> getAllAttributes()
    {
        Set<Attribute> result = new TreeSet<Attribute>();
        result.addAll(this.getDeclaredAttributes());
        List<ResourceClass> parents = getAllParentClasses();
        for(Iterator<ResourceClass> i = parents.iterator(); i.hasNext();)
        {
            ResourceClass rc = i.next();
            result.addAll(rc.getDeclaredAttributes());
        }
        return sortAttributes(result);
    }
    
    /**
     * Retrns an attribute delcared by this class or it's ancestor.
     * 
     * @param name name of the attribute.
     * @return the attribute.
     * @throws EntityDoesNotExistException if the attribute is not found.
     */
    public Attribute getAttribute(String name)
        throws EntityDoesNotExistException
    {
        if(attributes.containsKey(name))
        {
            return getDeclaredAttribute(name);
        }
        List<ResourceClass> parents = getAllParentClasses();
        for(Iterator<ResourceClass> i = parents.iterator(); i.hasNext();)
        {
            ResourceClass rc = i.next();
            try
            {
                return rc.getDeclaredAttribute(name);
            }
            catch(EntityDoesNotExistException e)
            {
                continue;
            }
        }
        throw new EntityDoesNotExistException("attribute "+name+" not found");
    }

    /**
     * Returns concrete attributes not inherited from implementation parent.
     * 
     * @return concrete attributes not inherited from implementation parent.
     */
    public List<Attribute> getConcreteImplAttributes()
    {
        List<Attribute> all = getAllAttributes();
        if(getImplParentClass() != null)
        {
            all.removeAll(getImplParentClass().getAllAttributes());
        }
        List<Attribute> result = new ArrayList<Attribute>();
        for(Iterator<Attribute> i = all.iterator(); i.hasNext();)
        {
            Attribute a = i.next();
            if(a.isConcrete())
            {
                result.add(a);
            }
        }
        return sortAttributes(result);
    }
    
    /**
     * Returns concrete required attributes.
     * 
     * @return concrete required attributes.
     */
    public List<Attribute> getConcreteRequiredAttributes()
    {
        List<Attribute> all = getAllAttributes();
        List<Attribute> result = new ArrayList<Attribute>();
        for(Iterator<Attribute> i = all.iterator(); i.hasNext();)
        {
            Attribute a = i.next();
            if(a.isRequired() && a.isConcrete())
            {
                result.add(a);
            }
        }
        return sortAttributes(result);
    }
    
    /**
     * Returns concrete required attributes.
     * 
     * @return concrete required attributes.
     */
    public List<Attribute> getConcreteDeclaredAttributes()
    {
        List<Attribute> all = getDeclaredAttributes();
        List<Attribute> result = new ArrayList<Attribute>();
        for(Iterator<Attribute> i = all.iterator(); i.hasNext();)
        {
            Attribute a = i.next();
            if(a.isConcrete())
            {
                result.add(a);
            }
        }
        return sortAttributes(result);
    }   

    /**
     * Returns the attribute with the given name.
     * 
     * @param attributeName the name of the attribute.
     * @return the attribute definition.
     * @throws EntityDoesNotExistException if the attribute does not exist.
     */
    public Attribute getDeclaredAttribute(String attributeName)
        throws EntityDoesNotExistException
    {
        Attribute result = attributes.get(attributeName);
        if(result == null)
        {
            throw new EntityDoesNotExistException("attribute "+attributeName+" not found");
        }
        return result;
    }
    
    /**
     * Returns the parent classes of this resource class.
     * 
     * @return the parent classes of this resource class.
     */    
    public List<ResourceClass> getDeclaredParentClasses()
    {
        return new ArrayList<ResourceClass>(parentClasses.values());
    }
    
    /**
     * Returns all parent classes of this class.
     * 
     * @return all parent classes of this class.
     */
    public List<ResourceClass> getAllParentClasses()
    {
        Set<ResourceClass> result = new TreeSet<ResourceClass>();
        LinkedList<ResourceClass> stack = new LinkedList<ResourceClass>();
        stack.addLast(this);
        ResourceClass rc;
        while(!stack.isEmpty())
        {
            rc = stack.removeLast();
            result.addAll(rc.getDeclaredParentClasses());
            stack.addAll(rc.getDeclaredParentClasses());
        }
        return new ArrayList<ResourceClass>(result);
    }
    
    /**
     * Returns the implementation parent class.
     * 
     * @return the implementation parent class.
     */
    public ResourceClass getImplParentClass()
    {
        if(implParentClass != null)
        {
            return implParentClass; 
        }
        
        if(parentClasses.size() > 0)
        {
            return (ResourceClass)parentClasses.values().toArray()[0];
        }

        return null;
    }
    
    /**
     * Checks if this class is abstract.
     * 
     * @return <code>true</code> if this class is abstract.
     */
    public boolean isAbstract()
    {
        return hasFlags(ResourceClassFlags.ABSTRACT);
    }
    
    // mutators //////////////////////////////////////////////////////////////

    /**
     * Sets the name of the Java class for this resource class.
     * 
     * @param javaClassName the name of the Java class for this resource class.
     */    
    public void setJavaClassName(String javaClassName)
    {
        int lastDot = javaClassName.lastIndexOf('.');
        if(lastDot >= 0)
        {
            packageName = javaClassName.substring(0, lastDot);
            javaClassName = javaClassName.substring(lastDot+1);
        }
        else
        {
            packageName = "";
        }
        if(javaClassName.endsWith("Impl"))
        {
            implClassName = javaClassName;
            interfaceClassName = javaClassName.substring(0, javaClassName.length()-4);
        }
        else
        {
            interfaceClassName = javaClassName;
            implClassName = javaClassName+"Impl";
        }
    }
    
    /**
     * Sets the database table for this resource class.
     * 
     * @param dbTable the database table for this resource class.
     */
    public void setDbTable(String dbTable)
    {
        this.dbTable = dbTable;
    }
    
    /**
     * Sets the handler class name for this resource class.
     * 
     * @param handlerClassName the handler class name.
     */
    public void setHandlerClassName(String handlerClassName)
    {
        this.handlerClassName = handlerClassName;
    }

    /**
     * Adds an attribute to this resource class.
     * 
     * @param attr an attribute.
     * @throws EntityExistsException if the class already delcares an attribute by that name.
     */    
    public void addAttribute(Attribute attr)
        throws EntityExistsException
    {
        if(attributes.containsKey(attr.getName()))
        {
            throw new EntityExistsException("class "+getName()+" already declares attribute named "+
                attr.getName());
        }
        if(getAllAttributes().contains(attr))
        {
            throw new EntityExistsException("class "+getName()+" already inherits attribute named "+
                attr.getName());            
        }
        attributes.put(attr.getName(), attr);
        attr.setDeclaringClass(this);
    }
    
    /**
     * Removes an attribute from this resource class.
     * 
     * @param attr an attribute.
     * @throws EntityDoesNotExistException if the class does not declare the attribute. 
     */
    public void deleteAttribute(Attribute attr)
        throws EntityDoesNotExistException
    {
        if(!attributes.containsKey(attr.getName()))
        {
            throw new EntityDoesNotExistException("class "+getName()+" does not declare attribute "+
                "named "+attr.getName());
        }
        attributes.remove(attr.getName());
        attr.setDeclaringClass(null);
    }
    
    /**
     * Adds an parent class to this resource class.
     * 
     * @param parentClass a parent class.
     * @throws SchemaIntegrityException if parentClass contains conflicting attributes.
     * @throws CircularDependencyException if parentClass is a descendant of this class.
     */
    public void addParentClass(ResourceClass parentClass)
        throws SchemaIntegrityException, CircularDependencyException
    {
        List<String> conflicting = new ArrayList<String>();
        List<Attribute> newAttributes = parentClass.getAllAttributes();
        SortedMap<String, Attribute> existingAttributes = attributeMap(getAllAttributes());
        for(Iterator<Attribute> i = newAttributes.iterator(); i.hasNext();)
        {
            Attribute attr = i.next();
            Attribute existing = existingAttributes.get(attr.getName());
            if(existing != null && !attr.getDeclaringClass().equals(existing.getDeclaringClass()))
            {
                conflicting.add(attr.getName());
            }
        }
        if(!conflicting.isEmpty())
        {
            throw new SchemaIntegrityException("class "+parentClass.getName()+
                " contains attributes with conficting names "+conflicting.toString());
        }
        if(parentClass.getAllParentClasses().contains(this))
        {
            throw new CircularDependencyException(parentClass.getName()+" is a descendant of "+
                getName());
        }
        parentClasses.put(parentClass.getName(), parentClass);
    }
    
    /**
     * Removes a parent class to this resource class.
     * 
     * @param parentClass a parent class.
     */
    public void deleteParentClass(ResourceClass parentClass)
    {
        parentClasses.remove(parentClass.getName());
    }
    
    /**
     * Sets the implementation parent class.
     * 
     * @param implParentClass the implementation parent class.
     */
    public void setImplParentClass(ResourceClass implParentClass)
    {
        if(implParentClass != null && !getDeclaredParentClasses().contains(implParentClass))
        {
            throw new IllegalArgumentException(implParentClass.getName()+
                " is not a direct parent class of "+getName());
        }
        this.implParentClass = implParentClass;
    }
    
    /**
     * Sets the order in which the attributes should be returned.
     * 
     * @param order the desired order.
     * @throws EntityDoesNotExistException if any of the names in the list does not denote a valid 
     *         attribute.
     */
    public void setAttributeOrder(List<String> order)
        throws EntityDoesNotExistException
    {
        if(order != null)
        {
            for(Iterator<String> i = order.iterator(); i.hasNext(); )
            {
                String name = i.next();
                getAttribute(name);
            }
        }
        attributeOrder = order;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    private List<Attribute> sortAttributes(Collection<Attribute> attributes)
    {
        if(attributeOrder != null && attributeOrder.size() > 0)
        {
            List<Attribute> result = new ArrayList<Attribute>(attributes.size());
            for(Iterator<String> i = attributeOrder.iterator(); i.hasNext();)
            {
                String name = i.next();
                for(Iterator<Attribute> j=attributes.iterator(); j.hasNext();)
                {
                    Attribute attr = j.next();
                    if(attr.getName().equals(name))
                    {
                        j.remove();
                        result.add(attr);
                    }
                }
            }
            result.addAll(attributes);
            return result;
        }
        else
        {
            return new ArrayList<Attribute>(attributes);
        }
    }
    
    private SortedMap<String, Attribute> attributeMap(Collection<Attribute> attributes)
    {
        SortedMap<String, Attribute> result = new TreeMap<String, Attribute>();
        for(Iterator<Attribute> i = attributes.iterator(); i.hasNext();)
        {
            Attribute attr = i.next();
            result.put(attr.getName(), attr);
        }
        return result;
    }
}
