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

import java.util.Set;

import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.relation.ResourceIdentifierResolver;
import org.objectledge.coral.relation.query.parser.ASTRelationMapExpression;
import org.objectledge.coral.relation.query.parser.ASTResourceIdentifierId;
import org.objectledge.coral.relation.query.parser.ASTResourceIdentifierPath;
import org.objectledge.coral.relation.query.parser.ASTTransitiveRelationMapExpression;

/**
 * This kind of a query executor executes a parsed query and while executing it, it intersects
 * resoruce id sets, provided by first level mapping nodes and resource resolver nodes without any
 * mapping node above, with a provided set to decrease the complexity of set operations (sum and
 * intersection).
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: UnmappedLeafIntersectQueryExecutor.java,v 1.1 2004-02-25 10:57:20 zwierzem Exp $
 */
public class UnmappedLeafIntersectQueryExecutor extends QueryExecutor
{
	private IdSet initialIdSet;

    /**
     * Constructs the query executor.
     * 
	 * @param relationManager relation manager is used to retrieve relations 
	 * @param resolver resolver used to retrieve resource ids
     * @param initialIdSet used to intersect with query results on the "leaf" level 
     */
    public UnmappedLeafIntersectQueryExecutor(
        CoralRelationManager relationManager,
        ResourceIdentifierResolver resolver,
		Set initialIdSet)
    {
        super(relationManager, resolver);
		this.initialIdSet = new IdSet(initialIdSet);
	}

	/**
	 * Visits the relation mapping node and retrieves a set of resource ids from the relation using
	 * a provided set of resource ids from the subquery. Then if current node does not have any 
	 * mapping nodes above the set is intersected with provided id set. The resulting {@link IdSet}
	 * is returned up the tree.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @param relation defined for this mapping node
	 * @return the resulting {@link IdSet} 
	 */
    public Object doVisit(ASTRelationMapExpression node, Object data, Relation relation)
    {
        IdSet set = (IdSet) super.doVisit(node, data, relation);
        if(this.parentMappings == 1) // this is the "leaf" mapping
        {
			set.intersect(initialIdSet);
		}
		return set;
    }

	/**
	 * Visits the relation mapping node and recursively retrieves a set of resource ids from the 
	 * relation (by assuming relation is transitive but not circular) using a provided set of
	 * resource ids from the subquery. Then if current node does not have any mapping nodes above 
	 * the set is intersected with provided id set. The retrieved {@link IdSet} is returned up 
	 * the tree.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @param relation defined for this mapping node
	 * @return the resulting {@link IdSet} 
	 */
    public Object doVisit(ASTTransitiveRelationMapExpression node, Object data, Relation relation)
    {
		IdSet set = (IdSet) super.doVisit(node, data, relation);
		if(this.parentMappings == 1) // this is the "leaf" mapping
		{
			set.intersect(initialIdSet);
		}
		return set;
    }

	/**
	 * Visits the identifier node and retrieves a set of resource ids using the resolver. Then if
	 * current node does not have any mapping nodes above the set is intersected with provided id
	 * set. Then the result {@link IdSet} is returned up the tree.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting {@link IdSet} 
	 */
    public Object visit(ASTResourceIdentifierId node, Object data)
    {
		IdSet set = (IdSet) super.visit(node, data);
		if(this.parentMappings == 0) // this is the "leaf" resource id
		{
			set.intersect(initialIdSet);
		}
		return set;
    }

	/**
	 * Visits the identifier node and retrieves a set of resource ids using the resolver. Then if
	 * current node does not have any mapping nodes above the set is intersected with provided id
	 * set. Then the result {@link IdSet} is returned up the tree.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting {@link IdSet} 
	 */
    public Object visit(ASTResourceIdentifierPath node, Object data)
    {
		IdSet set = (IdSet) super.visit(node, data);
		if(this.parentMappings == 0) // this is the "leaf" resource id
		{
			set.intersect(initialIdSet);
		}
		return set;
    }
}
