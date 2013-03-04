package org.objectledge.coral.web.rest.security;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.web.rest.RequireCoralPermission;

public class PermissionProcessor
    implements SecurityConstraintProcessor<RequireCoralPermission>
{
    private final CoralSessionFactory coralSessionFactory;

    private final CoralPermissionChecker permissionChecker;

    private final UriInfo uriInfo;

    private final ResourceInfo resourceInfo;

    public PermissionProcessor(CoralSessionFactory coralSessionFactory,
        CoralPermissionChecker permissionChecker, UriInfo uriInfo, ResourceInfo resourceInfo)
    {
        this.coralSessionFactory = coralSessionFactory;
        this.permissionChecker = permissionChecker;
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
    }

    @Override
    public void process(RequireCoralPermission requireCoralPermission)
        throws SecurityViolationException
    {
        CoralSession session = coralSessionFactory.getCurrentSession();
        try
        {
            if(!permissionChecker.hasPermission(requireCoralPermission, uriInfo, resourceInfo,
                session))
            {
                throw new SecurityViolationException("Missing permission: '"
                    + requireCoralPermission.permission() + "'");
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("unexpected error", e);
        }
    }
}
