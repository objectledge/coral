package org.objectledge.coral.script;

import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
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
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;

/**
 * Resolves RML AST nodes into ARL entities.
 * 
 * @version $Id: RMLEntityResolver.java,v 1.2 2004-03-18 08:33:48 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class RMLEntityResolver
{
    // member objects ////////////////////////////////////////////////////////

    private CoralSession coralSession;

    // initialization ////////////////////////////////////////////////////////
    
    /**
     * Constructs an {@RMLEntityResolver}.
     *
     * @param coralSession the CoralSession to use.
     */
    public RMLEntityResolver(CoralSession coralSession)
    {
        this.coralSession = coralSession;
    }

    Permission resolve(ASTpermission node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSession.getSecurity().getPermission(node.getId());
        }
        else
        {
            Permission[] items = coralSession.getSecurity().getPermission(node.getName());
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

    Permission[] resolve(ASTpermissionList node)
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

    Role resolve(ASTrole node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSession.getSecurity().getRole(node.getId());
        }
        else
        {
            Role[] items = coralSession.getSecurity().getRole(node.getName());
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

    Role[] resolve(ASTroleList node)
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

    Subject resolve(ASTsubject node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSession.getSecurity().getSubject(node.getId());
        }
        else
        {
            return coralSession.getSecurity().getSubject(node.getName());
        }
    }

    Resource resolve(ASTresource node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSession.getStore().getResource(node.getId());
        }
        else
        {
            Resource[] items = coralSession.getStore().getResourceByPath(node.getName());
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

    ResourceClass resolve(ASTresourceClass node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSession.getSchema().getResourceClass(node.getId());
        }
        else
        {
            return coralSession.getSchema().getResourceClass(node.getName());
        }
    }
    
    ResourceClass[] resolve(ASTresourceClassList node)
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

    AttributeClass resolve(ASTattributeClass node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralSession.getSchema().getAttributeClass(node.getId());
        }
        else
        {
            return coralSession.getSchema().getAttributeClass(node.getName());
        }
    }
}
