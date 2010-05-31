/**
 * 
 */
package org.objectledge.coral.table.comparator;

import java.util.Comparator;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;

/**
 * A comparator for AttributeDefinitions that orders according to declaring class nesting level,
 * then declaring class name and finally attribute name.
 * 
 * @author rafal
 */
public class AttributeDefnitionsComparator
    implements Comparator<AttributeDefinition>
{
    @Override
    public int compare(AttributeDefinition attr1, AttributeDefinition attr2)
    {
        ResourceClass decl1 = attr1.getDeclaringClass();
        ResourceClass decl2 = attr2.getDeclaringClass();
        int c = 0;
        if(!decl1.equals(decl2))
        {
            if(decl1.isParent(decl2))
            {
                c = -1;
            }
            else if(decl2.isParent(decl1))
            {
                c = 1;
            }
        }
        if(c == 0)
        {
            c = decl1.getName().compareTo(decl2.getName());
            if(c == 0)
            {
                c = attr1.getName().compareTo(attr2.getName());
            }
        }
        return c;
    }
}
