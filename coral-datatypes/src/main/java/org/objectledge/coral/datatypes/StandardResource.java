// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//  
// * Redistributions of source code must retain the above copyright notice,  
//	 this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//	 this list of conditions and the following disclaimer in the documentation  
//	 and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//	 nor the names of its contributors may be used to endorse or promote products  
//	 derived from this software without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
// OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
// POSSIBILITY OF SUCH DAMAGE. 
// 
package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;

/**
 * Common base class for Resource data objects implementations.
 * <p>
 * Upper layer of the implemntation is provides update, revert and loadAttribute operatons that
 * interact with the database.
 * </p>
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 */
public class StandardResource
    extends ResourceAPISupport
{
    /**
     * Updates the image of the resource in the persistent storage.
     * 
     * @throws UnknownAttributeException if attribute is unknown.
     */
    public synchronized void update()
        throws UnknownAttributeException
    {
        Connection conn = null;
        boolean controler = false;
        try
        {
            controler = getDatabase().beginTransaction();
            conn = getDatabase().getConnection();
            handler(this).update(this, conn);
            getDatabase().commitTransaction(controler);
            clearModified();
        }
        catch(Exception e)
        {
            try
            {
                getDatabase().rollbackTransaction(controler);
            }
            catch(SQLException ee)
            {
                getLogger().error("Failed to rollback transaction", ee);
            }
            throw new BackendException("Failed to update resource", e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(SQLException ee)
                {
                    getLogger().error("Failed to close connection", ee);
                }
            }
        }
    }

    /**
     * Reverts the Resource object to the state present in the persistent storage.
     */
    public synchronized void revert()
    {
        Connection conn = null;
        try
        {
            conn = getDatabase().getConnection();
            handler(this).revert(this, conn, null);
        }
        catch(SQLException e)
        {
            throw new BackendException("Failed to revert resource", e);
        }
        finally
        {
            DatabaseUtils.close(conn);
        }
    }

    private <T extends Resource> ResourceHandler<T> handler(T resource)
    {
        return ((ResourceClass<T>)resource.getResourceClass()).getHandler();
    }

    /**
     * @return Returns the database.
     */
    private Database getDatabase()
    {
        return ((StandardResourceHandler<?>)delegate.getResourceClass().getHandler()).getDatabase();
    }

    /**
     * @return Returns the logger.
     */
    protected Logger getLogger()
    {
        return ((StandardResourceHandler<?>)delegate.getResourceClass().getHandler()).getLogger();
    }
}
