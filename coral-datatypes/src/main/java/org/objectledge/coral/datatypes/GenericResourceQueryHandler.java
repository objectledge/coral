package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;

public class GenericResourceQueryHandler
    extends BaseResourceQueryHandler
{
    public void appendFromClause(StringBuilder query, ResultColumn<?> rcm)
    {
        query.append("coral_resource r").append(rcm.getIndex());
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                query.append(", coral_generic_resource ");
                query.append("r").append(rcm.getIndex()).append("g").append(j + 1);
                query.append(", ");
                query.append(ad.getAttributeClass().getDbTable());
                query.append(" ");
                query.append("r").append(rcm.getIndex()).append("a").append(j + 1);
            }
        }
    }

    @Override
    public boolean appendWhereClause(StringBuilder query, boolean whereStarted, ResultColumn<?> rcm)
    {
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                if(whereStarted)
                {
                    query.append("\n  AND ");
                }
                else
                {
                    query.append("\nWHERE ");
                    whereStarted = true;
                }
                query.append("r").append(rcm.getIndex()).append("g").append(j + 1);
                query.append(".resource_id = r").append(rcm.getIndex()).append(".resource_id");
                query.append(" AND ");
                query.append("r").append(rcm.getIndex()).append("g").append(j + 1);
                query.append(".attribute_definition_id = ");
                query.append(ad.getIdString());
                query.append(" AND ");
                query.append("r").append(rcm.getIndex()).append("a").append(j + 1)
                    .append(".data_key = ");
                query.append("r").append(rcm.getIndex()).append("g").append(j + 1)
                    .append(".data_key");
            }
        }
        return whereStarted;
    }

    public void appendAttributeTerm(StringBuilder query, ResultColumnAttribute<?, ?> rca)
    {
        query
            .append("r")
            .append(rca.getColumn().getIndex())
            .append("a")
            .append(
                ((Integer)rca.getColumn().getNameIndex().get(rca.getAttribute().getName()))
                    .intValue() + 1);
        if(Entity.class.isAssignableFrom(rca.getAttribute().getAttributeClass().getJavaClass()))
        {
            query.append(".ref");
        }
        else
        {
            query.append(".data");
        }
    }
}
