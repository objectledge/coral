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

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.relation.CachingResourceIdentifierResolver;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.CoralRelationQuery;
import org.objectledge.coral.relation.MalformedRelationQueryException;
import org.objectledge.coral.relation.ResourceIdentifierResolver;
import org.objectledge.coral.relation.query.parser.RelationQueryParser;
import org.objectledge.coral.relation.query.parser.SimpleNode;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;

import bak.pcj.LongIterator;
import bak.pcj.set.LongSet;

/**
 * An implementation of the relation query.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralRelationQueryImpl.java,v 1.2 2004-12-27 03:06:06 rafal Exp $
 */
public class CoralRelationQueryImpl implements CoralRelationQuery
{
	private CoralRelationManager relationManager;
    private CoralStore store;

    /**
     * The implementation of relation query services.
     * 
     * @param relationManager used to retrieve relations while querying.
     * @param store used to retrieve queried resources.
     */
    public CoralRelationQueryImpl(CoralRelationManager relationManager, CoralStore store)
    {
    	this.relationManager = relationManager;
        this.store = store;
    }

    /**
     * {@inheritDoc}
     */
    public Resource[] query(String query, ResourceIdentifierResolver resolver)
        throws MalformedRelationQueryException, EntityDoesNotExistException
    {
        return query(query, resolver, null);
    }

    
    /**
     * {@inheritDoc}
     */
    public LongSet queryIds(String query, ResourceIdentifierResolver resolver, LongSet initialIdSet)
        throws MalformedRelationQueryException, EntityDoesNotExistException
    {        
        SimpleNode tree = null;
        try
        {
            tree = RelationQueryParser.executeParse(query);
        }
        catch(Exception e)
        {
            throw new MalformedRelationQueryException("query is malformed", e);
        }
        
        ResourceIdentifierResolver resolver2 = new CachingResourceIdentifierResolver(resolver);
        QueryExecutor executor = null;
        
        // calculate complexity
        if(initialIdSet != null)
        {
            int size = initialIdSet.size();
            QueryComplexityCalculator calc =
                new QueryComplexityCalculator(relationManager, resolver2, -1);
            int resultIntersectionCompl = ((Integer)(tree.jjtAccept(calc, null))).intValue();
            resultIntersectionCompl = 
                resultIntersectionCompl < size ? resultIntersectionCompl : size;
            
            calc = new QueryComplexityCalculator(relationManager, resolver2, size);
            int leafIntersectionCompl = ((Integer)(tree.jjtAccept(calc, null))).intValue();
            
            // choose executor
            if(leafIntersectionCompl < resultIntersectionCompl)
            {
                executor = 
                    new UnmappedLeafIntersectQueryExecutor(relationManager, resolver2, initialIdSet);
            }
            else
            {
                executor = 
                    new ResultIntersectQueryExecutor(relationManager, resolver2, initialIdSet);
            }
        }
        else
        {
            executor = new QueryExecutor(relationManager, resolver2);
        }
        
        IdSet idSet = (IdSet)(tree.jjtAccept(executor, null));
        
        return idSet.getSet();
    }

    /**
     * {@inheritDoc}
     */
	public Resource[] query(String query, ResourceIdentifierResolver resolver, LongSet initialIdSet)
        throws MalformedRelationQueryException, EntityDoesNotExistException
    {
	    LongSet idSet = queryIds(query, resolver, initialIdSet);
	    
        Resource[] resources = new Resource[idSet.size()];
        int j=0;
        for(LongIterator i=idSet.iterator(); i.hasNext(); j++)
        {
            resources[j] = store.getResource(i.next());
        }
        return resources;
    }
}
