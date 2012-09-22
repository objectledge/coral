package org.objectledge.coral.datatypes;

import java.util.Map;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.query.ResourceQueryHandler;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;

public abstract class BaseResourceQueryHandler
    implements ResourceQueryHandler
{
    protected void appendBuiltinAttributes(StringBuilder query, ResultColumn<?> rcm,
        Map<String, String> bulitinAttrNames)
    {
        query.append("r.resource_id, r.resource_class_id, ");
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) != 0 && !ad.getName().equals("id")
                && !ad.getName().equals("resource_class"))
            {
                query.append("r.").append(bulitinAttrNames.get(ad.getName())).append(", ");
            }
        }
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                query.append("a").append(rcm.getNameIndex(ad));
                if(Entity.class.isAssignableFrom(ad.getAttributeClass().getJavaClass()))
                {
                    query.append(".ref ");
                }
                else
                {
                    query.append(".data ");
                }
                query.append("a").append(rcm.getNameIndex(ad));
                query.append(", ");
            }
        }
        query.setLength(query.length() - 2);
    }

    protected void appendResourceClassWhereClause(StringBuilder query, ResultColumn<?> rcm)
    {
        ResourceClass<?>[] children = rcm.getRClass().getChildClasses();
        query.append("r").append(".resource_class_id");
        if(children.length > 0)
        {
            query.append(" IN (");
            for(int j = 0; j < children.length; j++)
            {
                query.append(children[j].getIdString());
                query.append(", ");
            }
            query.append(rcm.getRClass().getIdString()).append(")");
        }
        else
        {
            query.append(" = ");
            query.append(rcm.getRClass().getIdString());
        }
    }
}
