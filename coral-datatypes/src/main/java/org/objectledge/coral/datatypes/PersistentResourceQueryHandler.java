package org.objectledge.coral.datatypes;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;

public class PersistentResourceQueryHandler
    extends BaseResourceQueryHandler
{

    protected void appendDataFromClause(StringBuilder query, ResultColumn<?> rcm)
    {
        final String dbTable = rcm.getRClass().getDbTable();
        if(dbTable != null)
        {
            query.append("\nJOIN ").append(dbTable).append(" p ON (r.resource_id = p.resource_id)");
        }
    }

    protected boolean appendDataWhereClause(StringBuilder query, ResultColumn<?> rcm,
        boolean whereStarted)
    {
        return whereStarted;
    }

    protected void appendAttributes(StringBuilder query, ResultColumn<?> rcm)
    {
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                final String dbColumn = ad.getDbColumn();
                query.append(", p.").append(dbColumn != null ? dbColumn : ad.getName())
                    .append(" a").append(j + 1);
            }
        }
    }
}
