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

import java.util.HashSet;
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

import bak.pcj.LongIterator;
import bak.pcj.set.LongOpenHashSet;
import bak.pcj.set.LongSet;

/**
 * Query executor executes a parsed query and returns the set of queried {@link 
 * org.objectledge.coral.store.Resource} ids.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: QueryExecutor.java,v 1.7 2005-04-01 12:32:12 zwierzem Exp $
 */
public class QueryExecutor extends AbstractQueryVisitor
{
	/**
	 * {@inheritDoc}
	 */
	public QueryExecutor(CoralRelationManager relationManager, ResourceIdentifierResolver resolver)
	{
		super(relationManager, resolver);
	}

	/**
	 * Visits the sum expression node and performs set sum operation, the {@link IdSet} coming from
	 * leftmost subexpression is modified and passed as sum result.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting {@link IdSet} 
	 */
	public Object doVisit(ASTSumExpression node, Object data)
	{
		int numChildren = node.jjtGetNumChildren();
		IdSet left = (IdSet)(node.jjtGetChild(0).jjtAccept(this, data));
		for(int i=1; i<numChildren; i++)
		{
			IdSet right = (IdSet)(node.jjtGetChild(i).jjtAccept(this, data));
			left.sum(right);
		}
		return left;
	}

	/**
	 * Visits the intersection expression node and performs set intersection operation,
	 * the {@link IdSet} coming from leftmost subexpression is modified and passed as intersection
	 * result.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting {@link IdSet} 
	 */
	public Object doVisit(ASTIntersectionExpression node, Object data)
	{
		int numChildren = node.jjtGetNumChildren();
		IdSet left = (IdSet)(node.jjtGetChild(0).jjtAccept(this, data));
		for(int i=1; i<numChildren; i++)
		{
			IdSet right = (IdSet)(node.jjtGetChild(i).jjtAccept(this, data));
			left.intersect(right);
		}
		return left;
	}

	/**
	 * Visits the identifier node and retrieves a set of resource ids using the resolver. The
	 * retrieved {@link IdSet} is returned up the tree.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting {@link IdSet} 
	 */
	public Object visit(ASTResourceIdentifierId node, Object data)
	{
		return resolve(node.getIdentifier());
	}

	/**
	 * Visits the identifier node and retrieves a set of resource ids using the resolver. The
	 * retrieved {@link IdSet} is returned up the tree.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting {@link IdSet} 
	 */
	public Object visit(ASTResourceIdentifierPath node, Object data)
	{
		return resolve(node.getIdentifier());
	}

	/**
	 * Visits the relation mapping node and retrieves a set of resource ids from the relation using
	 * a provided set of resource ids from the subquery. The retrieved {@link IdSet} is returned up
	 * the tree.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @param relation defined for this mapping node
	 * @return the resulting {@link IdSet} 
	 */
	public Object doVisit(ASTRelationMapExpression node, Object data, Relation relation)
	{
		LongSet mapSet = getMapSet(node, data).getSet();
		LongSet resultSet = new LongOpenHashSet(mapSet.size());
		for (LongIterator iter = mapSet.iterator(); iter.hasNext();)
        {
			resultSet.addAll(relation.get(iter.next()));
        }
		return new IdSet(resultSet);
	}

	/**
	 * Visits the relation mapping node and recursively retrieves a set of resource ids from the 
	 * relation (by assuming relation is transitive but not circular) using a provided set of
	 * resource ids from the subquery. The retrieved {@link IdSet} is returned up the tree.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @param relation defined for this mapping node
	 * @return the resulting {@link IdSet} 
	 */
	public Object doVisit(ASTTransitiveRelationMapExpression node, Object data, Relation relation)
	{
		LongSet mapSet = getMapSet(node, data).getSet();
		LongSet resultSet = new LongOpenHashSet(mapSet.size());
		for (LongIterator iter = mapSet.iterator(); iter.hasNext();)
		{
			buildTransitiveSet(relation, iter.next(), mapSet, resultSet);
		}
		return new IdSet(resultSet);
	}

	// implementation -------------------------------------------------------------------------

	private IdSet getMapSet(SimpleNode node, Object data)
	{
		return (IdSet) node.jjtGetChild(1).jjtAccept(this, data);
	}

	private void buildTransitiveSet(Relation relation, long id, LongSet mapSet, LongSet resultSet)
	{
		LongSet localResult = relation.get(id); 
		for (LongIterator iter = localResult.iterator(); iter.hasNext();)
        {
            long localId = iter.next();
			if(mapSet.contains(localId))
			{
				throw new RuntimeException("circular relation '"+relation.getName()
					+"' encountered in transitive mapping operation");
			}
			buildTransitiveSet(relation, localId, mapSet, resultSet);
        }
		resultSet.addAll(localResult);
	}

	/**
	 * Resolves a given identifier using the resolver.
	 * 
	 * @param identifier identifier to be resolved.
	 * @return a set of resolved ids 
	 */
	protected IdSet resolve(String identifier)
	{
		try
		{
			LongSet set = resolver.resolveIdentifier(identifier);
			return new IdSet(set);
		}
		catch(Exception e)
		{
			return new IdSet(new LongOpenHashSet());
		}
	}
}
