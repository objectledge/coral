package org.objectledge.coral.web.rest.security;

import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.web.rest.RequireCoralRole;

public class RoleProcessor
    implements SecurityConstraintProcessor<RequireCoralRole>
{

    private final CoralSessionFactory coralSessionFactory;

    public RoleProcessor(CoralSessionFactory coralSessionFactory)
    {
        this.coralSessionFactory = coralSessionFactory;
    }

    @Override
    public void process(RequireCoralRole securityConstraint)
        throws SecurityViolationException
    {
        RequireCoralRole requireCoralRole = (RequireCoralRole)securityConstraint;
        final String requiredRole = requireCoralRole.value();
        final CoralSession currentSession = coralSessionFactory.getCurrentSession();
        Role coralRole = currentSession.getSecurity().getUniqueRole(requiredRole);
        if(!currentSession.getUserSubject().hasRole(coralRole))
        {
            throw new SecurityViolationException("Missing role; '" + coralRole.getName() + "'");
        }
    }

}
