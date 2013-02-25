package org.objectledge.coral.web.rest;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class CoralPermissionCheckerImpl
    implements CoralPermissionChecker
{
    
    @Override
    public boolean hasPermission(RequireCoralPermission requireCoralPermission,
        UriInfo uriInfo, ResourceInfo resourceInfo, CoralSession session)
        throws EntityDoesNotExistException
    {
        validate(requireCoralPermission);
        Permission permission = getPermission(requireCoralPermission.permission(), session);
        final Collection<Long> resourceIds = getResourceId(requireCoralPermission,
            uriInfo);
        Collection<Resource> resources = getResources(resourceIds, session);
        final Subject user = session.getUserSubject();
        for(Resource resource : resources)
        {
            if(!user.hasPermission(resource, permission))
            {
                return false;
            }
        }
        return true;
    }

    Collection<Resource> getResources(Collection<Long> resourceIds, CoralSession session)
        throws EntityDoesNotExistException
    {
        Collection<Resource> resources = new ArrayList<>();
        for(Long resourceId : resourceIds)
        {
            resources.add(session.getStore().getResource(resourceId.longValue()));
        }
        return resources;
    }

    Collection<Long> getResourceId(RequireCoralPermission requireCoralPermission, UriInfo uriInfo)
    {
        if(requireCoralPermission.queryParam().equals(""))
        {
            // find resource using id from path
            return Arrays.asList(Long.valueOf(uriInfo.getPathParameters()
                .getFirst(requireCoralPermission.pathParam())));
        }
        else
        {
            // find resource using id from query parameters
            final List<String> ids = uriInfo.getQueryParameters()
                .get(requireCoralPermission.queryParam());
            return Collections2.transform(ids, new Function<String, Long>()
                {
                    @Override
                    @Nullable
                    public Long apply(@Nullable String resourceId)
                    {
                        return Long.valueOf(resourceId);
                    }
                });
        }
    }

    /**
     * Checks if annotation was used correctly. Throws RuntimeException if misused
     * 
     * @param requireCoralPermission
     */
    void validate(RequireCoralPermission requireCoralPermission)
    {
        if((!requireCoralPermission.pathParam().equals("id"))
            && (!requireCoralPermission.queryParam().equals("")))
        {
            // both pathParam and queryParam were specified which is wrong
            throw new RuntimeException(
                "RequireCoralPermission annotation was misused. Check javadoc for correct usage");
        }
    }

    Permission getPermission(String permission, CoralSession session)
    {
        return session.getSecurity().getUniquePermission(permission);
    }

    /**
     * Finds RequireCoralPermissions
     * 
     * @param annotations from method
     * @return collection with annotations or empty list, never null
     */
    @Override
    public Collection<RequireCoralPermission> findRequireCoralPermissions(Annotation[] annotations)
    {
        Collection<RequireCoralPermission> requireCoralPermissions = null;
        for(Annotation annotation : annotations)
        {
            if(annotation instanceof RequireCoralPermission)
            {
                final RequireCoralPermission requireCoralPermission = RequireCoralPermission.class
                    .cast(annotation);
                if(requireCoralPermissions == null)
                {
                    requireCoralPermissions = new ArrayList<>();
                }
                requireCoralPermissions.add(requireCoralPermission);
            }
        }
        if(requireCoralPermissions != null)
        {
            return requireCoralPermissions;
        }
        else
        {
            return Collections.emptyList();
        }
    }

}
