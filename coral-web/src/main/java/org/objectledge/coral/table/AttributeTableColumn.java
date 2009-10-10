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

package org.objectledge.coral.table;

import java.util.Comparator;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.table.comparator.AttributeValueComparator;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;

/**
 * A table column base on a Coral resource's attribute.
 *
 * @author <a href="rafal@caltha.pl">Rafał Krzewski</a>
 * @version $Id: AttributeTableColumn.java,v 1.2 2008-06-05 16:38:00 rafal Exp $
 */
public class AttributeTableColumn<R extends Resource, V>
    extends TableColumn<Resource>
{
    /**
     * Creates a new AttributeTableColumn instance.
     *
     * @param name
     * @param comparator
     * @throws TableException
     */
    public AttributeTableColumn(ResourceClass resourceClass, String attributeName,
        Comparator<V> valueComparator)
        throws TableException
    {
        super(attributeName, getAttributeComparator(resourceClass, attributeName, valueComparator));
    }
    
    /**
     * Creates an attribute value based column comparator object.
     *  
     * @param resourceClass the resource class.
     * @param attributeName the attribute name.
     * @param valueComparator the comparator for attribute values.
     */
    public static <R extends Resource, V> Comparator<R> getAttributeComparator(ResourceClass resourceClass, 
        String attributeName, Comparator<V> valueComparator)
    {
        AttributeDefinition attDef = resourceClass.getAttribute(attributeName);
        return new AttributeValueComparator<R, V>((Class<R>)resourceClass.getJavaClass(), attDef,
            valueComparator);        
    }
}
