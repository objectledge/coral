package org.objectledge.coral.script;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.query.FilteredQueryResults;
import org.objectledge.coral.query.QueryResults;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.script.parser.ASTalterAttributeClassSetDbTableStatement;
import org.objectledge.coral.script.parser.ASTalterAttributeClassSetHandlerClassStatement;
import org.objectledge.coral.script.parser.ASTalterAttributeClassSetJavaClassStatement;
import org.objectledge.coral.script.parser.ASTalterAttributeClassSetNameStatement;
import org.objectledge.coral.script.parser.ASTalterPermissionSetNameStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAddAttributeStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAddPermissionsStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAddSuperclassStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAlterAttributeSetDomainStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAlterAttributeSetFlagsStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAlterAttributeSetNameStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassDeleteAttributeStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassDeletePermissionsStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassDeleteSuperclassStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetDbTableStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetFlagsStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetHandlerClassStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetJavaClassStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetNameStatement;
import org.objectledge.coral.script.parser.ASTalterResourceDeleteAttributeStatement;
import org.objectledge.coral.script.parser.ASTalterResourceSetAttributeStatement;
import org.objectledge.coral.script.parser.ASTalterResourceSetNameStatement;
import org.objectledge.coral.script.parser.ASTalterResourceSetOwnerStatement;
import org.objectledge.coral.script.parser.ASTalterResourceSetParentStatement;
import org.objectledge.coral.script.parser.ASTalterRoleAddSubrolesStatement;
import org.objectledge.coral.script.parser.ASTalterRoleDeleteSubrolesStatement;
import org.objectledge.coral.script.parser.ASTalterRoleSetNameStatement;
import org.objectledge.coral.script.parser.ASTalterSubjectSetNameStatement;
import org.objectledge.coral.script.parser.ASTattribute;
import org.objectledge.coral.script.parser.ASTattributeDefinition;
import org.objectledge.coral.script.parser.ASTattributeDefinitionList;
import org.objectledge.coral.script.parser.ASTattributeFlag;
import org.objectledge.coral.script.parser.ASTattributeFlagList;
import org.objectledge.coral.script.parser.ASTattributeList;
import org.objectledge.coral.script.parser.ASTcreateAttributeClassStatement;
import org.objectledge.coral.script.parser.ASTcreatePermissionStatement;
import org.objectledge.coral.script.parser.ASTcreateResourceClassStatement;
import org.objectledge.coral.script.parser.ASTcreateResourceStatement;
import org.objectledge.coral.script.parser.ASTcreateRoleStatement;
import org.objectledge.coral.script.parser.ASTcreateSubjectStatement;
import org.objectledge.coral.script.parser.ASTdeleteAttributeClassStatement;
import org.objectledge.coral.script.parser.ASTdeletePermissionStatement;
import org.objectledge.coral.script.parser.ASTdeleteResourceClassStatement;
import org.objectledge.coral.script.parser.ASTdeleteResourceStatement;
import org.objectledge.coral.script.parser.ASTdeleteRoleStatement;
import org.objectledge.coral.script.parser.ASTdeleteSubjectStatement;
import org.objectledge.coral.script.parser.ASTechoStatement;
import org.objectledge.coral.script.parser.ASTfindAttributeClassStatement;
import org.objectledge.coral.script.parser.ASTfindGrantsForResourceClassStatement;
import org.objectledge.coral.script.parser.ASTfindGrantsForResourceStatement;
import org.objectledge.coral.script.parser.ASTfindGrantsForRoleStatement;
import org.objectledge.coral.script.parser.ASTfindGrantsForSubjectStatement;
import org.objectledge.coral.script.parser.ASTfindPermissionStatement;
import org.objectledge.coral.script.parser.ASTfindResourceClassStatement;
import org.objectledge.coral.script.parser.ASTfindResourceStatement;
import org.objectledge.coral.script.parser.ASTfindRoleStatement;
import org.objectledge.coral.script.parser.ASTfindRolesStatement;
import org.objectledge.coral.script.parser.ASTfindSubjectStatement;
import org.objectledge.coral.script.parser.ASTfindSubrolesStatement;
import org.objectledge.coral.script.parser.ASTfindSuperrolesStatement;
import org.objectledge.coral.script.parser.ASTgrantPermissionStatement;
import org.objectledge.coral.script.parser.ASTgrantRoleStatement;
import org.objectledge.coral.script.parser.ASTimpersonateStatement;
import org.objectledge.coral.script.parser.ASTresourceClassFlag;
import org.objectledge.coral.script.parser.ASTresourceClassFlagList;
import org.objectledge.coral.script.parser.ASTrevokePermissionStatement;
import org.objectledge.coral.script.parser.ASTrevokeRoleStatement;
import org.objectledge.coral.script.parser.ASTwhoamiStatement;
import org.objectledge.coral.script.parser.DefaultRMLVisitor;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.PermissionAssociation;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.RoleAssignment;
import org.objectledge.coral.security.RoleImplication;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.Resource;
import org.objectledge.utils.StackTrace;

/**
 * Executes RML statemetns.
 *
 */
public class RMLExecutor
    extends DefaultRMLVisitor
{
    // member objects ////////////////////////////////////////////////////////

    /** The coral session. */
    private CoralSession coralSession;
    
    /** The original session (when impersonating). */
    private CoralSession originalSession;
    
    /** The session factory. */
    private CoralSessionFactory coralSessionFactory;

    /** The output buffer. */
    private PrintWriter out;

    /** The entity resolver. */
    private RMLEntityResolver entities;

    // initialization ////////////////////////////////////////////////////////

    /**
     * Constructs an {@RMLExecutor}.
     *
     * @param coralSession the session to use.
     * @param out the output buffer.
     * @param coralSessionFactory the session factory (<code>null</code> to disable impersonation).
     * @throws EntityDoesNotExistException if no subject corresponing to the
     *         <code>principal</code> exists in the system.
     */
    public RMLExecutor(CoralSession coralSession, Writer out, 
        CoralSessionFactory coralSessionFactory)
        throws EntityDoesNotExistException
    {
        this.coralSession = coralSession;
        this.out = new PrintWriter(out);
        this.coralSessionFactory = coralSessionFactory;
        entities = new RMLEntityResolver(coralSession.getSchema(), coralSession.getSecurity(),
        		coralSession.getStore());
    }

    // statements ////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateAttributeClassStatement node, Object data)
    {
        try
        {
            coralSession.getSchema().createAttributeClass(node.getName(),
                node.getJavaClass(), node.getHandlerClass(), node.getDbTable());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindAttributeClassStatement node, Object data)
    {
        try
        {
            AttributeClass[] items;
            if(node.getId() != -1)
            {
                items = new AttributeClass[1];
                items[0] = coralSession.getSchema().getAttributeClass(node.getId());
            }
            else if(node.getName() != null)
            {
                items = new AttributeClass[1];
                items[0] = coralSession.getSchema().getAttributeClass(node.getName());
            }
            else
            {
                items = coralSession.getSchema().getAttributeClass();
            }
            String[][] result = new String[items.length+1][];
            result[0] = new String[] { "Id", "Name", "Java class", "Handler class", "DB table" };
            for(int i=0; i<items.length; i++)
            {
                result[i+1] = new String[5];
                result[i+1][0] = items[i].getIdString();
                result[i+1][1] = items[i].getName();
                result[i+1][2] = items[i].getJavaClass().getName();
                result[i+1][3] = items[i].getHandler().getClass().getName();
                result[i+1][4] = items[i].getDbTable();
            }
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteAttributeClassStatement node, Object data)
    {
        try
        {
            AttributeClass ac = entities.resolve(node.getAttributeClass());
            coralSession.getSchema().deleteAttributeClass(ac);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterAttributeClassSetNameStatement node, Object data)
    {
        try
        {
            AttributeClass ac = entities.resolve(node.getAttributeClass());
            coralSession.getSchema().setName(ac, node.getNewName());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterAttributeClassSetJavaClassStatement node, Object data)
    {
        try
        {
            AttributeClass ac = entities.resolve(node.getAttributeClass());
            coralSession.getSchema().setJavaClass(ac, node.getJavaClass());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterAttributeClassSetHandlerClassStatement node, Object data)
    {
        try
        {
            AttributeClass ac = entities.resolve(node.getAttributeClass());
            coralSession.getSchema().setHandlerClass(ac, node.getHandlerClass());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterAttributeClassSetDbTableStatement node, Object data)
    {
        try
        {
            AttributeClass ac = entities.resolve(node.getAttributeClass());
            coralSession.getSchema().setDbTable(ac, node.getDbTable());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateResourceClassStatement node, Object data)
    {
        try
        {
            int classFlags = parseFlags(node.getFlags());
            ResourceClass rc = coralSession.getSchema().createResourceClass(node.getName(),
                node.getJavaClass(), node.getHandlerClass(), node.getDbTable(), classFlags);
            ResourceClass[] parents = entities.resolve(node.getParents());
            for(int i=0; i<parents.length; i++)
            {
                coralSession.getSchema().addParentClass(rc, parents[i], new HashMap());
            }
            ASTattributeDefinition[] attrs = items(node.getAttributes());
            for(int i=0; i<attrs.length; i++)
            {
                AttributeClass ac = entities.resolve(attrs[i].getAttributeClass());
                int flags = parseFlags(attrs[i].getFlags());
                AttributeDefinition atdef = coralSession.getSchema().
                    createAttribute(attrs[i].getName(), ac, attrs[i].getDomain(), flags);
                coralSession.getSchema().addAttribute(rc, atdef, null);
            }
            Permission[] perms = entities.resolve(node.getPermissions());
            for(int i=0; i<perms.length; i++)
            {
                coralSession.getSecurity().addPermission(rc, perms[i]);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindResourceClassStatement node, Object data)
    {
        try
        {
            String[][] result;
            if(node.getId() != -1 || node.getName() != null)
            {
                try
                {
                    ResourceClass rc;
                    if(node.getId() != -1)
                    {
                        rc = coralSession.getSchema().getResourceClass(node.getId());
                    }
                    else
                    {
                        rc = coralSession.getSchema().getResourceClass(node.getName());
                    }
                    result = new String[2][];
                    result[0] = new String[] 
                    { 
                        "Id", "Name", "Java class", "Handler class", "Flags"
                    };
                    result[1] = new String[5];
                    result[1][0] = rc.getIdString();
                    result[1][1] = rc.getName();
                    result[1][2] = rc.getJavaClassName();
                    result[1][3] = rc.getHandler().getClass().getName();
                    result[1][4] = ResourceClassFlags.toString(rc.getFlags());
                    table(result);
                    ResourceClass[] parents = rc.getParentClasses();
                    List permissions = new ArrayList();
                    if(parents != null && parents.length > 0)
                    {
                        out.println("Parent classes");
                        result = new String[parents.length+1][];
                        result[0] = new String[] { "Id", "Name" };
                        for(int i=0; i<parents.length; i++)
                        {
                            result[i+1] = new String[2];
                            result[i+1][0] = parents[i].getIdString();
                            result[i+1][1] = parents[i].getName();

                            PermissionAssociation[] pas = parents[i].getPermissionAssociations();
                            for(int j=0; j<pas.length; j++)
                            {
                                permissions.add(pas[j]);
                            }
                        }
                        table(result);
                    }
                    AttributeDefinition[] attrs = rc.getAllAttributes();
                    sortAttributes(attrs);
                    if(attrs != null && attrs.length != 0)
                    {
                        out.println("Attributes");
                        result = new String[attrs.length+1][];
                        result[0] = new String[] 
                        { 
                            "Declared by", "Name", "Class", "Domain", "Flags" 
                        };
                        for(int i=0; i<attrs.length; i++)
                        {
                            result[i+1] = new String[5];
                            result[i+1][0] = attrs[i].getDeclaringClass().getName();
                            result[i+1][1] = attrs[i].getName();
                            result[i+1][2] = attrs[i].getAttributeClass().getName();
                            result[i+1][3] = attrs[i].getDomain() != null ?
                                attrs[i].getDomain() : "";
                            result[i+1][4] = AttributeFlags.toString(attrs[i].getFlags());
                        }
                        table(result);
                    }
                    if(permissions.size() > 0)
                    {
                        out.println("Permissions");
                        result = new String[permissions.size()+1][];
                        result[0] = new String[] { "Id", "Permission", "Resource Class" };
                        for(int i=0; i<permissions.size(); i++)
                        {
                            PermissionAssociation pa = (PermissionAssociation)permissions.get(i);
                            result[i+1] = new String[3];
                            result[i+1][0] = pa.getPermission().getIdString();
                            result[i+1][1] = pa.getPermission().getName();
                            result[i+1][2] = pa.getResourceClass().getName();
                        }
                        table(result);
                    }
                }
                catch(EntityDoesNotExistException e)
                {
                    if(node.getName() != null)
                    {
                        out.println("Resource class '"+node.getName()+"' not found");
                    }
                    else
                    {
                        out.println("Resource class #"+node.getId()+" not found");
                    }
                }
            }
            else
            {
                ResourceClass[] items = coralSession.getSchema().getResourceClass();
                sortEntities(items);
                result = new String[items.length+1][];
                result[0] = new String[] { "Id", "Name", "Java class" };
                for(int i=0; i<items.length; i++)
                {
                    result[i+1] = new String[3];
                    result[i+1][0] = items[i].getIdString();
                    result[i+1][1] = items[i].getName();
                    result[i+1][2] = items[i].getJavaClassName();
                }
                table(result);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteResourceClassStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            coralSession.getSchema().deleteResourceClass(rc);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetNameStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            coralSession.getSchema().setName(rc, node.getNewName());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetFlagsStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            coralSession.getSchema().setFlags(rc, parseFlags(node.getFlags()));
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetJavaClassStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            coralSession.getSchema().setJavaClass(rc, node.getJavaClass());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetHandlerClassStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            coralSession.getSchema().setHandlerClass(rc, node.getHandlerClass());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetDbTableStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            coralSession.getSchema().setDbTable(rc, node.getDbTable());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }


    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAlterAttributeSetNameStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            AttributeDefinition attr = rc.getAttribute(node.getAttributeName());
            coralSession.getSchema().setName(attr, node.getNewName());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAlterAttributeSetFlagsStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            AttributeDefinition attr = rc.getAttribute(node.getAttributeName());
            coralSession.getSchema().setFlags(attr, parseFlags(node.getFlags()));
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAlterAttributeSetDomainStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            AttributeDefinition attr = rc.getAttribute(node.getAttributeName());
            coralSession.getSchema().setDomain(attr, node.getDomain());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAddAttributeStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            ASTattributeDefinition attr = node.getAttributeDefinition();
            AttributeClass ac = entities.resolve(attr.getAttributeClass());
            int flags = parseFlags(attr.getFlags());
            AttributeDefinition atdef = coralSession.getSchema().
                createAttribute(attr.getName(), ac, attr.getDomain(), flags);
            coralSession.getSchema().addAttribute(rc, atdef, node.getValue());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassDeleteAttributeStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            AttributeDefinition attr = rc.getAttribute(node.getAttributeName());
            coralSession.getSchema().deleteAttribute(rc, attr);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAddSuperclassStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            ResourceClass sup = entities.resolve(node.getParentClass());
            HashMap values = new HashMap();
            if(node.getValues() != null)
            {
                ASTattribute[] attrDescs = items(node.getValues());
                for(int i=0; i<attrDescs.length; i++)
                {
                    AttributeDefinition atdef = sup.getAttribute(attrDescs[i].getName());
                    values.put(atdef, attrDescs[i].getValue());
                }
            }
            coralSession.getSchema().addParentClass(rc, sup, values);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassDeleteSuperclassStatement node, Object data)
    {
        try
        {
            ResourceClass ac = entities.resolve(node.getResourceClass());
            ResourceClass sup = entities.resolve(node.getParentClass());
            coralSession.getSchema().deleteParentClass(ac, sup);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreatePermissionStatement node, Object data)
    {
        try
        {
            coralSession.getSecurity().createPermission(node.getName());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindPermissionStatement node, Object data)
    {
        try
        {
            Permission[] items;
            if(node.getId() != -1)
            {
                items = new Permission[1];
                items[0] = coralSession.getSecurity().getPermission(node.getId());
            }
            else if(node.getName() != null)
            {
                items = coralSession.getSecurity().getPermission(node.getName());
            }
            else
            {
                items = coralSession.getSecurity().getPermission();
            }
            sortEntities(items);
            String[][] result = new String[items.length+1][];
            result[0] = new String[] { "Id", "Name" };
            for(int i=0; i<items.length; i++)
            {
                result[i+1] = new String[2];
                result[i+1][0] = items[i].getIdString();
                result[i+1][1] = items[i].getName();
            }
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeletePermissionStatement node, Object data)
    {
        try
        {
            Permission p = entities.resolve(node.getPermission());
            coralSession.getSecurity().deletePermission(p);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterPermissionSetNameStatement node, Object data)
    {
        try
        {
            Permission p = entities.resolve(node.getPermission());
            coralSession.getSecurity().setName(p, node.getNewName());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateRoleStatement node, Object data)
    {
        try
        {
            Role role = coralSession.getSecurity().createRole(node.getName());
            Role[] subs = entities.resolve(node.getSubRoles());
            for(int i=0; i<subs.length; i++)
            {
                coralSession.getSecurity().addSubRole(role, subs[i]);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindRoleStatement node, Object data)
    {
        try
        {
            Role[] items;
            if(node.getId() != -1)
            {
                items = new Role[1];
                items[0] = coralSession.getSecurity().getRole(node.getId());
            }
            else if(node.getName() != null)
            {
                items = coralSession.getSecurity().getRole(node.getName());
            }
            else
            {
                items = coralSession.getSecurity().getRole();
            }
            sortEntities(items);
            String[][] result = new String[items.length+1][];
            result[0] = new String[] { "Id", "Name" };
            for(int i=0; i<items.length; i++)
            {
                result[i+1] = new String[2];
                result[i+1][0] = items[i].getIdString();
                result[i+1][1] = items[i].getName();
            }
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteRoleStatement node, Object data)
    {
        try
        {
            Role p = entities.resolve(node.getRole());
            coralSession.getSecurity().deleteRole(p);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRoleSetNameStatement node, Object data)
    {
        try
        {
            Role r = entities.resolve(node.getRole());
            coralSession.getSecurity().setName(r, node.getNewName());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRoleAddSubrolesStatement node, Object data)
    {
        try
        {
            Role role = entities.resolve(node.getRole());
            Role[] subs = entities.resolve(node.getSubRoles());
            for(int i=0; i<subs.length; i++)
            {
                coralSession.getSecurity().addSubRole(role, subs[i]);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRoleDeleteSubrolesStatement node, Object data)
    {
        try
        {
            Role role = entities.resolve(node.getRole());
            Role[] subs = entities.resolve(node.getSubRoles());
            for(int i=0; i<subs.length; i++)
            {
                coralSession.getSecurity().deleteSubRole(role, subs[i]);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindSuperrolesStatement node, Object data)
    {
        try
        {
            Role role = entities.resolve(node.getRole());
            String[][] result;
            if(node.getDirect())
            {
                RoleImplication[] impls = role.getImplications();
                int count = 0;
                for(int i=0; i<impls.length;i++)
                {
                    if(impls[i].getSubRole().equals(role))
                    {
                        count++;
                    }
                }
                result = new String[count+1][];
                int j=0;
                for(int i=0; i<impls.length;i++)
                {
                    if(impls[i].getSubRole().equals(role))
                    {
                        Role sup = impls[i].getSuperRole();
                        result[j+1] = new String[2];
                        result[j+1][0] = sup.getIdString();
                        result[j+1][1] = sup.getName();
                        j++;
                    }
                }
            }
            else
            {
                Role[] sups = role.getSuperRoles();
                result = new String[sups.length+1][];
                for(int i=0; i<sups.length; i++)
                {
                    result[i+1] = new String[2];
                    result[i+1][0] = sups[i].getIdString();
                    result[i+1][1] = sups[i].getName();
                }
            }
            result[0] = new String[] { "Id", "Name" };
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindSubrolesStatement node, Object data)
    {
        try
        {
            Role role = entities.resolve(node.getRole());
            String[][] result;
            if(node.getDirect())
            {
                RoleImplication[] impls = role.getImplications();
                int count = 0;
                for(int i=0; i<impls.length;i++)
                {
                    if(impls[i].getSuperRole().equals(role))
                    {
                        count++;
                    }
                }
                result = new String[count+1][];
                int j=0;
                for(int i=0; i<impls.length;i++)
                {
                    if(impls[i].getSuperRole().equals(role))
                    {
                        Role sub = impls[i].getSubRole();
                        result[j+1] = new String[2];
                        result[j+1][0] = sub.getIdString();
                        result[j+1][1] = sub.getName();
                        j++;
                    }
                }
            }
            else
            {
                Role[] subs = role.getSubRoles();
                result = new String[subs.length+1][];
                for(int i=0; i<subs.length; i++)
                {
                    result[i+1] = new String[2];
                    result[i+1][0] = subs[i].getIdString();
                    result[i+1][1] = subs[i].getName();
                }
            }
            result[0] = new String[] { "Id", "Name" };
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateSubjectStatement node, Object data)
    {
        try
        {
            Subject s = coralSession.getSecurity().createSubject(node.getName());
            Role[] roles = entities.resolve(node.getRoles());
            for(int i=0; i<roles.length; i++)
            {
                coralSession.getSecurity().grant(roles[i], s, false);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindSubjectStatement node, Object data)
    {
        try
        {
            Subject[] items;
            if(node.getId() != -1)
            {
                items = new Subject[1];
                items[0] = coralSession.getSecurity().getSubject(node.getId());
            }
            else if(node.getName() != null)
            {
                items = new Subject[1];
                items[0] = coralSession.getSecurity().getSubject(node.getName());
            }
            else
            {
                items = coralSession.getSecurity().getSubject();
            }
            sortEntities(items);
            String[][] result = new String[items.length+1][];
            result[0] = new String[] { "Id", "Name" };
            for(int i=0; i<items.length; i++)
            {
                result[i+1] = new String[2];
                result[i+1][0] = items[i].getIdString();
                result[i+1][1] = items[i].getName();
            }
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteSubjectStatement node, Object data)
    {
        try
        {
            Subject s = entities.resolve(node.getSubject());
            coralSession.getSecurity().deleteSubject(s);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterSubjectSetNameStatement node, Object data)
    {
        try
        {
            Subject s = entities.resolve(node.getSubject());
            coralSession.getSecurity().setName(s, node.getNewName());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateResourceStatement node, Object data)
    {
        try
        {
            String name = node.getName();
            ResourceClass rc = entities.resolve(node.getResourceClass());
            Resource parent = node.getParent() != null ?
                entities.resolve(node.getParent()) : null;
            HashMap attrs = new HashMap();
            ASTattribute[] attrDescs = items(node.getAttributes());
            for(int i=0; i<attrDescs.length; i++)
            {
                AttributeDefinition atdef = rc.getAttribute(attrDescs[i].getName());
                attrs.put(atdef, attrDescs[i].getValue());
            }
            coralSession.getStore().createResource(name, parent, rc, attrs);
            if(name.indexOf('/') > 0 || name.indexOf('*') > 0)
            {
                out.println("WARNING resource name contains * or / characters.");
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindResourceStatement node, Object data)
    {
        try
        {
            if(node.getResource() != null)
            {
                Resource item = entities.resolve(node.getResource());
                AttributeDefinition[] attrs = item.getResourceClass().getAllAttributes();
                sortAttributes(attrs);
                String[][] result = new String[attrs.length+1][];
                result[0] = new String[] { "Declared by", "Name", "Class", "Domain", "Value" };
                for(int i=0; i<attrs.length; i++)
                {
                    result[i+1] = new String[5];
                    result[i+1][0] = attrs[i].getDeclaringClass().getName();
                    result[i+1][1] = attrs[i].getName();
                    result[i+1][2] = attrs[i].getAttributeClass().getName();
                    result[i+1][3] = attrs[i].getDomain() != null ?
                        attrs[i].getDomain() : "";
                    if(item.isDefined(attrs[i]) && item.get(attrs[i]) != null)
                    {
                        result[i+1][4] = attrs[i].getAttributeClass().
                            getHandler().toPrintableString(item.get(attrs[i]));
                    }
                    else
                    {
                        result[i+1][4] = "undefined";
                    }
                }
                table(result);
            }
            else if(node.getFrom() == null && node.getWhere() == null &&
                    node.getOrderBy() == null && node.getSelect() == null)
            {
                Resource[] items = coralSession.getStore().getResource();
                sortResources(items);
                String[][] result = new String[items.length + 1][];
                result[0] = new String[] { "Id", "Path", "Class", "Owner" };
                for(int i=0; i<items.length; i++)
                {
                    result[i+1] = new String[4];
                    result[i+1][0] = items[i].getIdString();
                    result[i+1][1] = items[i].getPath();
                    result[i+1][2] = items[i].getResourceClass().getName();
                    result[i+1][3] = items[i].getOwner().getName();
                }
                table(result);
            }
            else
            {
                QueryResults rawResults = coralSession.getQuery().executeQuery(node);
                FilteredQueryResults results = rawResults.getFiltered();
                int cols = results.getColumnCount();
                ArrayList temp = new ArrayList();
                String[] heading = new String[cols];
                int i;
                for(i=1; i<=cols; i++)
                {
                    heading[i-1] = results.getColumnName(i);
                }
                temp.add(heading);
                Iterator rows = results.iterator();
                while(rows.hasNext())
                {
                    FilteredQueryResults.Row row = (FilteredQueryResults.Row)rows.next();
                    String[] s = new String[cols];
                    for(i=1; i<=cols; i++)
                    {
                        Object value = row.get(i);
                        s[i-1] = value != null ?
                            results.getColumnType(i).getHandler().toPrintableString(value) :
                            "undefined";
                    }
                    temp.add(s);
                }
                String[][] result = new String[temp.size()][];
                temp.toArray(result);
                table(result);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteResourceStatement node, Object data)
    {
        try
        {
            Resource[] items = null;
            if(node.getId() != -1)
            {
                items = new Resource[1];
                items[0] = coralSession.getStore().getResource(node.getId());
            }
            else if(node.getName() != null)
            {
                items = coralSession.getStore().getResource(node.getName());
            }
            int count = 0;
            for(int i=0; i<items.length; i++)
            {
                if(node.getRecursive())
                {
                    count += coralSession.getStore().deleteTree(items[i]);
                }
                else
                {
                    coralSession.getStore().deleteResource(items[i]);
                    count++;
                }
            }
            if(count > 1)
            {
                out.println("Poof! "+count+" resources deleted. "+
                            "Hope this was what you wanted.");
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceSetNameStatement node, Object data)
    {
        try
        {
            Resource res = entities.resolve(node.getResource());
            String name = node.getNewName();
            coralSession.getStore().setName(res, name);
            if(name.indexOf('/') > 0 || name.indexOf('*') > 0)
            {
                out.println("WARNING resource name contains * or / characters.");
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceSetParentStatement node, Object data)
    {
        try
        {
            Resource res = entities.resolve(node.getResource());
            if(node.getParent() != null)
            {
                Resource parent = entities.resolve(node.getParent());
                coralSession.getStore().setParent(res, parent);
            }
            else
            {
                coralSession.getStore().unsetParent(res);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceSetOwnerStatement node, Object data)
    {
        try
        {
            Resource res = entities.resolve(node.getResource());
            Subject owner = entities.resolve(node.getOwner());
            coralSession.getStore().setOwner(res, owner);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceSetAttributeStatement node, Object data)
    {
        try
        {
            Resource res = entities.resolve(node.getResource());
            AttributeDefinition atdef = res.getResourceClass().
                getAttribute(node.getAttribute().getName());
            res.set(atdef, node.getAttribute().getValue());
            res.update();
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceDeleteAttributeStatement node, Object data)
    {
        try
        {
            Resource res = entities.resolve(node.getResource());
            AttributeDefinition atdef = res.getResourceClass().
                getAttribute(node.getAttributeName());
            res.unset(atdef);
            res.update();
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAddPermissionsStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            Permission[] perms = entities.resolve(node.getPermissions());
            for(int i=0; i<perms.length; i++)
            {
                coralSession.getSecurity().addPermission(rc, perms[i]);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassDeletePermissionsStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            Permission[] perms = entities.resolve(node.getPermissions());
            for(int i=0; i<perms.length; i++)
            {
                coralSession.getSecurity().deletePermission(rc, perms[i]);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindGrantsForResourceClassStatement node, Object data)
    {
        try
        {
            ResourceClass rc = entities.resolve(node.getResourceClass());
            Permission[] items = rc.getPermissions();
            String[][] result = new String[items.length+1][];
            result[0] = new String[] { "Id", "Name" };
            for(int i=0; i<items.length; i++)
            {
                result[i+1] = new String[2];
                result[i+1][0] = items[i].getIdString();
                result[i+1][1] = items[i].getName();
            }
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTgrantRoleStatement node, Object data)
    {
        try
        {
            Subject s = entities.resolve(node.getSubject());
            Role r = entities.resolve(node.getRole());
            coralSession.getSecurity().grant(r, s, node.canGrant());
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTrevokeRoleStatement node, Object data)
    {
        try
        {
            Subject s = entities.resolve(node.getSubject());
            Role r = entities.resolve(node.getRole());
            coralSession.getSecurity().revoke(r, s);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindRolesStatement node, Object data)
    {
        try
        {
            Subject subject = entities.resolve(node.getSubject());
            String[][] result;
            if(node.getDirect())
            {
                RoleAssignment[] roles = subject.getRoleAssignments();
                result = new String[roles.length+1][];
                result[0] = new String[] { "Id", "Name", "Can grant",
                                           "Granted by", "Grant date"};
                for(int i=0; i<roles.length;i++)
                {
                    Role role = roles[i].getRole();
                    result[i+1] = new String[5];
                    result[i+1][0] = role.getIdString();
                    result[i+1][1] = role.getName();
                    result[i+1][2] = roles[i].isGrantingAllowed() ?
                        "yes" : "no";
                    result[i+1][3] = roles[i].getGrantedBy().getName();
                    result[i+1][4] = roles[i].getGrantTime().toString();
                }
            }
            else
            {
                Role[] roles = subject.getRoles();
                result = new String[roles.length+1][];
                result[0] = new String[] { "Id", "Name" };
                for(int i=0; i<roles.length; i++)
                {
                    result[i+1] = new String[2];
                    result[i+1][0] = roles[i].getIdString();
                    result[i+1][1] = roles[i].getName();
                }
            }
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindGrantsForSubjectStatement node, Object data)
    {
        try
        {
            Subject subject = entities.resolve(node.getSubject());
            if(node.getResource() == null)
            {
                ArrayList al = new ArrayList();
                Role[] roles = subject.getRoles();
                for(int i=0; i<roles.length; i++)
                {
                    PermissionAssignment[] paa = roles[i].getPermissionAssignments();
                    for(int j=0; j<paa.length; j++)
                    {
                        al.add(paa[j]);
                    }
                }
                String[][] result = new String[al.size()+1][];
                result[0] = new String[] { "Role", "Resource Id", "Permission",
                                           "Recursive", "Granted by", "Grant date" };
                for(int i=0; i<al.size(); i++)
                {
                    PermissionAssignment pa = (PermissionAssignment)al.get(i);
                    result[i+1] = new String[6];
                    result[i+1][0] = pa.getRole().getName();
                    result[i+1][1] = pa.getResource().getIdString();
                    result[i+1][2] = pa.getPermission().getName();
                    result[i+1][3] = pa.isInherited() ? "yes" : "no";
                    result[i+1][4] = pa.getGrantedBy().getName();
                    result[i+1][5] = pa.getGrantTime().toString();
                }
                table(result);
            }
            else
            {
                Resource res = entities.resolve(node.getResource());
                Permission[] perms = subject.getPermissions(res);
                String[][] result = new String[perms.length+1][];
                result[0] = new String[] { "Id", "Name" };
                for(int i=0; i<perms.length; i++)
                {
                    result[i+1] = new String[2];
                    result[i+1][0] = perms[i].getIdString();
                    result[i+1][1] = perms[i].getName();
                }
                table(result);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindGrantsForRoleStatement node, Object data)
    {
        try
        {
            Role role = entities.resolve(node.getRole());
            if(node.getResource() == null)
            {
                PermissionAssignment[] paa = role.getPermissionAssignments();
                String[][] result = new String[paa.length+1][];
                result[0] = new String[] { "Resource Id", "Permission", "Recursive",
                                           "Granted by", "Grant date"  };
                for(int i=0; i<paa.length; i++)
                {
                    PermissionAssignment pa = paa[i];
                    result[i+1] = new String[5];
                    result[i+1][0] = pa.getResource().getIdString();
                    result[i+1][1] = pa.getPermission().getName();
                    result[i+1][2] = pa.isInherited() ? "yes" : "no";
                    result[i+1][3] = pa.getGrantedBy().getName();
                    result[i+1][4] = pa.getGrantTime().toString();
                }
                table(result);
            }
            else
            {
                Resource res = entities.resolve(node.getResource());
                Permission[] perms = role.getPermissions(res);
                String[][] result = new String[perms.length+1][];
                result[0] = new String[] { "Id", "Name" };
                for(int i=0; i<perms.length; i++)
                {
                    result[i+1] = new String[2];
                    result[i+1][0] = perms[i].getIdString();
                    result[i+1][1] = perms[i].getName();
                }
                table(result);
            }
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTgrantPermissionStatement node, Object data)
    {
        try
        {
            Resource[] res;
            if(node.getResource().getId() != -1)
            {
                res = new Resource[1];
                res[0] = coralSession.getStore().getResource(node.getResource().getId());
            }
            else
            {
                res = coralSession.getStore().getResourceByPath(node.getResource().getName());
            }
            Role role = entities.resolve(node.getRole());
            Permission permission = entities.resolve(node.getPermission());
            boolean recursive = node.getRecursive();
            for(int i=0; i<res.length; i++)
            {
                coralSession.getSecurity().grant(res[i], role, permission, recursive);
            }
            out.println("persmissions on "+res.length+" resources granted");
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTrevokePermissionStatement node, Object data)
    {
        try
        {
            Resource[] res;
            if(node.getResource().getId() != -1)
            {
                res = new Resource[1];
                res[0] = coralSession.getStore().getResource(node.getResource().getId());
            }
            else
            {
                res = coralSession.getStore().getResourceByPath(node.getResource().getName());
            }
            Role role = entities.resolve(node.getRole());
            Permission permission = entities.resolve(node.getPermission());
            for(int i=0; i<res.length; i++)
            {
                coralSession.getSecurity().revoke(res[i], role, permission);
            }
            out.println("persmissions on "+res.length+" resources revoked");
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindGrantsForResourceStatement node, Object data)
    {
        try
        {
            Resource res = entities.resolve(node.getResource());
            PermissionAssignment[] paa = res.getPermissionAssignments();
            String[][] result = new String[paa.length+1][];
            result[0] = new String[] { "Role", "Permission", "Recursive",
                                       "Granted by", "Grant date" };
            for(int i=0; i<paa.length; i++)
            {
                PermissionAssignment pa = paa[i];
                result[i+1] = new String[5];
                result[i+1][0] = pa.getRole().getName();
                result[i+1][1] = pa.getPermission().getName();
                result[i+1][2] = pa.isInherited() ? "yes" : "no";
                result[i+1][3] = pa.getGrantedBy().getName();
                result[i+1][4] = pa.getGrantTime().toString();
            }
            table(result);
        }
        catch(Exception e)
        {
            wrap(e);
        }
        return data;
    }

    //////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTwhoamiStatement node, Object data)
    {
        out.println("You are "+originalSession.getUserSubject().getName());
        if(coralSession != originalSession)
        {
            out.println("Currently impersonating "+coralSession.getUserSubject().getName());
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTimpersonateStatement node, Object data)
    {
        if(node.getSubject() != null)
        {
            try
            {
                Role rootRole = coralSession.getSecurity().getRole(Role.ROOT);
                if(!coralSession.getUserSubject().hasRole(rootRole))
                {
                    out.println("You are not allowed to impersonate other subjects");
                    return data;
                }
                if(coralSessionFactory == null)
                {
                    out.println("Impersonation is disabled");
                    return data;
                }
                Subject subject = entities.resolve(node.getSubject());
                if(originalSession == null)
                {
                    originalSession = coralSession;
                }
                else
                {
                    coralSession.close();
                }
                coralSession = coralSessionFactory.getSession(subject.getPrincipal());
                out.println("You are now impersonating "+subject.getName());
            }
            catch(Exception e)
            {
                wrap(e);
            }
        }
        else
        {
            coralSession.close();
            coralSession = originalSession;
            originalSession = null;
            coralSession.makeCurrent();            
        }
        return data;
    }

    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTechoStatement node, Object data)
    {
        out.println(node.getMessage());
        return data;
    }

    // implementation //////////////////////////////////////////////////////////////////

    /**
     * Wrap up the exception and put in into the output buffer.
     *
     * @param t the exception
     */
    private void wrap(Throwable t)
    {
        out.print(new StackTrace(t));
        out.flush();
    }

    /**
     * Sort an array of AttributeDefinitions, first by declaring class name,
     * and then by attribute name.
     */
    private void sortAttributes(AttributeDefinition[] attrs)
    {
        Comparator comp = new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    AttributeDefinition a1 = (AttributeDefinition)o1;
                    AttributeDefinition a2 = (AttributeDefinition)o2;
                    if(a1.getDeclaringClass().equals(a2.getDeclaringClass()))
                    {
                        return a1.getName().compareTo(a2.getName());
                    }
                    else
                    {
                        return a1.getDeclaringClass().getName().
                            compareTo(a2.getDeclaringClass().getName());
                    }
                }
            };
        Arrays.sort(attrs, comp);
    }

    /**
     * Sort resources by path.
     */
    private void sortResources(Resource[] data)
    {
        Comparator comp = new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    Resource r1 = (Resource)o1;
                    Resource r2 = (Resource)o2;
                    return r1.getPath().compareTo(r2.getPath());
                }
            };
        Arrays.sort(data, comp);
    }

    /**
     * Sort entities by name.
     */
    private void sortEntities(Entity[] data)
    {
        Comparator comp = new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    Entity r1 = (Entity)o1;
                    Entity r2 = (Entity)o2;
                    return r1.getName().compareTo(r2.getName());
                }
            };
        Arrays.sort(data, comp);
    }

    /**
     * Prints out tabular data.
     *
     * @param data the table data, rows are the outer dimension.
     */
    private void table(String[][] data)
    {
        int cols = data[0].length;
        int rows = data.length;
        int[] width = new int[cols];
        for(int i=0; i<rows; i++)
        {
            for(int j=0; j<data[i].length; j++)
            {
                int l = data[i][j] != null ? data[i][j].length() : 0;
                if(l > width[j])
                {
                    width[j] = l;
                }
            }
        }
        if(rows > 0)
        {
            sep(width);
            row(width, data[0]);
            sep(width);
        }
        if(rows > 1)
        {
            for(int i=1; i<rows; i++)
            {
                row(width, data[i]);
            }
            sep(width);
        }
    }

    /**
     * Prints out a character span.
     *
     * @param len the span lenght.
     * @param c the character.
     */
    private void span(int len, char c)
    {
        for(int i=0; i<len; i++)
        {
            out.print(c);
        }
    }

    /**
     * Prints out a table separator composedof + and - characters.
     *
     * @param width the widths of table columns.
     */
    private void sep(int[] width)
    {
        out.print('+');
        for(int i=0; i<width.length; i++)
        {
            span(width[i], '-');
            out.print('+');
        }
        out.println();
    }

    /**
     * Prints out a table row.
     *
     * @param width the widths of table columns.
     * @param data the columns of the row.
     */
    private void row(int[] width, String[] data)
    {
        out.print('|');
        for(int i=0; i<width.length; i++)
        {
            if(i<data.length && data[i] != null)
            {
                out.print(data[i]);
                span(width[i]-data[i].length(), ' ');
            }
            else
            {
                span(width[i], ' ');
            }
            out.print('|');
        }
        out.println();
    }
    
    private int parseFlags(ASTresourceClassFlagList flags)
    {
        StringBuilder buff = new StringBuilder();
        int count = flags.jjtGetNumChildren();
        for(int i=0; i<count; i++)
        {
            ASTresourceClassFlag flag = (ASTresourceClassFlag)flags.jjtGetChild(i);
            buff.append(flag.getValue());
            buff.append(' ');
        }
        if(count > 0)
        {
            buff.setLength(buff.length()-1);
        }
        return ResourceClassFlags.parseFlags(buff.toString());       
    }

    private int parseFlags(ASTattributeFlagList flags)
    {
        StringBuilder buff = new StringBuilder();
        int count = flags.jjtGetNumChildren();
        for(int i=0; i<count; i++)
        {
            ASTattributeFlag flag = (ASTattributeFlag)flags.jjtGetChild(i);
            buff.append(flag.getValue());
            buff.append(' ');
        }
        if(count > 0)
        {
            buff.setLength(buff.length()-1);
        }
        return AttributeFlags.parseFlags(buff.toString());       
    }
    
    private ASTattributeDefinition[] items(ASTattributeDefinitionList list)
    {
        if(list != null)
        {
            ASTattributeDefinition[] result = new ASTattributeDefinition[list.jjtGetNumChildren()];
            for(int i=0; i<list.jjtGetNumChildren(); i++)
            {
                result[i] = (ASTattributeDefinition)list.jjtGetChild(i);
            }
            return result;
        }
        return new ASTattributeDefinition[0];
    }

    private ASTattribute[] items(ASTattributeList list)
    {
        if(list != null)
        {
            ASTattribute[] result = new ASTattribute[list.jjtGetNumChildren()];
            for(int i=0; i<list.jjtGetNumChildren(); i++)
            {
                result[i] = (ASTattribute)list.jjtGetChild(i);
            }
            return result;
        }
        return new ASTattribute[0];
    }
}
