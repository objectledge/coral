package org.objectledge.coral.tools;

import org.objectledge.authentication.AuthenticationContext;
import org.objectledge.authentication.DefaultPrincipal;
import org.objectledge.authentication.UserManager;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.parameters.Parameters;

/**
 * A user data object used to access various user properties.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: UserTool.java,v 1.1 2005-03-01 10:30:35 zwierzem Exp $
 */
public class UserTool
{
	private UserManager userManager;
    private AuthenticationContext authenticationContext;
    private CoralSession coralSession;
    

	public UserTool(CoralSession coralSession, UserManager userManager,
        AuthenticationContext authenticationContex)
	{
        this.coralSession = coralSession;
        this.userManager = userManager;
        this.authenticationContext = authenticationContex;
	}

	// subjects - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	/**
	 * Return the subject.
	 *
	 * @return the subject.
	 */
	public Subject getSubject()
	{
	    return getCoralSession().getUserSubject();
	}

	/**
	 * Returns the login of the user.
	 *
	 * @return the user login.
	 */
	public String getLogin()
		throws Exception
	{
		return userManager.getLogin(getSubject().getName());
	}

	/**
	 * Returns the user personal data container.
	 *
	 * @return the parameters container.
	 */
	public Parameters getPersonalData()
		throws Exception
	{
		return userManager.getPersonalData(new DefaultPrincipal(getSubject().getName()));
	}

    /**
     * Returns the login of the user.
     *
     * @return the user login.
     */
    public boolean isUserAuthenticated()
        throws Exception
    {
        return authenticationContext.isUserAuthenticated();
    }

	// role & permission checking - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	/**
	 * Checks whether subject has a role.
	 *
	 * @param role the role.
	 */
	public boolean hasRole(String role)
		throws Exception
	{
		Role roleEntity = getCoralSession().getSecurity().getUniqueRole(role);
		return getSubject().hasRole(roleEntity);
	}

	/**
	 * Checks rights to the resource.
	 *
	 * @param resource the resource.
	 * @param permission the permission.
	 */
	public boolean hasPermission(Resource resource, String permission)
		throws Exception
	{
		Permission permissionEntity = getCoralSession().getSecurity().
			getUniquePermission(permission);
		return getSubject().hasPermission(resource, permissionEntity);
	}

	// private methods - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    
    private CoralSession getCoralSession()
    {
        return coralSession;
    }
}

