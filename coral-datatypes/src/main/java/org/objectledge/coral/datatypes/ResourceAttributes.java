package org.objectledge.coral.datatypes;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.store.Resource;

public interface ResourceAttributes
{
    Resource getDelegate();

    <A> A getValue(AttributeDefinition<A> attr);

    <A> void setValue(AttributeDefinition<A> attr, A value);

    long getValueId(AttributeDefinition<?> attr);

    void setValueId(AttributeDefinition<?> attr, long valueId);
    
    boolean isValueModified(AttributeDefinition<?> attr);
}
