package org.objectledge.coral.web.rest.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;

import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.web.rest.RequireAll;
import org.objectledge.coral.web.rest.RequireCoralPermission;
import org.objectledge.coral.web.rest.RequireCoralRole;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class RequireAllProcessor
    implements SecurityConstraintProcessor<RequireAll>
{

    private final CoralSessionFactory coralSessionFactory;

    private final ResourceInfo resourceInfo;

    private final UriInfo uriInfo;

    private final CoralPermissionChecker permissionChecker;

    public RequireAllProcessor(CoralSessionFactory coralSessionFactory, ResourceInfo resourceInfo,
        UriInfo uriInfo, CoralPermissionChecker permissionChecker)
    {
        this.coralSessionFactory = coralSessionFactory;
        this.resourceInfo = resourceInfo;
        this.uriInfo = uriInfo;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public void process(RequireAll requireAll)
        throws SecurityViolationException
    {
        final List<RequireCoralRole> roles = Arrays.asList(requireAll.roles());
        final RoleProcessor roleProcessor = new RoleProcessor(coralSessionFactory);
        final boolean hasAllRoles = Iterables.all(roles, new Predicate<RequireCoralRole>()
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

        if(!hasAllRoles)
        {
            throw new WebApplicationException(401);
        }

        final PermissionProcessor permissionProcessor = new PermissionProcessor(
            coralSessionFactory, permissionChecker, uriInfo, resourceInfo);
        Collection<RequireCoralPermission> permissions = Arrays.asList(requireAll.permissions());
        final boolean hasAllPermissions = Iterables.all(permissions,
            new Predicate<RequireCoralPermission>()
            {

                @Override
                public boolean apply(RequireCoralPermission input)
                {
                    try
                    {
                        permissionProcessor.process(input);
                        return true;
                    }
                    catch(SecurityViolationException e)
                    {
                        return false;
                    }
                }
            });
        if(!hasAllPermissions)
        {
            throw new WebApplicationException(401);
        }
    }
}
