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
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: AbstractCoralRelationManager.java,v 1.2 2004-02-20 14:58:51 zwierzem Exp $
 */
public abstract class AbstractCoralRelationManager implements CoralRelationManager
{
	/**
	 * This class holds a minimal representation for a {@link RelationModification}
	 * and {@link Relation}.
	 */
	public class RelationUpdateData
		implements RelationModification.ModificationOperationVisitor
	{
		private Relation relation;
		private boolean clear = false;
		private Set added = new HashSet(128);
		private Set removed = new HashSet(128);
		
		/**
		 * Constructs a minimal representation for a {@link RelationModification}
		 * and {@link Relation}.
		 * 
		 * @param modification relation modification data
		 * @param relation modified relation
		 */
		public RelationUpdateData(RelationModification modification, Relation relation)
		{
			this.relation = relation;
			for (Iterator iter = modification.getOperations().iterator(); iter.hasNext();)
			{
				RelationModification.ModificationOperation operation =
					(RelationModification.ModificationOperation) iter.next();
				operation.visit(this);
			}
		}
		
		// creation api ---------------------------------------------------------------------------

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
				long[] id2set = relation.get(id1.longValue());
				for (int i = 0; i < id2set.length; i++)
                {
                    remove(new RelationModification.RemoveOperation(id1, new Long(id2set[i])));
                }
			}
			else // if(oper.hasId2())
			{
				Long id2 = new Long(oper.getId2());
				long[] id1set = relation.get(id2.longValue());
				for (int i = 0; i < id1set.length; i++)
				{
					remove(new RelationModification.RemoveOperation(new Long(id1set[i]), id2));
				}
			}
        }

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
	}
}
