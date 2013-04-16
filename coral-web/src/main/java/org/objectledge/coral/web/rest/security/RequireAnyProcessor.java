package org.objectledge.coral.web.rest.security;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.web.rest.RequireAny;
import org.objectledge.coral.web.rest.RequireCoralPermission;
import org.objectledge.coral.web.rest.RequireCoralRole;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class RequireAnyProcessor
    implements SecurityConstraintProcessor<RequireAny>
{

    private final CoralSessionFactory coralSessionFactory;

    private final CoralPermissionChecker permissionChecker;

    private final UriInfo uriInfo;

    private final ResourceInfo resourceInfo;

    public RequireAnyProcessor(CoralSessionFactory coralSessionFactory,
        CoralPermissionChecker permissionChecker, UriInfo uriInfo, ResourceInfo resourceInfo)
    {
        this.coralSessionFactory = coralSessionFactory;
        this.permissionChecker = permissionChecker;
        this.uriInfo = uriInfo;
        this.resourceInfo = resourceInfo;
    }

    @Override
    public void process(RequireAny requireAny)
        throws SecurityViolationException
    {
        final RequireCoralRole[] arrayOfRoles = requireAny.roles();
        final List<RequireCoralRole> roles = Arrays.asList(arrayOfRoles);
        final RoleProcessor roleProcessor = new RoleProcessor(coralSessionFactory);
        final boolean hasSomeRole = Iterables.any(roles, new Predicate<RequireCoralRole>()
            {
                @Override
                public boolean apply(RequireCoralRole requireCoralRole)
                {
                    try
                    {
                        roleProcessor.process(requireCoralRole);
                        return true;
                    }
                    catch(SecurityViolationException e)
                    {
                        return false;
                    }
                }
            });
        if(!hasSomeRole)
        {
            final List<RequireCoralPermission> permissions = Arrays.asList(requireAny.permissions());
            final PermissionProcessor permissionProcessor = new PermissionProcessor(coralSessionFactory, permissionChecker, uriInfo, resourceInfo);
            final boolean hasAnyPermission = Iterables.any(permissions,
                new Predicate<RequireCoralPermission>()
                {

                    @Override
                    public boolean apply(RequireCoralPermission requireCoralPermission)
                    {
                        try
                        {
                            permissionProcessor.process(requireCoralPermission);
                            return true;
                        }
                        catch(SecurityViolationException e)
                        {
                            return false;
                        }
                    }
                });
            if(!hasAnyPermission)
            {
                throw new SecurityViolationException();
            }
        }
    }
}
