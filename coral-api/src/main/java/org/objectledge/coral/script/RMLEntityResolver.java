package org.objectledge.coral.script;

import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.script.parser.ASTattributeClass;
import org.objectledge.coral.script.parser.ASTpermission;
import org.objectledge.coral.script.parser.ASTpermissionList;
import org.objectledge.coral.script.parser.ASTresource;
import org.objectledge.coral.script.parser.ASTresourceClass;
import org.objectledge.coral.script.parser.ASTresourceClassList;
import org.objectledge.coral.script.parser.ASTrole;
import org.objectledge.coral.script.parser.ASTroleList;
import org.objectledge.coral.script.parser.ASTsubject;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;

/**
 * Resolves RML AST nodes into ARL entities.
 * 
 * @version $Id: RMLEntityResolver.java,v 1.3 2004-08-27 11:29:23 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class RMLEntityResolver
{
    // member objects ////////////////////////////////////////////////////////

	private CoralSchema coralSchema;

	private CoralSecurity coralSecurity;
	
	private CoralStore coralStore;
	
    // initialization ////////////////////////////////////////////////////////
    
    /**
     * Constructs an {@RMLEntityResolver}.
     *
     * @param coralSession the CoralSession to use.
     */
    public RMLEntityResolver(CoralSchema coralSchema, CoralSecurity coralSecurity, CoralStore coralStore)
    {
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
        this.coralStore = coralStore;
    }

    public Permission resolve(ASTpermission node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSecurity.getPermission(node.getId());
        }
        else
        {
            Permission[] items = coralSecurity.getPermission(node.getName());
            if(items.length == 0)
            {
                throw new EntityDoesNotExistException("permission '"+node.getName()+
                                                      "' not found");
            }
            else if(items.length > 1)
            {
                throw new AmbigousEntityNameException("permission name '"+node.getName()+
                                                "' is ambigous");
            }
            return items[0];
        }
    }

    public Permission[] resolve(ASTpermissionList node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node == null)
        {
            return new Permission[0];
        }
        Permission[] result = new Permission[node.jjtGetNumChildren()];
        for(int i=0; i<result.length; i++)
        {
            result[i] = resolve((ASTpermission)node.jjtGetChild(i));
        }
        return result;
    }

    public Role resolve(ASTrole node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSecurity.getRole(node.getId());
        }
        else
        {
            Role[] items = coralSecurity.getRole(node.getName());
            if(items.length == 0)
            {
                throw new EntityDoesNotExistException("role '"+node.getName()+
                                                      "' not found");
            }
            else if(items.length > 1)
            {
                throw new AmbigousEntityNameException("role name '"+node.getName()+
                                                "' is ambigous");
            }
            return items[0];
        }
    }

    public Role[] resolve(ASTroleList node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node == null)
        {
            return new Role[0];
        }
        Role[] result = new Role[node.jjtGetNumChildren()];
        for(int i=0; i<result.length; i++)
        {
            result[i] = resolve((ASTrole)node.jjtGetChild(i));
        }
        return result;
    }

    public Subject resolve(ASTsubject node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSecurity.getSubject(node.getId());
        }
        else
        {
            return coralSecurity.getSubject(node.getName());
        }
    }

    public Resource resolve(ASTresource node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralStore.getResource(node.getId());
        }
        else
        {
            Resource[] items = coralStore.getResourceByPath(node.getName());
            if(items.length == 0)
            {
                throw new EntityDoesNotExistException("resource '"+node.getName()+
                                                      "' not found");
            }
            else if(items.length > 1)
            {
                throw new AmbigousEntityNameException("resource name '"+node.getName()+
                                                "' is ambigous");
            }
            return items[0];
        }
    }

    public ResourceClass resolve(ASTresourceClass node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSchema.getResourceClass(node.getId());
        }
        else
        {
            return coralSchema.getResourceClass(node.getName());
        }
    }
    
    public ResourceClass[] resolve(ASTresourceClassList node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node == null)
        {
            return new ResourceClass[0];
        }
        ResourceClass[] result = new ResourceClass[node.jjtGetNumChildren()];
        for(int i=0; i<result.length; i++)
        {
            result[i] = resolve((ASTresourceClass)node.jjtGetChild(i));
        }
        return result;
    }

    public AttributeClass resolve(ASTattributeClass node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSchema.getAttributeClass(node.getId());
        }
        else
        {
            return coralSchema.getAttributeClass(node.getName());
        }
    }
}
