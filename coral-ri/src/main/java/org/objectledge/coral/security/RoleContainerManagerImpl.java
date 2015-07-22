package org.objectledge.coral.security;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.objectledge.collections.ImmutableSet;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.event.CoralEventHub;

public class RoleContainerManagerImpl
    implements RoleContainerManager
{
    private Map<ImmutableSet<Role>, WeakReference<RoleContainer>> containers = new HashMap<>();

    private CoralEventHub coralEventHub;

    private CoralCore coralCore;

    public RoleContainerManagerImpl(CoralEventHub coralEventHub, CoralCore coralCore)
    {
        this.coralEventHub = coralEventHub;
        this.coralCore = coralCore;
    }

    @Override
    public synchronized RoleContainer getRoleContainer(ImmutableSet<Role> roles)
    {
        WeakReference<RoleContainer> ref = containers.get(roles);
        RoleContainer container = null;
        if(ref != null)
        {
            container = ref.get();
        }
        if(container == null)
        {
            container = new RoleContainer(coralEventHub, coralCore, roles);
            containers.put(roles, new WeakReference<>(container));
        }
        return container;
    }
}
