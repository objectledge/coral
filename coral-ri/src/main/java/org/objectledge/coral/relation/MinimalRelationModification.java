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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectledge.coral.relation.RelationModification.AddOperation;
import org.objectledge.coral.relation.RelationModification.ClearOperation;
import org.objectledge.coral.relation.RelationModification.RemoveOperation;

/**
 * This class holds a minimal representation of a {@link RelationModification}
 * for a given {@link Relation}.
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: MinimalRelationModification.java,v 1.3 2004-03-02 10:04:01 zwierzem Exp $
 */
public class MinimalRelationModification
{
	private boolean clear = false;
	private Set added = new HashSet(128);
	private Set removed = new HashSet(128);
	
	/**
	 * Constructs a minimal representation of a {@link RelationModification}
	 * for a given {@link Relation}, the representation is preapred for a non inverted relation.
	 * 
	 * @param modification relation modification data
	 * @param relation modified relation
	 */
	public MinimalRelationModification(RelationModification modification, Relation relation)
	{
		ConstructingVisitor visitor = null;
		if(relation.isInverted())
		{
			visitor = new InvertingConstructingVisitor(relation.getInverted());
		}
		else
		{
			visitor = new ConstructingVisitor(relation);
		}
		modification.accept(visitor);
	}
	
	// data retrieval api ---------------------------------------------------------------------

	/**
	 * Should the relation be cleared.
	 * @return <code>true</code> if relation should be cleared
	 */
	public boolean getClear()
	{
		return clear;
	}

	/**
	 * Additions to the relation.
	 * @return an array of relation pairs
	 */
    public long[][] getAdded()
    {
        return setToArray(added);
    }

	/**
	 * Removals from the relation.
	 * @return an array of relation pairs
	 */
    public long[][] getRemoved()
    {
		return setToArray(removed);
    }
    
    // implementation -----------------------------------------------------------------------------

	private void remove(RelationModification.RemoveOperation operation)
	{
		if(added.contains(operation))
		{
			added.remove(operation);
		}
		else
		{
			removed.add(operation);
		}
	}

	private long[][] setToArray(Set set)
	{
		ArrayList list = new ArrayList(set.size());
		for (Iterator iter = set.iterator(); iter.hasNext();)
		{
			RelationModification.ModificationOperation pair =
				(RelationModification.ModificationOperation) iter.next();
			long[] entry = new long[] { pair.getId1(), pair.getId2() };
			list.add(entry);
		}
		long[][] result = new long[list.size()][];
		list.toArray(result);
		return result;
	}	

	// creation -----------------------------------------------------------------------------------

	/**
	 * Class used during Minimal representation construction. 
	 */
	private class ConstructingVisitor implements RelationModification.ModificationOperationVisitor
	{
		/** Relation is used in batch remove operations. */
		protected Relation relation;
		
		public ConstructingVisitor(Relation relation)
		{
			this.relation = relation;
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(ClearOperation oper)
		{
			added.clear();
			removed.clear();
			clear = true;
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(AddOperation oper)
		{
			if(removed.contains(oper))
			{
				removed.remove(oper);
			}
			else
			{
				added.add(oper);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(RemoveOperation oper)
		{
			if(oper.hasId1() && oper.hasId2())
			{
				remove(oper);
			}
			else if(oper.hasId1())
			{
				Long id1 = new Long(oper.getId1());
				Set id2set = relation.get(id1.longValue());
				for (Iterator iter = id2set.iterator(); iter.hasNext();)
                {
                    Long id2 = (Long) iter.next();
					remove(new RelationModification.RemoveOperation(id1, id2));
				}
			}
			else // if(oper.hasId2())
			{
				Long id2 = new Long(oper.getId2());
				Set id1set = relation.get(id2.longValue());
				for (Iterator iter = id1set.iterator(); iter.hasNext();)
				{
					Long id1 = (Long) iter.next();
					remove(new RelationModification.RemoveOperation(id1, id2));
				}
			}
		}
	}

	/**
	 * Class used during Minimal representation construction.
	 */
	private class InvertingConstructingVisitor
		extends ConstructingVisitor
	{
		public InvertingConstructingVisitor(Relation relation)
		{
			super(relation);
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(AddOperation oper)
		{
			oper.invert();
			super.visit(oper);
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(RemoveOperation oper)
		{
			oper.invert();
			super.visit(oper);
		}
	}
}
