// 
// Copyright (c) 2003, 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
//   
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//   
// * Redistributions of source code must retain the above copyright notice,  
// this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
// this list of conditions and the following disclaimer in the documentation  
// and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
// nor the names of its contributors may be used to endorse or promote products  
// derived from this software without specific prior written permission. 
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
package org.objectledge.coral.relation.query;

import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.relation.ResourceIdentifierResolver;
import org.objectledge.coral.relation.query.parser.ASTIntersectionExpression;
import org.objectledge.coral.relation.query.parser.ASTInvertedRelationExpression;
import org.objectledge.coral.relation.query.parser.ASTRelationMapExpression;
import org.objectledge.coral.relation.query.parser.ASTRelationName;
import org.objectledge.coral.relation.query.parser.ASTStart;
import org.objectledge.coral.relation.query.parser.ASTSumExpression;
import org.objectledge.coral.relation.query.parser.ASTTransitiveRelationMapExpression;
import org.objectledge.coral.relation.query.parser.RelationQueryParserVisitor;
import org.objectledge.coral.relation.query.parser.SimpleNode;

/**
 * Base class for all Query tree visitors.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: AbstractQueryVisitor.java,v 1.5 2004-03-09 14:33:48 zwierzem Exp $
 */
public abstract class AbstractQueryVisitor implements RelationQueryParserVisitor
{
	/** Relation manager is used to retrieve relations. */
	protected CoralRelationManager relationManager;
	/** Resolver used to retrieve resource ids. */
	protected ResourceIdentifierResolver resolver;
	/** Number o mappings over currently visited node. */
	protected int parentMappings;

	/**
	 * Creates a new instance of query visitor. 
	 *
	 * @param relationManager relation manager is used to retrieve relations 
	 * @param resolver resolver used to retrieve resource ids
	 */
    public AbstractQueryVisitor(
        CoralRelationManager relationManager,
        ResourceIdentifierResolver resolver)
	{
		this.relationManager = relationManager;
		this.resolver = resolver;
		parentMappings = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object visit(SimpleNode node, Object data)
	{
		if(node.jjtGetNumChildren() == 1)
		{
			return node.jjtGetChild(0).jjtAccept(this, data);
		}
		else
		{
			throw new Error("SimpleNode cannot have more than one child");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object visit(ASTStart node, Object data)
	{
		return visit((SimpleNode)node, data);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object visit(ASTInvertedRelationExpression node, Object data)
	{
		int numChildren = node.jjtGetNumChildren();
		if(numChildren != 1)
		{
			throw new Error("InvertedRelationExpression must have only 1 child");
		}
		Relation relation = (Relation)(node.jjtGetChild(0).jjtAccept(this, data));
		return relation.getInverted();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object visit(ASTRelationName node, Object data)
	{
		try
		{
			return relationManager.getRelation(node.getRelationName());
		}
		catch (EntityDoesNotExistException e)
		{
			throw new RuntimeException("Relation '"+node.getRelationName()+"' does not exist");
		}
		catch (AmbigousEntityNameException e)
        {
			throw new RuntimeException(
				"There are many relations named '"+node.getRelationName()+"'");
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public Object visit(ASTSumExpression node, Object data)
	{
		int numChildren = node.jjtGetNumChildren();
		if(numChildren < 2)
		{
			throw new Error("SumExpression must have at least 2 children");
		}
		return doVisit(node, data);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object visit(ASTIntersectionExpression node, Object data)
	{
		int numChildren = node.jjtGetNumChildren();
		if(numChildren < 2)
		{
			throw new Error("IntersectionExpression must have at least 2 children");
		}
		return doVisit(node, data);
	}

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTRelationMapExpression node, Object data)
    {
		parentMappings++;
		int numChildren = node.jjtGetNumChildren();
		if(numChildren != 2)
		{
			throw new Error("RelationMapExpression must have 2 children");
		}
		Relation relation = getRelation(node, data);
		Object result = doVisit(node, data, relation);
		parentMappings--;
		return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object visit(ASTTransitiveRelationMapExpression node, Object data)
    {
    	parentMappings++;
		int numChildren = node.jjtGetNumChildren();
		if(numChildren != 2)
		{
			throw new Error("RelationMapExpression must have 2 children");
		}
		Relation relation = getRelation(node, data);
        Object result = doVisit(node, data, relation);
		parentMappings--;
		return result;
    }

	// methods to be overriden --------------------------------------------------------------------

	/**
	 * Performs {@link #doVisit(ASTSumExpression,Object)} logic in subclasses.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the visit results 
	 */
	public abstract Object doVisit(ASTSumExpression node, Object data);

	/**
	 * Performs {@link #doVisit(ASTIntersectionExpression,Object)} logic in subclasses.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the visit results 
	 */
	public abstract Object doVisit(ASTIntersectionExpression node, Object data);

	/**
	 * Performs {@link #doVisit(ASTRelationMapExpression,Object)} logic in subclasses.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @param relation defined for this mapping node
	 * @return the visit results 
	 */
    public abstract Object doVisit(ASTRelationMapExpression node, Object data, Relation relation);

	/**
	 * Performs {@link #doVisit(ASTTransitiveRelationMapExpression,Object)} logic in subclasses.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @param relation defined for this mapping node
	 * @return the visit results 
	 */
    public abstract Object doVisit(
        ASTTransitiveRelationMapExpression node,
        Object data,
        Relation relation);
	
	// implementation -----------------------------------------------------------------------------

	private Relation getRelation(SimpleNode node, Object data)
	{
		return (Relation) node.jjtGetChild(0).jjtAccept(this, data);
	}
}
