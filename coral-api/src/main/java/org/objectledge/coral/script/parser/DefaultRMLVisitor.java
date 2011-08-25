package org.objectledge.coral.script.parser;

/**
 * Basic implementation of the visitor that simply traverses the whole tree.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: DefaultRMLVisitor.java,v 1.4 2006-03-03 10:59:52 rafal Exp $
 */
public class DefaultRMLVisitor
    implements RMLVisitor
{
    /**
     * {@inheritDoc}
     */
    public Object visit(SimpleNode node, Object data)
    {
        data = node.childrenAccept(this, data);
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateAttributeClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindAttributeClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTattributeClass node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteAttributeClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterAttributeClassSetNameStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterAttributeClassSetJavaClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterAttributeClassSetHandlerClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterAttributeClassSetDbTableStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTresourceClass node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTresourceClassList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTpermission node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTpermissionList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTattributeFlag node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTattributeFlagList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTresourceClassFlag node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTresourceClassFlagList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTattributeDefinition node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTattributeDefinitionList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateResourceClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindResourceClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteResourceClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetNameStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetFlagsStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAlterAttributeSetNameStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAlterAttributeSetFlagsStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAlterAttributeSetDomainStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetJavaClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetHandlerClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassSetDbTableStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAddAttributeStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassDeleteAttributeStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAddSuperclassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassDeleteSuperclassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreatePermissionStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindPermissionStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeletePermissionStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterPermissionSetNameStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTrole node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTroleList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateRoleStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindRoleStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteRoleStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRoleSetNameStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRoleAddSubrolesStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRoleDeleteSubrolesStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindSuperrolesStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindSubrolesStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTsubject node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateSubjectStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindSubjectStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteSubjectStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterSubjectSetNameStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTresource node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTattribute node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTattributeList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateResourceStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindResourceStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteResourceStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceSetNameStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceSetParentStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceSetOwnerStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceSetAttributeStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceDeleteAttributeStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassAddPermissionsStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterResourceClassDeletePermissionsStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindGrantsForResourceClassStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTgrantRoleStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTrevokeRoleStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindRolesStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindGrantsForSubjectStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindGrantsForRoleStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTgrantPermissionStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTrevokePermissionStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindGrantsForResourceStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTwhoamiStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTimpersonateStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTechoStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTscript node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTsingleStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    // resource query grammar

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTconditionalExpression node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTorExpression node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTandExpression node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTnotExpression node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdefinedCondition node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTequalityCondition node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcomparisonCondition node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTapproximationCondition node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTclassAndAliasSpecifier node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTclassAndAliasSpecifierList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTorderBySpecifier node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTorderByList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTselectSpecifier node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTselectList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }
    
    /**
     * {@inheritDoc}
     */
    public Object visit(ASTrelation node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTresourcePair node, Object data)
    {
        return visit((SimpleNode)node, data);
    }
    
    /**
     * {@inheritDoc}
     */
    public Object visit(ASTresourcePairList node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTcreateRelationStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }
    
    /**
     * {@inheritDoc}
     */
    public Object visit(ASTfindRelationStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }
    
    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRelationSetNameStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRelationAddPairsStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRelationDeletePairsStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRelationDeleteForwardStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRelationDeleteReverseStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTalterRelationDeleteAllStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTdeleteRelationStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }
    
    public Object visit(ASTcopyResourceStatement node, Object data)
    {
        return visit((SimpleNode)node, data);
    }
}

