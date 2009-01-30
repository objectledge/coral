// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//  
// * Redistributions of source code must retain the above copyright notice,  
//	 this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//	 this list of conditions and the following disclaimer in the documentation  
//	 and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//	 nor the names of its contributors may be used to endorse or promote products  
//	 derived from this software without specific prior written permission. 
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
package org.objectledge.coral.session;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityFactory;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.relation.RelationModification;

/**
 * Session private wrapper for the Coral relation manger component.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralRelationManager.java,v 1.5 2009-01-30 13:43:58 rafal Exp $
 */
public class SessionCoralRelationManager implements CoralRelationManager
{
    private CoralCore coral;
    private CoralSessionImpl session;

    /**
     * Creates a session local CoralStore wrapper.
     * 
     * @param coral the coral component hub.
     * @param session the coral session.
     */
    SessionCoralRelationManager(CoralCore coral, CoralSessionImpl session)
    {
        this.coral = coral;
        this.session = session;
    }

    /** 
     * {@inheritDoc}
     */
    public Relation[] getRelation()
    {
        session.verify();
        return coral.getRelationManager().getRelation();
    }

    /** 
     * {@inheritDoc}
     */
    public Relation getRelation(long id) throws EntityDoesNotExistException
    {
        session.verify();
        return coral.getRelationManager().getRelation(id);
    }

    /** 
     * {@inheritDoc}
     */
    public Relation getRelation(String name)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        session.verify();
        return coral.getRelationManager().getRelation(name);
    }

    /** 
     * {@inheritDoc}
     */
    public Relation createRelation(String name) throws EntityExistsException
    {
        session.verify();
        return coral.getRelationManager().createRelation(name);
    }

    /** 
     * {@inheritDoc}
     */
    public void setName(Relation relation, String name) throws EntityExistsException
    {
        session.verify();
        coral.getRelationManager().setName(relation, name);
    }

    /** 
     * {@inheritDoc}
     */
    public void updateRelation(Relation relation, RelationModification modification)
    {
        session.verify();
        coral.getRelationManager().updateRelation(relation, modification);
    }

    /** 
     * {@inheritDoc}
     */
    public void deleteRelation(Relation relation) throws IllegalArgumentException
    {
        session.verify();
        coral.getRelationManager().deleteRelation(relation);
    }

    /**
     * {@inheritDoc}
     */
    public long[] getRelationDefinition(Relation relation)
    {
        session.verify();
        return coral.getRelationManager().getRelationDefinition(relation);
    }

    @Override
    public EntityFactory<Relation> getRelationFactory()
    {
        session.verify();
        return coral.getRelationManager().getRelationFactory();
    }
}
