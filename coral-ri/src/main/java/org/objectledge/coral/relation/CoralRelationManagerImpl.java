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

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;

/**
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralRelationManagerImpl.java,v 1.1 2004-02-24 13:34:59 zwierzem Exp $
 */
public class CoralRelationManagerImpl implements CoralRelationManager
{
    /**
     * 
     */
    public CoralRelationManagerImpl()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     */
    public Relation[] getRelation()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Relation getRelation(long id) throws EntityDoesNotExistException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Relation getRelation(String name) throws EntityDoesNotExistException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Relation createRelation(String name) throws EntityExistsException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(Relation relation, String name)
    {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    public void updateRelation(Relation relation, RelationModification modification)
    {
    	MinimalRelationModification minimalMod =
    		new MinimalRelationModification(modification, relation);
		if(relation.isInverted())
		{
			relation = relation.getInverted();
		}
		RelationImpl relationImpl = (RelationImpl)relation;
        
        // TODO Update the relation table, synchronize on relation
        // update in memory relation representation
		if(minimalMod.getClear())
		{
			relationImpl.clear();
		}
		long[][] data = minimalMod.getRemoved();
		for (int i = 0; i < data.length; i++)
        {
            relationImpl.remove(data[i][0], data[i][1]);
        }
		data = minimalMod.getAdded();
		for (int i = 0; i < data.length; i++)
		{
			relationImpl.add(data[i][0], data[i][1]);
		}
    }

    /**
     * {@inheritDoc}
     */
    public void deleteRelation(Relation relation) throws IllegalArgumentException
    {
        // TODO Auto-generated method stub

    }
}
