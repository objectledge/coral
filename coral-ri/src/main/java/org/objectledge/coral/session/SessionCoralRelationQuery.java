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

import java.util.Set;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.relation.CoralRelationQuery;
import org.objectledge.coral.relation.MalformedRelationQueryException;
import org.objectledge.coral.relation.ResourceIdentifierResolver;
import org.objectledge.coral.store.Resource;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralRelationQuery.java,v 1.1 2004-03-16 14:16:18 fil Exp $
 */
public class SessionCoralRelationQuery implements CoralRelationQuery
{
    private CoralCore coral;
    private CoralSessionImpl session;

    /**
     * Creates a session local CoralStore wrapper.
     * 
     * @param coral the coral component hub.
     * @param coralSession the coral session.
     */
    SessionCoralRelationQuery(CoralCore coral, CoralSessionImpl session)
    {
        this.coral = coral;
        this.session = session;
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] query(String query, ResourceIdentifierResolver resolver)
        throws MalformedRelationQueryException, EntityDoesNotExistException
    {
        session.verify();
        return coral.getRelationQuery().query(query, resolver);
    }

    /** 
     * {@inheritDoc}
     */
    public Resource[] query(String query, ResourceIdentifierResolver resolver, Set initialIdSet)
        throws MalformedRelationQueryException, EntityDoesNotExistException
    {
        session.verify();
        return coral.getRelationQuery().query(query, resolver, initialIdSet);
    }
}
