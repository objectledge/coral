// 
// Copyright (c) 2003-2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral.relation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;

/**
 * Natural tree-realation of the resources.
 * 
 * <p>The "forwards" relation expresses resource -> it's children relationship. The
 * "reverse"/inverted relation expresses resource -> it's parent relationship.</p>
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceHierarchyRelationImpl.java,v 1.5 2005-05-20 00:46:16 rafal Exp $
 */
public class ResourceHierarchyRelationImpl
    implements Relation
{
    private static final long ID = 1L;

    private static final Long ID_OBJECT = new Long(1L);

    private static final String ID_STRING = "1";

    /** Name of the resource parent-child hierarchy relation. */
    public static final String NAME = "coral.ResourceHierarchy";

    private final CoralStore store;
    
    private final Relation inverted = new InvertedResourceHierarchyRelationImpl();

    /**
     * Creates new ResourceHierarchyRelationImpl instance.
     * 
     * @param coralStore Coral store this relation instance reflects.
     */
    public ResourceHierarchyRelationImpl(CoralStore coralStore)
    {
        this.store = coralStore;
    }
    
    /**
     * {@inheritDoc}
     */
    public Relation getInverted()
    {
        return inverted;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInverted()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Resource[] get(Resource r)
    {
        return store.getResource(r);
    }

    /**
     * {@inheritDoc}
     */
    public Set get(long id)
    {
        Resource r;
        Resource[] children;
        try
        {
            r = store.getResource(id);
            children = store.getResource(r);
            Set s = new HashSet(children.length);
            for(Resource rx : children)
            {
                s.add(rx.getIdObject());
            }
            return s;
        }
        catch(EntityDoesNotExistException e)
        {
            return Collections.EMPTY_SET;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRef(Resource r, Resource rInv)
    {
        if(rInv == null)
        {
            return false;
        }
        if(rInv.getParent() == null)
        {
            return r == null;
        }
        return rInv.getParent().equals(r);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRef(long id, long idInv)
    {
        try
        {
            Resource r = store.getResource(id);
            Resource rInv = store.getResource(idInv);
            return hasRef(r, rInv);
        }
        catch(Exception e)
        {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public float getAvgMappingSize()
    {
        // are you threatening me???
        return (float)Math.E;
    }

    /**
     * {@inheritDoc}
     */
    public long getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    public Long getIdObject()
    {
        return ID_OBJECT;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getIdString()
    {
        return ID_STRING;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * Inverted relation.
     */
    public class InvertedResourceHierarchyRelationImpl
        implements Relation
    {
        /**
         * {@inheritDoc}
         */
        public Relation getInverted()
        {
            return ResourceHierarchyRelationImpl.this;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isInverted()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] get(Resource r)
        {
            return new Resource[] { r.getParent() };
        }

        /**
         * {@inheritDoc}
         */
        public Set get(long id)
        {
            try
            {
                Resource r = store.getResource(id);
                if(r.getParent() != null)
                {
                    return Collections.singleton(r.getParent().getIdObject());
                }
                return Collections.EMPTY_SET;
            }
            catch(EntityDoesNotExistException e)
            {
                return Collections.EMPTY_SET;
            }
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasRef(Resource r, Resource rInv)
        {
            return ResourceHierarchyRelationImpl.this.hasRef(rInv, r);
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasRef(long id, long idInv)
        {
            return ResourceHierarchyRelationImpl.this.hasRef(idInv, id);
        }

        /**
         * {@inheritDoc}
         */
        public float getAvgMappingSize()
        {
            return 1.0F;
        }

        /**
         * {@inheritDoc}
         */
        public long getId()
        {
            return ID;
        }

        /**
         * {@inheritDoc}
         */
        public Long getIdObject()
        {
            return ID_OBJECT;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getIdString()
        {
            return ID_STRING;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getName()
        {
            return NAME;
        }        
    }
}
