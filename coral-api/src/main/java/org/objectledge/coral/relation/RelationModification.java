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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectledge.coral.store.Resource;

/**
 * A class representing a batch of {@link Relation} modifications.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationModification.java,v 1.8 2004-12-23 07:18:29 rafal Exp $
 */
public class RelationModification
{
	private List operations = new ArrayList(128);
	 
    // basic api ----------------------------------------------------------------------------------

	/**
	 * Visits list of collected operations.
	 *  
	 * @param visitor visitoru used to visit the operations. 
	 */
	public void accept(ModificationOperationVisitor visitor)
	{
		for (Iterator iter = operations.iterator(); iter.hasNext();)
		{
			RelationModification.ModificationOperation operation =
				(RelationModification.ModificationOperation) iter.next();
			operation.visit(visitor);
		}
	}
	
	/**
	 * Resets this <code>RelationModification</code> state - as if no modifications were
	 * performed.
	 */
	public void reset()
	{
		operations.clear();
	}

    // modification -------------------------------------------------------------------------------

    /**
     * Add a ordered pair to the relationship's definition.
     *
     * @param r1 the first element of the pair.
     * @param r2 the second element of the pair.
     */
    public void add(Resource r1, Resource r2)
    {
        add(new Long(r1.getId()), new Long(r2.getId()));
    }

    /**
     * Add a number of pairs to the relationship's definition.
     *
     * @param r1 the first element of the pairs.
     * @param ress second elements of the pairs.
     */
    public void add(Resource r1, Resource[] ress)
    {
        Long id1 = new Long(r1.getId());
        for (int i = 0; i < ress.length; i++)
        {
            add(id1, new Long(ress[i].getId()));
        }
    }

    /**
     * Add a number of pairs to the relationship's definition.
     *
     * @param ress first elements of the pairs.
     * @param r2 the second element of the pairs.
     */
    public void add(Resource[] ress, Resource r2)
    {
        Long id2 = new Long(r2.getId());
        for (int i = 0; i < ress.length; i++)
        {
            add(new Long(ress[i].getId()), id2);
        }
    }

    /**
     * Delete a ordered pair from the relationship's definition.
     *
     * @param r1 the first element of the pair.
     * @param r2 the second element of the4 pair.
     */
    public void remove(Resource r1, Resource r2)
    {
        rem(new Long(r1.getId()), new Long(r2.getId()));
    }

    /**
     * Delete all pairs where r is the first element of the pair from relationship's definition.
     *
     * @param r the resource
     */
    public void remove(Resource r)
    {
        rem(new Long(r.getId()), null);
    }

    /**
     * Delete all pairs where r is the second element of the pair from relationship's definition.
     *
     * @param r the resource
     */
    public void removeInv(Resource r)
    {    	
        rem(null, new Long(r.getId()));
    }

    /**
     * Removes all references defined for the relation.
     */
    public void clear()
    {
        operations.clear();
        operations.add(new ClearOperation());
    }

    // implementation -----------------------------------------------------------------------------

    private void add(Long id1, Long id2)
    {
		operations.add(new AddOperation(id1, id2));
    }

    private void rem(Long id1, Long id2)
    {
		operations.add(new RemoveOperation(id1, id2));
    }

    /**
     * A class representing an operation.
     */
    public abstract static class ModificationOperation
    {
		private int hashCode;
        private long id1 = -1L;
        private long id2 = -1L;
		
		/**
		 * For creation of parameterless operations.
		 */
		public ModificationOperation()
		{
		}
		
		/**
		 * Creates a modification operation bound to one or two ids.
		 * 
		 * @param id1 left side of the relation
		 * @param id2 right side of the relation
		 */
        public ModificationOperation(Long id1, Long id2)
        {
			if(id1 == null && id2 == null)
			{
				throw new IllegalArgumentException("both params cannot be null");
			}

			if(id1 != null && id2 != null)
			{
				hashCode = (id1.hashCode()) ^ (id2.hashCode());
				this.id1 = id1.longValue();
				this.id2 = id2.longValue();
			}
			else if(id1 != null)
			{
				hashCode = id1.hashCode() ^ 0xf0f0f0f0;
				this.id1 = id1.longValue();
			}
			else
			{
				hashCode = id2.hashCode() ^ 0x0f0f0f0f;
				this.id2 = id2.longValue();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode()
		{
			return hashCode;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object o)
		{
			if(o instanceof ModificationOperation)
			{
				ModificationOperation lp = (ModificationOperation)o;
				return (this.id1 == lp.id1) && (this.id2 == lp.id2);
			}
			return false;
		}

		/**
		 * Checks whether first id in the pair is defined.
		 * @return <code>true</code> if Id1 is defined
		 */
		public boolean hasId1()
		{
			return id1 != -1L;
		}

        /**
         * Returns the first id in the pair.
         * @return the id value
         */
        public long getId1()
        {
            return id1;
        }

		/**
		 * Checks whether second id in the pair is defined.
		 * @return <code>true</code> if Id2 is defined
		 */
		public boolean hasId2()
		{
			return id2 != -1L;
		}

        /**
         * Returns the second id in the pair.
         * @return the id value
         */
        public long getId2()
        {
            return id2;
        }

		/**
		 * Inverts the operation direction.
		 */
		public void invert()
		{
			long temp = id2;
			this.id2 = id1;
			this.id1 = temp;
		}

		/**
		 * Executes visit on a given visitor.
		 * 
		 * @param visitor visitor of this operation
		 */		
		public abstract void visit(ModificationOperationVisitor visitor);
    }

	/**
	 * Modification operations visitor interface for typesafe and effortless operations interpreter
	 * creation.
	 */
	public interface ModificationOperationVisitor
	{
		/**
		 * Visits {@link ClearOperation}.
		 * @param oper visited operation
		 */
		public void visit(ClearOperation oper);
        
		/**
		 * Visits {@link AddOperation}.
		 * @param oper visited operation
		 */
		public void visit(AddOperation oper);
        
		/**
		 * Visits {@link RemoveOperation}.
		 * @param oper visited operation
		 */
		public void visit(RemoveOperation oper);
	}
    
	/**
	 * Relation clearing operation.
	 */
	public static class ClearOperation extends ModificationOperation
	{
		/**
		 * {@inheritDoc}
		 */
		public void visit(ModificationOperationVisitor visitor)
		{
			visitor.visit(this);
		}
	}

	/**
	 * Relation addition operation.
	 */
	public static class AddOperation extends ModificationOperation
	{
		/**
		 * {@inheritDoc}
		 */
        public AddOperation(Long id1, Long id2)
        {
            super(id1, id2);
        }
        
		/**
		 * {@inheritDoc}
		 */
		public void visit(ModificationOperationVisitor visitor)
		{
			visitor.visit(this);
		}
	}

	/**
	 * Relation removal operation, allows <em>wildcard</em> removals by defining only one side of
	 * the relation.
	 */
	public static class RemoveOperation extends ModificationOperation
	{
		/**
		 * {@inheritDoc}
		 */
		public RemoveOperation(Long id1, Long id2)
		{
			super(id1, id2);
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(ModificationOperationVisitor visitor)
		{
			visitor.visit(this);
		}
	}
}
