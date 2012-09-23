package org.objectledge.coral.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectledge.coral.entity.Entity;
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
        Map<ResourceQueryHandler, List<AttributeDefinition<?>>> attrHandlers = partitionByHandler(rcm);
        List<ResourceClass<?>> hostClasses = hostClasses(rcm);
        query.append("(SELECT ");
        appendBuiltinAttributes(query, rcm, bulitinAttrNames);
        for(ResourceQueryHandler qh : attrHandlers.keySet())
        {
            qh.appendAttributes(query, rcm, attrHandlers.get(qh), hostClasses);
        }
        query.append("\nFROM coral_resource r");
        for(ResourceQueryHandler qh : attrHandlers.keySet())
        {
            qh.appendDataFromClause(query, rcm, attrHandlers.get(qh), hostClasses, false);
        }
        for(ResourceQueryHandler qh : attrHandlers.keySet())
        {
            qh.appendDataFromClause(query, rcm, attrHandlers.get(qh), hostClasses, true);
        }
        boolean whereStarted = false;
        if(restrictClasses)
        {
            whereStarted = appendWhere(query, whereStarted);
            appendResourceClassWhereClause(query, rcm);
        }
        for(ResourceQueryHandler qh : attrHandlers.keySet())
        {
            whereStarted = qh.appendDataWhereClause(query, rcm, attrHandlers.get(qh), hostClasses,
                whereStarted);
        }
        query.append(") r").append(rcm.getIndex());
    }

    private Map<ResourceQueryHandler, List<AttributeDefinition<?>>> partitionByHandler(
        ResultColumn<?> rcm)
    {
        Map<ResourceQueryHandler, List<AttributeDefinition<?>>> map = new HashMap<ResourceQueryHandler, List<AttributeDefinition<?>>>();
        for(AttributeDefinition<?> ad : rcm.getAttributes())
        {
            ResourceQueryHandler qh = ad.getDeclaringClass().getHandler().getQueryHandler();
            List<AttributeDefinition<?>> as = map.get(qh);
            if(as == null)
            {
                as = new ArrayList<AttributeDefinition<?>>();
                map.put(qh, as);
            }
            as.add(ad);
        }
        return map;
    }

    private List<ResourceClass<?>> hostClasses(ResultColumn<?> rcm)
    {
        List<ResourceClass<?>> hostClasses = new ArrayList<ResourceClass<?>>();
        for(AttributeDefinition<?> ad : rcm.getAttributes())
        {
            final ResourceClass<?> declaringClass = ad.getDeclaringClass();
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0 && declaringClass.getDbTable() != null)
            {
                if(!hostClasses.contains(declaringClass))
                {
                    hostClasses.add(declaringClass);
                }
            }
        }
        Collections.sort(hostClasses, new Comparator<Entity>()
            {
                @Override
                public int compare(Entity o1, Entity o2)
                {
                    return (int)(o1.getId() - o2.getId());
                }
            });
        return hostClasses;
    }

    private void appendBuiltinAttributes(StringBuilder query, ResultColumn<?> rcm,
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
