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
package org.objectledge.coral.relation;

import java.util.Set;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.Resource;

/**
 * Executes queries based on relations defined between Coral resources. 
 *
 * <p>TODO: Update Query Grammar</p>
 * 
 * <p>Queries have a following grammar:</p>
 * <pre>
 * expression = sum_expression
 * sum_expression = intersection_expression [ "OR" intersection_expression ]
 * intersection_expression = unary_expression [ "AND" unary_expression ]
 * unary_expression = "(" expression ")" | resource_identifier
 * resource_identifier = "'[a-zA-Z/_][a-zA-Z0-9/._]*'" | "[0-9]+" 
 * </pre>
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralRelationQuery.java,v 1.1 2004-02-20 09:15:48 zwierzem Exp $
 */
public interface CoralRelationQuery
{
	/**
	 * Executes a query for resources based on a given relation query string. Resource identifiers 
	 * in the query string are resolved to resource id sets by a given
	 * {@link ResourceIdentifierResolver}.
	 *
	 * @param query a string representation of a query
	 * @param resolver resource identifier resolver to be used while executing the query
	 * @return an array of resources - the query result 
	 * @throws MalformedRelationQueryException on query parsing errors
	 * @throws EntityDoesNotExistException thrown by the resolver
	 */
	public Resource[] query(String query, ResourceIdentifierResolver resolver)
		throws MalformedRelationQueryException, EntityDoesNotExistException;

	/**
	 * Executes a query for resources based on a given relation query string and for efficiency
	 * intersects query results with a given resource id set. Resource identifiers in the query
	 * string are resolved to resource id sets by a given {@link ResourceIdentifierResolver}.
	 *
	 * @param query a string representation of a query
	 * @param resolver resource identifier resolver to be used while executing the query
	 * @param initialIdSet set of resource ids ({@link Long} objects}
	 * @return an array of resources - the query result 
	 * @throws MalformedRelationQueryException on query parsing errors
	 * @throws EntityDoesNotExistException thrown by the resolver
	 */
	public Resource[] query(String query, ResourceIdentifierResolver resolver, Set initialIdSet)
		throws MalformedRelationQueryException, EntityDoesNotExistException;
}
