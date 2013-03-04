package org.objectledge.coral.web.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;

import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;

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
                RequireCoralRole requireCoralRole = (RequireCoralRole)annotation;
                final String requiredRole = requireCoralRole.value();
                final CoralSession currentSession = coralSessionFactory.getCurrentSession();
                Role coralRole = currentSession.getSecurity().getUniqueRole(requiredRole);
                if(!currentSession.getUserSubject().hasRole(coralRole))
                {
                    throw new WebApplicationException(401);
                }
            }
        }
    }

}
