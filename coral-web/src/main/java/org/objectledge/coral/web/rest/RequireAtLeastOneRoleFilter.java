package org.objectledge.coral.web.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;

import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;

@RequireAtLeastOneRole({})
public class RequireAtLeastOneRoleFilter
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
        final RequireAtLeastOneRole annotation = resourceInfo.get().getResourceMethod()
            .getAnnotation(RequireAtLeastOneRole.class);
        if(annotation != null)
        {
            final RequireCoralRole[] requiredRoles = annotation.value();
            final CoralSession session = coralSessionFactory.getCurrentSession();
            final Subject user = session.getUserSubject();
            for(RequireCoralRole requiredRole : requiredRoles)
            {
                final Role role = session.getSecurity().getUniqueRole(requiredRole.value());
                if(user.hasRole(role))
                {
                    return;
                }
            }
            throw new WebApplicationException(401);
        }
    }
}
