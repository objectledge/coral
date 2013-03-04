package org.objectledge.coral.web.rest.filters;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.web.rest.RequireCoralPermission;
import org.objectledge.coral.web.rest.security.CoralPermissionChecker;
import org.objectledge.coral.web.rest.security.PermissionProcessor;
import org.objectledge.coral.web.rest.security.SecurityViolationException;

@RequireCoralPermission(permission = "*")
public class RequireCoralPermissionFilter
    implements ContainerRequestFilter
{

    @Inject
    private CoralSessionFactory coralSessionFactory;

    @Inject
    Provider<ResourceInfo> resourceInfo;

    @Inject
    Provider<UriInfo> uriInfo;

    @Inject
    CoralPermissionChecker permissionChecker;

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException
    {
        final Annotation[] annotations = resourceInfo.get().getResourceMethod().getAnnotations();
        Collection<RequireCoralPermission> requireCoralPermissions = permissionChecker
            .findRequireCoralPermissions(annotations);
        final PermissionProcessor permissionProcessor = new PermissionProcessor(
            coralSessionFactory, permissionChecker, uriInfo.get(), resourceInfo.get());
        for(RequireCoralPermission requireCoralPermission : requireCoralPermissions)
        {
            try
            {
                permissionProcessor.process(requireCoralPermission);
            }
            catch(SecurityViolationException e)
            {
                throw new WebApplicationException(Response.status(401).entity(e.getMessage())
                    .build());
            }
        }
    }
}
