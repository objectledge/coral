package org.objectledge.coral.datatypes;

import java.util.Map;

public class PersistentResourceQueryHandler
    extends BaseResourceQueryHandler
{
    @Override
    public void appendFromClause(StringBuilder query, ResultColumn<?> rcm,
        Map<String, String> bulitinAttrNames)
    {
        query.append("coral_resource r").append(rcm.getIndex());
        final String dbTable = rcm.getRClass().getDbTable();
        if(dbTable != null)
        {
            query.append(",\n").append(dbTable).append(" p").append(rcm.getIndex());
        }
    }

    @Override
    public boolean appendWhereClause(StringBuilder query, boolean whereStarted, ResultColumn<?> rcm)
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
        query.append("r").append(rcm.getIndex()).append(".resource_id = p").append(rcm.getIndex())
            .append(".resource_id");
        return whereStarted;
    }

    @Override
    public void appendAttributeTerm(StringBuilder query, ResultColumnAttribute<?, ?> rca)
    {
        String name = rca.getAttribute().getDbColumn();
        if(name == null)
        {
            name = rca.getAttribute().getName();
        }
        query.append("p").append(rca.getColumn().getIndex()).append(".").append(name);
    }
}
