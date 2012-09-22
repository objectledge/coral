package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;

public class GenericResourceQueryHandler
    extends BaseResourceQueryHandler
{

    protected boolean appendDataWhereClause(StringBuilder query, ResultColumn<?> rcm,
        boolean whereStarted)
    {
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                whereStarted = appendWhere(query, whereStarted);
                query.append(rcm.isOuter(ad) ? "d" : "g");
                query.append(j + 1).append(".attribute_definition_id").append(" = ")
                    .append(ad.getId());
            }
        }
        return whereStarted;
    }

    protected void appendDataFromClause(StringBuilder query, ResultColumn<?> rcm)
    {
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0 && !rcm.isOuter(ad))
            {
                query.append("\nJOIN coral_generic_resource ");
                query.append("g").append(j + 1);
                query.append(" ON (r.resource_id = g").append(j + 1).append(".resource_id)");
                query.append("\nJOIN ");
                query.append(ad.getAttributeClass().getDbTable()).append(" ").append("a")
                    .append(j + 1);
                query.append(" ON (g").append(j + 1).append(".data_key = a").append(j + 1)
                    .append(".data_key)");
            }
        }
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0 && rcm.isOuter(ad))
            {
                query.append("\nJOIN coral_attribute_definition d").append(j + 1);
                query.append(" ON (r.resource_class_id = d").append(j + 1)
                    .append(".resource_class_id)");
                query.append("\nLEFT OUTER JOIN coral_generic_resource ");
                query.append("g").append(j + 1);
                query.append(" ON (r.resource_id = g").append(j + 1).append(".resource_id");
                query.append(" AND d").append(j + 1).append(".attribute_definition_id = g")
                    .append(j + 1).append(".attribute_definition_id)");
                query.append("\nLEFT OUTER JOIN ");
                query.append(ad.getAttributeClass().getDbTable()).append(" ").append("a")
                    .append(j + 1);
                query.append(" ON (g").append(j + 1).append(".data_key = a").append(j + 1)
                    .append(".data_key)");
            }
        }
    }

    protected void appendAttributes(StringBuilder query, ResultColumn<?> rcm)
    {
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                query.append(", a").append(rcm.getNameIndex(ad));
                if(Entity.class.isAssignableFrom(ad.getAttributeClass().getJavaClass()))
                {
                    query.append(".ref ");
                }
                else
                {
                    query.append(".data ");
                }
                query.append("a").append(rcm.getNameIndex(ad));
            }
        }
    }
}
