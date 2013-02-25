package org.objectledge.coral.web.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.objectledge.web.rest.JerseyConfigurationHook;

public class RegisterCoralFilters
    implements JerseyConfigurationHook
{

    @Override
    public void configure(ResourceConfig config)
    {
        config.register(RequireCoralRoleFilter.class);
        config.register(RequireCoralPermissionFilter.class);
        config.register(RequireAtLeastOneFilter.class);
    }

}
