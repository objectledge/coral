package org.objectledge.coral.datatypes;

import java.util.Map;

import org.objectledge.coral.query.ResourceQueryHandler;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;

public abstract class BaseResourceQueryHandler
    implements ResourceQueryHandler
{
    public void appendFromClause(StringBuilder query, ResultColumn<?> rcm,
        Map<String, String> bulitinAttrNames, boolean restrictClasses)
    {
        query.append("(SELECT ");
        appendBuiltinAttributes(query, rcm, bulitinAttrNames);
        appendAttributes(query, rcm);
        query.append("\nFROM coral_resource r");
        appendDataFromClause(query, rcm);
        boolean whereStarted = false;
        if(restrictClasses)
        {
            whereStarted = appendWhere(query, whereStarted);
            appendResourceClassWhereClause(query, rcm);
        }
        whereStarted = appendDataWhereClause(query, rcm, whereStarted);
        query.append(") r").append(rcm.getIndex());
    }

    protected abstract boolean appendDataWhereClause(StringBuilder query, ResultColumn<?> rcm,
        boolean whereStarted);

    protected abstract void appendDataFromClause(StringBuilder query, ResultColumn<?> rcm);

    protected abstract void appendAttributes(StringBuilder query, ResultColumn<?> rcm);

    protected void appendBuiltinAttributes(StringBuilder query, ResultColumn<?> rcm,
        Map<String, String> bulitinAttrNames)
    {
        query.append("r.resource_id, r.resource_class_id");
        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) != 0 && !ad.getName().equals("id")
                && !ad.getName().equals("resource_class"))
            {
                query.append(", ").append("r.").append(bulitinAttrNames.get(ad.getName()));
            }
        }
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

    protected boolean appendWhere(StringBuilder query, boolean whereStarted)
    {
        if(whereStarted)
        {
            query.append("\nAND ");
        }
        else
        {
            query.append("\nWHERE ");
        }
        return true;
    }
}
