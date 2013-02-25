package org.objectledge.coral.web.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;

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
        Collection<RequireCoralPermission> requireCoralPermissions = permissionChecker.findRequireCoralPermissions(annotations);
        try
        {
            CoralSession session = coralSessionFactory.getCurrentSession();
            for(RequireCoralPermission requireCoralPermission : requireCoralPermissions)
            {
                if(!permissionChecker.hasPermission(requireCoralPermission, uriInfo.get(),
                    resourceInfo.get(), session))
                {
                    throw new WebApplicationException(401);
                }
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new WebApplicationException(404);
        }
    }
}
