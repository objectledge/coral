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

import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;

/**
 * Acts as a management interface for {@link Relation} creation and deletion. 
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralRelationManager.java,v 1.3 2004-03-17 11:20:00 zwierzem Exp $
 */
public interface CoralRelationManager
{
	/**
	 * Retrieves all relations.
	 *
	 * @return the array of existing relations.
	 */
	public Relation[] getRelation();

	/**
	 * Retrieves relation with a given id.
	 *
	 * @param id the id of the relation.
	 * @return the relation.
	 * @throws EntityDoesNotExistException if the <code>Relation</code>
	 *         with the specified id does not exist.
	 */
	public Relation getRelation(long id)
		throws EntityDoesNotExistException;

	/**
	 * Retrieves relation with a given name.
	 *
	 * @param name the name of the new resource.
	 * @return the relation.
     * @throws EntityDoesNotExistException if the <code>Relation</code>
     *         with the specified name does not exist.
     * @throws AmbigousEntityNameException if there is more than one <code>Relation</code>
     *         with the specified name.
	 */
	public Relation getRelation(String name)
		throws EntityDoesNotExistException, AmbigousEntityNameException;

	/**
	 * Creates a relation image in the persistent storage.
	 *
	 * @param name the name of the new resource.
	 * @return the newly created relation.
	 * @throws EntityExistsException thrown if relation with a given name already exists
	 */
	public Relation createRelation(String name)
		throws EntityExistsException;

	/**
	 * Renames the relation.
	 *
	 * @param relation the relation to rename.
	 * @param name the new name of the relation.
	 * @throws EntityExistsException if a relation with a given name already exists.
	 */
	public void setName(Relation relation, String name)
		throws EntityExistsException;

	/**
	 * Updates a relation image in the persistent storage unsing provided
	 * {@link RelationModification} object.
	 *
	 * @param relation relation to be modified
	 * @param modification modification batch object for the relation
	 */
	public void updateRelation(Relation relation, RelationModification modification);
    
	/**
	 * Destroys a relation.
	 *
	 * @param relation the relation to remove.
	 * @throws IllegalArgumentException if the relation has not been saved in the persistent 
	 * 		storage yet.
	 */
	public void deleteRelation(Relation relation)
		throws IllegalArgumentException;
}
