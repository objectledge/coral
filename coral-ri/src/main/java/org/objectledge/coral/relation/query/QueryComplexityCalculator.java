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

import java.util.Iterator;
import java.util.Set;

import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.relation.ResourceIdentifierResolver;
import org.objectledge.coral.relation.query.parser.ASTIntersectionExpression;
import org.objectledge.coral.relation.query.parser.ASTRelationMapExpression;
import org.objectledge.coral.relation.query.parser.ASTResourceIdentifierId;
import org.objectledge.coral.relation.query.parser.ASTResourceIdentifierPath;
import org.objectledge.coral.relation.query.parser.ASTSumExpression;
import org.objectledge.coral.relation.query.parser.ASTTransitiveRelationMapExpression;
import org.objectledge.coral.relation.query.parser.SimpleNode;

/**
 * Complexity calculator calculates a parsed query complexity and returns the estimated number of
 * operations needed to perform a query.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: QueryComplexityCalculator.java,v 1.1 2004-02-24 17:10:15 zwierzem Exp $
 */
public class QueryComplexityCalculator extends AbstractQueryVisitor
{
	private int externalSetSize;
	/**
	 * {@inheritDoc}
	 */
    public QueryComplexityCalculator(
        CoralRelationManager relationManager,
        ResourceIdentifierResolver resolver,
        int externalSetSize)
    {
        super(relationManager, resolver);
		this.externalSetSize = externalSetSize;
    }

	/**
	 * Visits the sum expression node and calculates the sum operation complexity by summing
	 * complexities coming from subexpressions.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting complexity {@link Integer} object
	 */
	public Object doVisit(ASTSumExpression node, Object data)
	{
		int numChildren = node.jjtGetNumChildren();
		int complexity = 0;
		for(int i=1; i<numChildren; i++)
		{
			Integer right = (Integer)(node.jjtGetChild(i).jjtAccept(this, data));
			complexity += right.intValue();
		}
		return new Integer(complexity);
	}

	/**
	 * Visits the intersection expression node and calculates the intersection operation complexity
	 * by summing complexities coming from subexpressions but leaving one out.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting complexity {@link Integer} object
	 */
	public Object doVisit(ASTIntersectionExpression node, Object data)
	{
		int numChildren = node.jjtGetNumChildren();
		Integer left = (Integer)(node.jjtGetChild(0).jjtAccept(this, data));
		int complexity = left.intValue();
		for(int i=1; i<numChildren; i++)
		{
			Integer right = (Integer)(node.jjtGetChild(i).jjtAccept(this, data));
			complexity += right.intValue();
		}
		float complexity2 = (float)complexity / (float)numChildren;
		return new Integer((int)(complexity2*(float)(numChildren-1)));
	}

	/**
	 * {@inheritDoc}
	 */
	public Object doVisit(ASTRelationMapExpression node, Object data, Relation relation)
	{
		int mapSetComplexity = getMapSetComplexity(node, data);
		int mappingComplexity = (int)(relation.getAvgMappingSize() * ((float)mapSetComplexity));
		return new Integer(mappingComplexity);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object doVisit(ASTTransitiveRelationMapExpression node, Object data, Relation relation)
	{
		int mapSetComplexity = getMapSetComplexity(node, data);
		float avgMappingSize = relation.getAvgMappingSize();
		int mappingComplexity =
			(int)(avgMappingSize * Math.log(avgMappingSize) * ((float)mapSetComplexity));
		return new Integer(mappingComplexity);
	}

	/**
	 * Visits the identifier node and returns the size of the set resolved from this identifier.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting complexity {@link Integer} object
	 */
	public Object visit(ASTResourceIdentifierId node, Object data)
	{
		return getSize(node.getIdentifier());
	}

	/**
	 * Visits the identifier node and returns the size of the set resolved from this identifier.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting complexity {@link Integer} object
	 */
	public Object visit(ASTResourceIdentifierPath node, Object data)
	{
		return getSize(node.getIdentifier());
	}

	// implementation -------------------------------------------------------------------------

	private int getMapSetComplexity(SimpleNode node, Object data)
	{
		return ((Integer) node.jjtGetChild(1).jjtAccept(this, data)).intValue();
	}

	private void buildTransitiveSet(Relation relation, Long id, Set mapSet, Set resultSet)
	{
		Set localResult = relation.get(id.longValue()); 
		for (Iterator iter = localResult.iterator(); iter.hasNext();)
		{
			Long localId = (Long) iter.next();
			if(mapSet.contains(localId))
			{
				throw new RuntimeException("circular relation '"+relation.getName()
					+"' encountered in transitive mapping operation");
			}
			buildTransitiveSet(relation, localId, mapSet, resultSet);
		}
		resultSet.addAll(localResult);
	}

	private Integer getSize(String identifier)
	{
		int size = setSize(identifier);
		if(externalSetSize > -1)
		{
			size = (size < externalSetSize) ? size : externalSetSize;
		}
		return new Integer(size);
	}

	private int setSize(String identifier)
	{
		int size = 0;
		try
		{
			Set set = resolver.resolveIdentifier(identifier);
			size += set.size();
		}
		catch(Exception e)
		{
			// go on with a 0 size
		}
		return size;
	}
}
