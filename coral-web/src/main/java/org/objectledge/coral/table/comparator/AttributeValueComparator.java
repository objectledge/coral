// 
// Copyright (c) 2003-2005, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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

package org.objectledge.coral.table.comparator;

import java.util.Comparator;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.store.Resource;

/**
 * A Comparator that compares Resources by the values of an attribute.
 *
 * @param <T> the type of resources to be compared.
 * 
 * @author <a href="rafal@caltha.pl">Rafał Krzewski</a>
 * @version $Id: AttributeValueComparator.java,v 1.2 2008-06-05 16:37:58 rafal Exp $
 */
public class AttributeValueComparator<T extends Resource, V>
    implements Comparator<T>
{
    private final AttributeDefinition<V> attrDef;
    private final Comparator<V> valueComparator;

    /**
     * Creates a new AttributeValueComparator instance.
     *
     * @param type the type of the resource.
     * @param attrDef the attribute definition.
     * @param valueComparator a compartor for the attribute values.
     */
    public AttributeValueComparator(Class<T> type, AttributeDefinition<V> attrDef,
        Comparator<V> valueComparator)
    {
        if(!attrDef.getDeclaringClass().getJavaClass().isAssignableFrom(type))
        {
            throw new IllegalArgumentException(type + " is not a subclass of "
                + attrDef.getDeclaringClass().getJavaClass());
        }
        if((attrDef.getFlags() & AttributeFlags.REQUIRED) == 0)
        {
            throw new IllegalArgumentException(attrDef + " is not a REQUIRED attribute");
        }
        this.attrDef = attrDef;
        this.valueComparator = valueComparator;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compare(T o1, T o2)
    {
        V v1 = (V) o1.get(attrDef);
        V v2 = (V) o2.get(attrDef);
        return valueComparator.compare(v1, v2);
    }
}
