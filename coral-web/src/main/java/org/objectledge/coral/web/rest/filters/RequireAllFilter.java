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
import org.objectledge.coral.web.rest.RequireAll;
import org.objectledge.coral.web.rest.security.RequireAllProcessor;
import org.objectledge.coral.web.rest.security.CoralPermissionChecker;
import org.objectledge.coral.web.rest.security.SecurityViolationException;

@RequireAll
public class RequireAllFilter
    implements ContainerRequestFilter
{

    @Inject
    private Provider<ResourceInfo> resourceInfo;

    @Inject
    private CoralSessionFactory coralSessionFactory;

    @Inject
    private Provider<UriInfo> uriInfo;

    @Inject
    private CoralPermissionChecker permissionChecker;

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException
    {
        final RequireAll requireAll = resourceInfo.get().getResourceMethod()
            .getAnnotation(RequireAll.class);
        if(requireAll == null)
        {
            return;
        }
        final RequireAllProcessor allProcessor = new RequireAllProcessor(coralSessionFactory, resourceInfo.get(), uriInfo.get(), permissionChecker);
        try
        {
            allProcessor.process(requireAll);
        }
        catch(SecurityViolationException e)
        {
            throw new WebApplicationException(401);
        }
    }

}
