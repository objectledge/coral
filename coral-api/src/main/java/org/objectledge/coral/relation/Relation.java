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

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.store.Resource;

/**
 * Represents a many-to-many relationship (a cross reference) among {@link Resource}s. 
 *
 * <p>The relationship is represented as a set of ordered pairs of resources.
 * The methods defined in this interface allow performing simple operations on the relationship.
 * </p>  
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: Relation.java,v 1.9 2006-03-03 12:39:47 rafal Exp $
 */
public interface Relation extends Entity
{
	/**
	 * Returns the reverse view of this relationship.
	 *
	 * @return the reverse view of this relationship.
	 */
	public Relation getInverted();

	/**
	 * Returns <code>true</code> if this is the reverse view of the relationship.
	 * 
	 * @return <code>true</code> if this is the reverse view of the relationship.
	 */
	public boolean isInverted();

	// reading ------------------------------------------------------------------------------------

	/**
	 * Returns the second elements of all pairs in the relationship definition
	 * where r is the first element.
	 *
	 * @param r the Resource.
	 * @return the array of resources in relation to a given resource
	 */
	public Resource[] get(Resource r);

	/**
	 * Returns the second elements ids of all pairs in the relationship definition
	 * where id is the first elements id.
	 *
	 * @param id the Resource id.
	 * @return the unmodifiable set of resource ids in relation to a given resource id
	 */
	public Set<Long> get(long id);

	/** 
	 * Returns <code>true</code> if given resource references the other.
	 *
	 * @param r resource one.
	 * @param rInv resource two
	 * @return <code>true</code> if given resource (r) is bound to another given resource (rInv).
	 */
	public boolean hasRef(Resource r, Resource rInv);

	/** 
	 * Returns <code>true</code> if given resource id references the other.
	 *
	 * @param id resource id one.
	 * @param idInv resource id two
	 * @return <code>true</code> if given resource id (id) is bound to another 
	 * 		given resource id (idInv).
	 */
	public boolean hasRef(long id, long idInv);
	
	// statistical info ---------------------------------------------------------------------------

    /**
     * Returns the number of pairs defined in this relation.
     * 
     * @return the number of pairs defined in this relation.
     */
    public int size();
    
	/**
	 * Returns a number equal to the average size of a mapping for this relation.
     * 
     * <p>Average size is calculated in a following way:
	 * <pre>
	 * TotalSize = 0
	 * foreach x in RelationPair(x,y)
	 *    totalSize += |P| where P = {RelationPair(a,b) : a = x} 
	 * end
	 *                              totalSize
	 * averageMappingSize = ------------------------- 
	 * 						 | {RelationPair(x,y)} | 
	 * </pre>
	 * @return average mapping size 
	 */
	public float getAvgMappingSize();
}
