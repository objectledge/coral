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
import org.objectledge.coral.relation.ResourceIdentifierResolver;
import org.objectledge.coral.relation.query.parser.ASTStart;

/**
 * This kind of a query executor executes a parsed query and before returning the result intersects
 * it with a provided set and returns intersection result.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ResultIntersectQueryExecutor.java,v 1.1 2004-02-25 10:57:20 zwierzem Exp $
 */
public class ResultIntersectQueryExecutor extends QueryExecutor
{
	private IdSet initialIdSet;

    /**
     * Constructs the query executor.
     * 
	 * @param relationManager relation manager is used to retrieve relations 
	 * @param resolver resolver used to retrieve resource ids
     * @param initialIdSet used to intersect with query results 
     */
    public ResultIntersectQueryExecutor(
        CoralRelationManager relationManager,
        ResourceIdentifierResolver resolver,
		Set initialIdSet)
    {
        super(relationManager, resolver);
		this.initialIdSet = new IdSet(initialIdSet);
	}

	/**
	 * Visits the start node and retrieves a set of resource ids from the children nodes. Then
	 * retrieved {@link IdSet} is intersected with a provided set.
	 * 
	 * @param node visited node
	 * @param data additional data storage
	 * @return the resulting {@link IdSet} 
	 */
	public Object visit(ASTStart node, Object data)
	{
		IdSet idSet = (IdSet) super.visit(node, data);
		idSet.intersect(initialIdSet);
		return idSet;
	}
}
