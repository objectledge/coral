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
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;

/**
 * Common base class for Resource data objects implementations. 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AbstractResource.java,v 1.40 2007-11-18 21:20:30 rafal Exp $
 */
public abstract class AbstractResource
    extends ResourceAPISupport
{
    synchronized void retrieve(Resource delegate, ResourceClass<?> rClass, Connection conn, 
        Object data)
    	throws SQLException
    {
        setDelegate(delegate);
        for(ResourceClass<?> parent : rClass.getDirectParentClasses())
        {
            retrieve(delegate, parent, conn, data);
        }
    }

    synchronized void create(Resource delegate, ResourceClass<?> rClass,
        Map<AttributeDefinition<?>, ?> attributes, Connection conn)
        throws SQLException, ValueRequiredException, ConstraintViolationException
    {
        setDelegate(delegate);
        for (ResourceClass<?> parent : rClass.getDirectParentClasses())
        {
            parent.getHandler().create(delegate, attributes, conn);
        }
        for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
	            Object value = attributes.get(attr);
	            if(value == null)
	            {
	                if((attr.getFlags() & AttributeFlags.REQUIRED) != 0)
	                {
	                    throw new ValueRequiredException("value for REQUIRED attribute "+
	                                                     attr.getName()+" is missing");
	                }
	            }
            }
        }
    }
    
    synchronized void revert(ResourceClass<?> rClass, Connection conn, Object data)
	    throws SQLException
	{
        // recreate arrays - size might have changed
        resetAttributes();
        for(ResourceClass<?> parent : rClass.getDirectParentClasses())
	    {
            revert(parent, conn, data);
	    }
	}

    synchronized void update(ResourceClass<?> rClass, Connection conn)
	    throws SQLException
	{
        for(ResourceClass<?> parent : rClass.getDirectParentClasses())
        {
            update(parent, conn);
        }
	}
    
	synchronized void delete(ResourceClass<?> rClass, Connection conn)
	    throws SQLException
	{
	    for(ResourceClass<?> parent : rClass.getDirectParentClasses())
        {
            delete(parent, conn);
        }
	}

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
            update(delegate.getResourceClass(), conn);
            delegate.update();    
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
     * Reverts the Resource object to the state present in the persistent
     * storage. 
     */
    public synchronized void revert()
    {
        Connection conn = null;
        try
        {
            conn = getDatabase().getConnection();
            AbstractResourceHandler<?> handler = (AbstractResourceHandler<?>)delegate.getResourceClass()
                .getHandler();
            Object data = handler.getData(delegate, conn, null); 
            revert(delegate.getResourceClass(), conn, data);
        }
        catch(SQLException e)
        {
            throw new BackendException("Failed to revert resource", e);
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
     * Lazy-load attribute value.
     * 
     * @param attribute the attribute definition.
     * @param aId attribute value id.
     * @return attribute value.
     */
    protected <T> T loadAttribute(AttributeDefinition<T> attribute, long aId)
    {
        Connection conn = null;
        try
        {
            conn = getDatabase().getConnection();
            return attribute.getAttributeClass().getHandler().
                retrieve(aId, conn);
        }
        catch(Exception e)
        {
            if(delegate == null)
            {
                throw new BackendException("failed to retrieve attribute value " +
                    "(attribute definition = "+attribute.getName()+
                    " , attribute id = "+aId+")", e);
            }
            else
            {
                throw new BackendException("failed to retrieve attribute value " +
                    "(attribute definition = "+attribute.getName()+
                    " , attribute id = "+aId+") for resource: "+delegate.getIdString() , e);
            }
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
     * @return Returns the database.
     */
    private Database getDatabase()
    {
        return ((AbstractResourceHandler<?>)delegate.getResourceClass().getHandler()).getDatabase();
    }

    /**
     * @return Returns the logger.
     */
    protected Logger getLogger()
    {
        return ((AbstractResourceHandler<?>)delegate.getResourceClass().getHandler()).getLogger();
    }
}
