package org.objectledge.coral.web.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.Resource;

public class RequireCoralPermissionFilter
    implements ContainerRequestFilter
{

    @Inject
    private CoralSessionFactory coralSessionFactory;

    @Context
    Provider<ResourceInfo> resourceInfo;

    @Context
    Provider<UriInfo> uriInfo;


    @Override
    public void filter(ContainerRequestContext requestContext)
        throws IOException
    {
        final Annotation[][] parameterAnnotations = resourceInfo.get().getResourceMethod()
            .getParameterAnnotations();
        for(Annotation[] annotationsForParameter : parameterAnnotations)
        {
            Collection<String> permissions = null;
            String queryParamName = null;
            String pathParamName = null;
            for(Annotation annotation : annotationsForParameter)
            {
                if(annotation instanceof RequireCoralPermission)
                {
                    if(permissions == null)
                    {
                        permissions = new ArrayList<>();
                    }
                    RequireCoralPermission requireCoralPermission = (RequireCoralPermission)annotation;
                    permissions.add(requireCoralPermission.value());
                }
                else if(annotation instanceof QueryParam)
                {
                    queryParamName = QueryParam.class.cast(annotation).value();
                }
                else if(annotation instanceof PathParam)
                {
                    pathParamName = QueryParam.class.cast(annotation).value();
                }
            }
            if((queryParamName == null && permissions != null && pathParamName == null))
            {
                throw new RuntimeException("Misuse of annotations");
            }
            Collection<String> resources = null;
            if(queryParamName != null && permissions != null)
            {
                final List<String> resourceIds = uriInfo.get().getQueryParameters()
                    .get(queryParamName);
                resources = resourceIds;
            }
            else if(pathParamName != null && permissions != null)
            {
                final String resourceId = uriInfo.get().getPathParameters().getFirst(pathParamName);
                resources = Arrays.asList(resourceId);
            }

            if(resources != null)
            {
                final CoralSession session = coralSessionFactory.getCurrentSession();
                final Set<Permission> coralPermissions = getCoralPermissions(permissions, session);
                final Subject userSubject = session.getUserSubject();
                for(String resourceId : resources)
                {
                    checkPermissions(resourceId, coralPermissions, userSubject, session);
                }
            }
        }
    }

    private Set<Permission> getCoralPermissions(Collection<String> permissions, CoralSession session)
    {
        Set<Permission> coralPermissions = new HashSet<>();
        for(String permission : permissions)
        {
            coralPermissions.addAll(Arrays.asList(session.getSecurity().getPermission(permission)));
        }
        return coralPermissions;
    }

    private void checkPermissions(String resourceId, Collection<Permission> permissions,
        Subject user, CoralSession session)
    {
        try
        {
            final Resource resource = session.getStore().getResource(Long.valueOf(resourceId));
            for(Permission permission : permissions)
            {
                if(!user.hasPermission(resource, permission))
                {
                    throw new WebApplicationException(401);
                }
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new WebApplicationException(Response.status(404).entity(e).build());
        }
    }

}
