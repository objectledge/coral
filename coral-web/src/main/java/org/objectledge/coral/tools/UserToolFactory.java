// 
//Copyright (c) 2003, 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
//All rights reserved. 
//   
//Redistribution and use in source and binary forms, with or without modification,  
//are permitted provided that the following conditions are met: 
//   
//* Redistributions of source code must retain the above copyright notice,  
//this list of conditions and the following disclaimer. 
//* Redistributions in binary form must reproduce the above copyright notice,  
//this list of conditions and the following disclaimer in the documentation  
//and/or other materials provided with the distribution. 
//* Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//nor the names of its contributors may be used to endorse or promote products  
//derived from this software without specific prior written permission. 
// 
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
//WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
//IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
//INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
//BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
//OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
//WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
//POSSIBILITY OF SUCH DAMAGE. 
//

package org.objectledge.coral.tools;

import org.objectledge.authentication.AuthenticationContext;
import org.objectledge.authentication.UserManager;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.templating.tools.ContextToolFactory;

/**
 * Context tool factory component to build the user tool.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: UserToolFactory.java,v 1.2 2005-05-20 00:46:07 rafal Exp $
 */
public class UserToolFactory implements ContextToolFactory
{
    private Context context;
    private UserManager userManager;

    /**
     * Creates a new UserToolFactory instance.
     * 
     * @param context request Context component.
     * @param userManager UserManager component.
     */
    public UserToolFactory(Context context, UserManager userManager)
    {
        this.context = context;
        this.userManager = userManager;
    }
    
    /**
     * Creates the user tool.
     * 
	 * @return creates the user tool. 
	 */
	public Object getTool()
	{
        CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
        AuthenticationContext authenticationContext = AuthenticationContext
            .getAuthenticationContext(context);
		return new UserTool(coralSession, userManager, authenticationContext);
	}
	
	/**
	 * Does nothing - user tool is to simple to pool.
	 */
	public void recycleTool(Object tool)
	{
		//do nothing UserTool is too simple object to be pooled
	}

	/**
     * Returns a templating context key for the user tool.
     * 
     * @return the user tool context key (<coce>"userTool"</code>). 
	 */
	public String getKey()
	{
		return "userTool";
	}    
}
