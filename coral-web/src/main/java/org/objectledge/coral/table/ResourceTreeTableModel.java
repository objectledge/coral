package org.objectledge.coral.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableException;

/**
 * A variant of ResourceListTableModel that exposes tree structure of the provided resource list.
 * 
 * @author rafal.krzewski@caltha.pl
 * @param <T>
 */
public class ResourceTreeTableModel<T extends Resource>
    extends ResourceListTableModel<T>
{
    private Map<T, T[]> childrenByParentId;

    public ResourceTreeTableModel(List<T> list, Locale locale)
        throws TableException
    {
        super(list, locale);
    }

    public ResourceTreeTableModel(T[] array, Locale locale)
        throws TableException
    {
        super(array, locale);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] getChildren(T parent)
    {
        if(childrenByParentId == null)
        {
            childrenByParentId = new HashMap<>();
        }
        T[] children = childrenByParentId.get(parent);
        if(children == null)
        {
            children = (T[])findChildren(parent);
            childrenByParentId.put(parent, children);
        }
        return children;
    }

    private Resource[] findChildren(Resource parent)
    {
        ArrayList<T> children = new ArrayList<>();
        if(parent == null)
        {
            for(Map.Entry<String, T> entry : resourcesById.entrySet())
            {
                if(entry.getValue().getId() == CoralStore.ROOT_RESOURCE
                    || getObject(entry.getValue().getParent().getIdString()) == null)
                {
                    children.add(entry.getValue());
                }
            }
        }
        else
        {
            for(Map.Entry<String, T> entry : resourcesById.entrySet())
            {
                if(entry.getValue().getParent() != null
                    && entry.getValue().getParent().getId() == parent.getId())
                {
                    children.add(entry.getValue());
                }
            }
        }
        return children.toArray(new Resource[children.size()]);
    }
}
