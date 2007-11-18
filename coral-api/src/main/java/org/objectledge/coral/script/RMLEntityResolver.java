package org.objectledge.coral.script;

import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.script.parser.ASTattributeClass;
import org.objectledge.coral.script.parser.ASTpermission;
import org.objectledge.coral.script.parser.ASTpermissionList;
import org.objectledge.coral.script.parser.ASTrelation;
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
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;

/**
 * Resolves RML AST nodes into ARL entities.
 * 
 * @version $Id: RMLEntityResolver.java,v 1.7 2007-11-18 21:20:48 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class RMLEntityResolver
{
    // member objects ////////////////////////////////////////////////////////

	private CoralSchema coralSchema;

	private CoralSecurity coralSecurity;
	
	private CoralStore coralStore;
    
    private CoralRelationManager coralRelationManager;
	
    // initialization ////////////////////////////////////////////////////////
    
    /**
     * Constructs an {@RMLEntityResolver}.
     *
     * @param coralSchema the CoralSchema.
     * @param coralSecurity the CoralSecurity.
     * @param coralStore the CoralStore.
     */
    public RMLEntityResolver(CoralSchema coralSchema, CoralSecurity coralSecurity, 
        CoralStore coralStore, CoralRelationManager coralRelationManager)
    {
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
        this.coralStore = coralStore;
        this.coralRelationManager = coralRelationManager;
    }

    /**
     * Resolve Permission from an AST node.
     * 
     * @param node the AST node.
     * @return the Permission.
     * @throws EntityDoesNotExistException if the node does not describe an existing Permissison.
     * @throws AmbigousEntityNameException if multiple Permissions match node's description.
     */
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

    /**
     * Resolve Permission list from an AST node.
     * 
     * @param node the AST node.
     * @return the Permission.
     * @throws EntityDoesNotExistException if the any of the child nodes does not describe an 
     * existing Permission.
     * @throws AmbigousEntityNameException if multiple Permissions match a child node's description.
     */
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

    /**
     * Resolve Role from an AST node.
     * 
     * @param node the AST node.
     * @return the Role.
     * @throws EntityDoesNotExistException if the node does not describe an existing Role.
     * @throws AmbigousEntityNameException if multiple Roles match node's description.
     */
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

    /**
     * Resolve Role list from an AST node.
     * 
     * @param node the AST node.
     * @return the Role.
     * @throws EntityDoesNotExistException if the any of the child nodes does not describe an 
     * existing Role.
     * @throws AmbigousEntityNameException if multiple Roles match a child node's description.
     */
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

    /**
     * Resolve Subject from an AST node.
     * 
     * @param node the AST node.
     * @return the Subject.
     * @throws EntityDoesNotExistException if the node does not describe an existing Subject.
     * @throws AmbigousEntityNameException if multiple Subjects match node's description.
     */
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

    /**
     * Resolve Resource from an AST node.
     * 
     * @param node the AST node.
     * @return the Resource.
     * @throws EntityDoesNotExistException if the node does not describe an existing Resource.
     * @throws AmbigousEntityNameException if multiple Resources match node's description.
     */
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

    /**
     * Resolve ResourceClass from an AST node.
     * 
     * @param node the AST node.
     * @return the ResourceClass.
     * @throws EntityDoesNotExistException if the node does not describe an existing ResourceClass.
     * @throws AmbigousEntityNameException if multiple ResourceClasss match node's description.
     */
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
    
    /**
     * Resolve ResourceClass list from an AST node.
     * 
     * @param node the AST node.
     * @return the ResourceClass.
     * @throws EntityDoesNotExistException if the any of the child nodes does not describe an 
     * existing ResourceClass.
     * @throws AmbigousEntityNameException if multiple ResourceClasss match a child node's 
     * description.
     */
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

    /**
     * Resolve AttributeClass from an AST node.
     * 
     * @param node the AST node.
     * @return the AttributeClass.
     * @throws EntityDoesNotExistException if the node does not describe an existing AttributeClass.
     * @throws AmbigousEntityNameException if multiple AttributeClasss match node's description.
     */
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
    
    /**
     * Resolve Relation from an AST node.
     * 
     * @param node the AST node.
     * @return the Relation.
     * @throws EntityDoesNotExistException if the node does not describe an existing Relation.
     * @throws AmbigousEntityNameException if multiple Relations match node's description.
     */
    public Relation resolve(ASTrelation node)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        if(node.getId() != -1)
        {
            return coralRelationManager.getRelation(node.getId());
        }
        else
        {
            return coralRelationManager.getRelation(node.getName());
        }
    }
}
