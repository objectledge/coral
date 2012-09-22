package org.objectledge.coral.datatypes;

import java.util.Map;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;

public class GenericResourceQueryHandler
    extends BaseResourceQueryHandler
{
    public void appendFromClause(StringBuilder query, ResultColumn<?> rcm,
        Map<String, String> bulitinAttrNames, boolean restrictClasses)
    {
        query.append("(SELECT ");
        appendBuiltinAttributes(query, rcm, bulitinAttrNames);

        query.append("\nFROM coral_resource r");
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
        query.append("\nWHERE ");
        if(restrictClasses)
        {
            appendResourceClassWhereClause(query, rcm);
            query.append(" AND ");
        }
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                query.append(rcm.isOuter(ad) ? "d" : "g");
                query.append(j + 1).append(".attribute_definition_id").append(" = ")
                    .append(ad.getId());
                query.append(" AND ");
            }
        }
        if(query.toString().endsWith(" AND "))
        {
            query.setLength(query.length() - 5); // roll back lack " AND "
        }
        else
        {
            query.setLength(query.length() - 7); // roll back "\nWHERE "
        }
        query.append(") r").append(rcm.getIndex());
    }

    @Override
    public boolean appendWhereClause(StringBuilder query, boolean whereStarted, ResultColumn<?> rcm)
    {
        return whereStarted;
    }

    public void appendAttributeTerm(StringBuilder query, ResultColumnAttribute<?, ?> rca)
    {
        query.append("r").append(rca.getColumn().getIndex()).append(".a")
            .append(rca.getColumn().getNameIndex(rca.getAttribute()));
    }
}
