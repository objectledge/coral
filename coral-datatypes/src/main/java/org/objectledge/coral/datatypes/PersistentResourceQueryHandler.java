package org.objectledge.coral.datatypes;

import java.util.List;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;

public class PersistentResourceQueryHandler
    extends BaseResourceQueryHandler
{
    public void appendDataFromClause(StringBuilder query, ResultColumn<?> rcm,
        List<AttributeDefinition<?>> attrs, List<ResourceClass<?>> hostClasses, boolean outer)
    {
        if(!outer)
        {
            for(int i = 0; i < hostClasses.size(); i++)
            {
                final ResourceClass<?> rc = hostClasses.get(i);
                query.append("\nJOIN ").append(rc.getDbTable()).append(" p").append(i + 1);
                query.append(" ON (r.resource_id = p").append(i + 1);
                query.append(".resource_id)");
            }
        }
    }

    public boolean appendDataWhereClause(StringBuilder query, ResultColumn<?> rcm,
        List<AttributeDefinition<?>> attrs, List<ResourceClass<?>> hostClasses, boolean whereStarted)
    {
        return whereStarted;
    }

    public void appendAttributes(StringBuilder query, ResultColumn<?> rcm,
        List<AttributeDefinition<?>> attrs, List<ResourceClass<?>> hostClasses)
    {
        for(AttributeDefinition<?> ad : attrs)
        {
            int ni = rcm.getNameIndex(ad);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                final String dbColumn = ad.getDbColumn();
                final int i = hostClasses.indexOf(ad.getDeclaringClass());
                query.append(", p").append(i + 1).append(".")
                    .append(dbColumn != null ? dbColumn : ad.getName()).append(" a").append(ni);
            }
        }
    }

}
