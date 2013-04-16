package org.objectledge.coral.web.rest.filters;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;

import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.web.rest.RequireCoralRole;
import org.objectledge.coral.web.rest.security.RoleProcessor;
import org.objectledge.coral.web.rest.security.SecurityConstraintProcessor;
import org.objectledge.coral.web.rest.security.SecurityViolationException;

@RequireCoralRole("thisIsJustRequiredValue")
public class RequireCoralRoleFilter
    implements ContainerRequestFilter
{

    @Inject
    private CoralSessionFactory coralSessionFactory;

    @Inject
    Provider<ResourceInfo> resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException
    {
        final Annotation[] annotations = resourceInfo.get().getResourceMethod().getAnnotations();
        for(Annotation annotation : annotations)
        {
            if(annotation instanceof RequireCoralRole)
            {
                final SecurityConstraintProcessor<RequireCoralRole> processor = new RoleProcessor(
                    coralSessionFactory);
                try
                {
                    processor.process((RequireCoralRole)annotation);
                }
                catch(SecurityViolationException e)
                {
                    throw new WebApplicationException(401);
                }
            }
        }
    }

}
