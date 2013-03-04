package org.objectledge.coral.web.rest.security;

import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.web.rest.RequireCoralPermission;

public interface CoralPermissionChecker
{
    boolean hasPermission(RequireCoralPermission requireCoralPermission, UriInfo uriInfo,
        ResourceInfo resourceInfo, CoralSession session)
        throws EntityDoesNotExistException;

    Collection<RequireCoralPermission> findRequireCoralPermissions(Annotation[] annotations);
}
