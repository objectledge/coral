package org.objectledge.coral.datatypes;

import org.objectledge.coral.query.ResourceQueryHandler;

public abstract class BaseResourceQueryHandler
    implements ResourceQueryHandler
{
    public void appendResourceIdTerm(StringBuilder query, ResultColumn<?> rcm)
    {
        query.append("r").append(rcm.getIndex()).append(".resource_id");
    }
}
