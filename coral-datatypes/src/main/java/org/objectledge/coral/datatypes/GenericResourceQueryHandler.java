package org.objectledge.coral.datatypes;

import java.util.List;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;

public class GenericResourceQueryHandler
    extends BaseResourceQueryHandler
{

    public boolean appendDataWhereClause(StringBuilder query, ResultColumn<?> rcm,
        List<AttributeDefinition<?>> attrs, List<ResourceClass<?>> hostClasses, boolean whereStarted)
    {
        for(int j = 0; j < attrs.size(); j++)
        {
            AttributeDefinition<?> ad = attrs.get(j);
            int ni = rcm.getNameIndex(ad);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                whereStarted = appendWhere(query, whereStarted);
                query.append(rcm.isOuter(ad) ? "d" : "g");
                query.append(ni).append(".attribute_definition_id").append(" = ")
                    .append(ad.getId());
            }
        }
        return whereStarted;
    }

    public void appendDataFromClause(StringBuilder query, ResultColumn<?> rcm,
        List<AttributeDefinition<?>> attrs, List<ResourceClass<?>> hostClasses, boolean outer)
    {
        if(!outer)
        {
            for(AttributeDefinition<?> ad : attrs)
            {
                int ni = rcm.getNameIndex(ad);
                if((ad.getFlags() & AttributeFlags.BUILTIN) == 0 && !rcm.isOuter(ad))
                {
                    query.append("\nJOIN coral_generic_resource ");
                    query.append("g").append(ni);
                    query.append(" ON (r.resource_id = g").append(ni).append(".resource_id)");
                    query.append("\nJOIN ");
                    query.append(ad.getAttributeClass().getDbTable()).append(" ").append("a")
                        .append(ni);
                    query.append(" ON (g").append(ni).append(".data_key = a").append(ni)
                        .append(".data_key)");
                }
            }
        }
        else
        {
            for(AttributeDefinition<?> ad : attrs)
            {
                int ni = rcm.getNameIndex(ad);
                if((ad.getFlags() & AttributeFlags.BUILTIN) == 0 && rcm.isOuter(ad))
                {
                    query.append("\nJOIN coral_attribute_definition d").append(ni);
                    query.append(" ON (r.resource_class_id = d").append(ni)
                        .append(".resource_class_id)");
                    query.append("\nLEFT OUTER JOIN coral_generic_resource ");
                    query.append("g").append(ni);
                    query.append(" ON (r.resource_id = g").append(ni).append(".resource_id");
                    query.append(" AND d").append(ni).append(".attribute_definition_id = g")
                        .append(ni).append(".attribute_definition_id)");
                    query.append("\nLEFT OUTER JOIN ");
                    query.append(ad.getAttributeClass().getDbTable()).append(" ").append("a")
                        .append(ni);
                    query.append(" ON (g").append(ni).append(".data_key = a").append(ni)
                        .append(".data_key)");
                }
            }
        }
    }

    public void appendAttributes(StringBuilder query, ResultColumn<?> rcm,
        List<AttributeDefinition<?>> attrs, List<ResourceClass<?>> hostClasses)
    {
        for(AttributeDefinition<?> ad : attrs)
        {
            int ni = rcm.getNameIndex(ad);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                query.append(", a").append(ni);
                if(Entity.class.isAssignableFrom(ad.getAttributeClass().getJavaClass()))
                {
                    query.append(".ref ");
                }
                else
                {
                    query.append(".data ");
                }
                query.append("a").append(ni);
            }
        }
    }
}
