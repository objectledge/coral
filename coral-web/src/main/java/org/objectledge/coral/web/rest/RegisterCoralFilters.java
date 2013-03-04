package org.objectledge.coral.web.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.objectledge.coral.web.rest.filters.RequireAllFilter;
import org.objectledge.coral.web.rest.filters.RequireAnyFilter;
import org.objectledge.coral.web.rest.filters.RequireCoralPermissionFilter;
import org.objectledge.coral.web.rest.filters.RequireCoralRoleFilter;
import org.objectledge.web.rest.JerseyConfigurationHook;

public class RegisterCoralFilters
    implements JerseyConfigurationHook
{

    @Override
    public void configure(ResourceConfig config)
    {
        config.register(RequireCoralRoleFilter.class);
        config.register(RequireCoralPermissionFilter.class);
        config.register(RequireAnyFilter.class);
        config.register(RequireAllFilter.class);
    }

}
