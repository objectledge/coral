package org.objectledge.coral.web.rest.filters;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.web.rest.RequireAny;
import org.objectledge.coral.web.rest.security.RequireAnyProcessor;
import org.objectledge.coral.web.rest.security.CoralPermissionChecker;
import org.objectledge.coral.web.rest.security.SecurityViolationException;

@RequireAny
public class RequireAnyFilter
    implements ContainerRequestFilter
{

    @Inject
    private Provider<ResourceInfo> resourceInfo;

    @Inject
    private CoralSessionFactory coralSessionFactory;

    @Inject
    private CoralPermissionChecker permissionChecker;

    @Inject
    private Provider<UriInfo> uriInfo;

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException
    {
        final RequireAny requireAny = resourceInfo.get().getResourceMethod()
            .getAnnotation(RequireAny.class);
        if(requireAny == null)
        {
            return;
        }
        final RequireAnyProcessor anyProcessor = new RequireAnyProcessor(coralSessionFactory, permissionChecker, uriInfo.get(), resourceInfo.get());
        try
        {
            anyProcessor.process(requireAny);
        }
        catch(SecurityViolationException e)
        {
            throw new WebApplicationException(401);
        }
    }

}
