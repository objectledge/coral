package org.objectledge.coral.security;

import org.objectledge.collections.ImmutableSet;

public interface RoleContainerManager
{
    public RoleContainer getRoleContainer(ImmutableSet<Role> roles);
}
