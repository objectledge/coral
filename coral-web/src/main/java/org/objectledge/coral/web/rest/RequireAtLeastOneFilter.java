package org.objectledge.coral.web.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;

@RequireAtLeastOne({})
public class RequireAtLeastOneFilter
    implements ContainerRequestFilter
{

    @Inject
    private CoralSessionFactory coralSessionFactory;

    @Inject
    Provider<ResourceInfo> resourceInfo;

    @Inject
    CoralPermissionChecker permissionChecker;

    @Inject
    Provider<UriInfo> uriInfo;

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException
    {
        final RequireAtLeastOne requireAtLeastOne = resourceInfo.get().getResourceMethod()
            .getAnnotation(RequireAtLeastOne.class);
        if(requireAtLeastOne == null)
        {
            return;
        }
        final RequireCoralPermission[] requiredPermissions = requireAtLeastOne.value();
        validate(requiredPermissions);
        final CoralSession session = coralSessionFactory.getCurrentSession();
        for(RequireCoralPermission requireCoralPermission : requiredPermissions)
        {
            try
            {
                if(permissionChecker.hasPermission(requireCoralPermission, uriInfo.get(),
                    resourceInfo.get(), session))
                {
                    return;
                }
            }
            catch(EntityDoesNotExistException e)
            {
                throw new WebApplicationException(404);
            }
        }
        throw new WebApplicationException(401);
    }

    private void validate(final RequireCoralPermission[] requiredPermissions)
    {
        if(requiredPermissions.length == 0)
        {
            throw new RuntimeException(
                "Misused annotation @RequireAtLeastOne. Provide permissions to check!");
        }
    }


}
