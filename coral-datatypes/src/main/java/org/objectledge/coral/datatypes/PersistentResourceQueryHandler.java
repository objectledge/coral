package org.objectledge.coral.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;

public class PersistentResourceQueryHandler
    extends BaseResourceQueryHandler
{
    protected void appendDataFromClause(StringBuilder query, ResultColumn<?> rcm)
    {
        final List<ResourceClass<?>> hostClasses = hostClasses(rcm);
        for(int i = 0; i < hostClasses.size(); i++)
        {
            final ResourceClass<?> rc = hostClasses.get(i);
            query.append("\nJOIN ").append(rc.getDbTable()).append(" p").append(i + 1);
            query.append(" ON (r.resource_id = p").append(i + 1);
            query.append(".resource_id)");
        }
    }

    protected boolean appendDataWhereClause(StringBuilder query, ResultColumn<?> rcm,
        boolean whereStarted)
    {
        return whereStarted;
    }

    protected void appendAttributes(StringBuilder query, ResultColumn<?> rcm)
    {
        final List<ResourceClass<?>> hostClasses = hostClasses(rcm);

        for(int j = 0; j < rcm.getAttributes().size(); j++)
        {
            AttributeDefinition<?> ad = rcm.getAttributes().get(j);
            if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                final String dbColumn = ad.getDbColumn();
                final int i = hostClasses.indexOf(ad.getDeclaringClass());
                query.append(", p").append(i + 1).append(".")
                    .append(dbColumn != null ? dbColumn : ad.getName()).append(" a").append(j + 1);
            }
        }
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
}
