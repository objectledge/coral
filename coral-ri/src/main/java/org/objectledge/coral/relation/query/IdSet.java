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

/**
 * Id set represents a set of resource ids. It is used during query execution.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: IdSet.java,v 1.2 2005-04-01 12:32:12 zwierzem Exp $
 */
public class IdSet
{
	private Set<Long> set;

	/**
	 * Constucts an id set from a set of {@link Long} objects.
	 *  
	 * @param set set of ids
	 */
	public IdSet(Set<Long> set)
	{
		this.set = set;
	}

	/**
	 * Adds all elements stored in a given set to this set (modifies this object).
	 *   
	 * @param other the added set of ids 
	 */
	public void sum(IdSet other)
	{
		set.addAll(other.set);
	}

	/**
	 * Performs an intersection operation of this set and a given set by modifying this set.
	 *   
	 * @param other the intersected set of ids 
	 */
	public void intersect(IdSet other)
	{
		set.retainAll(other.set);
	}
	
    /**
     * Returns a set stored in this id set.
     * 
     * @return internal set of {@link Long} objects
     */
    public Set<Long> getSet()
    {
        return set;
    }
}
