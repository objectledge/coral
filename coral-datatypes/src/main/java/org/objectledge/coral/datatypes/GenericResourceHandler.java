package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Handles persistence of {@link GenericResource} objects.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResourceHandler.java,v 1.13 2005-01-20 13:09:40 rafal Exp $
 */
public class GenericResourceHandler
    extends AbstractResourceHandler
{
    // Member objects ////////////////////////////////////////////////////////

    /**
     * The constructor.
     * 
     * @param coralSchema the coral schema.
     * @param coralSecurity the coral security.
     * @param instantiator the instantiator.
     * @param resourceClass the resource class.
     */
    public GenericResourceHandler(CoralSchema coralSchema, CoralSecurity coralSecurity, 
        Instantiator instantiator, ResourceClass resourceClass)
    {
        super(coralSchema, coralSecurity, instantiator, resourceClass);
    }

    // Resource handler interface ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void addAttribute(AttributeDefinition attribute, 
                             Object value, Connection conn)
        throws ValueRequiredException, SQLException
    {
        addAttribute0(resourceClass, attribute, value, conn);
        ResourceClass[] subclasses = resourceClass.getChildClasses();
        for(int i=0; i<subclasses.length; i++)
        {
            subclasses[i].getHandler().addAttribute(attribute, value, conn);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAttribute(AttributeDefinition attribute, Connection conn)
        throws SQLException
    {
        deleteAttribute0(resourceClass, attribute, conn);
        ResourceClass[] subclasses = resourceClass.getChildClasses();
        for(int i=0; i<subclasses.length; i++)
        {
            deleteAttribute0(subclasses[i], attribute, conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addParentClass(ResourceClass parent, Map values, Connection conn)
        throws ValueRequiredException, SQLException
    {
        AttributeDefinition[] attrs = parent.getAllAttributes();
        addAttribute0(resourceClass, attrs, values, conn);
        ResourceClass[] subclasses = resourceClass.getChildClasses();
        for(int i=0; i<subclasses.length; i++)
        {
            subclasses[i].getHandler().addParentClass(parent, values, conn);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteParentClass(ResourceClass parent, Connection conn)
        throws SQLException
    {
        AttributeDefinition[] attrs = parent.getAllAttributes();
        deleteAttribute0(resourceClass, attrs, conn);
        ResourceClass[] subclasses = resourceClass.getChildClasses();
        for(int i=0; i<subclasses.length; i++)
        {
            deleteAttribute0(subclasses[i], attrs, conn);
        }
    }

    // private ///////////////////////////////////////////////////////////////

    /**
     * Add attribute to all existing resources of a specific class.
     *
     * @param rc the resource class to modify.
     * @param attr the attribute to add.
     * @param value the initial value of the attribute (may be
     * <code>null</code>).
     * @param conn the JDBC connection to use.
     */
    private void addAttribute0(ResourceClass rc, AttributeDefinition attr, 
                               Object value, Connection conn)
        throws ValueRequiredException, SQLException, ConstraintViolationException
    {
        if((attr.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return;
        }
        Statement stmt1 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery(
            "SELECT resource_id FROM coral_resource WHERE resource_class_id = "+
            rc.getIdString());
        
        // if there are resources to modify, and the attribute is REQUIRED
        // make sure that a value is present.
        if(rs.isBeforeFirst())
        {   
            if(value == null)
            {
                if((attr.getFlags() & AttributeFlags.REQUIRED) != 0)
                {
                    throw new ValueRequiredException("value for a REQUIRED attribute "+
                                                     attr.getName()+" is missing");
                }
            }
            else
            {
                Statement stmt2 = conn.createStatement();
                value = attr.getAttributeClass().getHandler().toAttributeValue(value);
                attr.getAttributeClass().getHandler().checkDomain(attr.getDomain(), value);
                while(rs.next())
                {
                    long resId = rs.getLong(1);
                    long atId = attr.getAttributeClass().getHandler().create(value, conn);
                    stmt2.execute(
                        "INSERT INTO coral_generic_resource "+
                        "(resource_id, attribute_definition_id, data_key) "+
                        "VALUES ("+resId+", "+attr.getIdString()+", "+atId+")"
                    );
                }
            }
            revert(rc, conn);
        }
    }

    /**
     * Add attributes to all existing resources of a specific class.
     *
     * @param rc the resource class to modify.
     * @param attr the attributes to add.
     * @param values the initial values of the attributes, keyed with
     * AttributeDefinition object (<code>null</code> values permitted).
     * @param conn the JDBC connection to use.
     */
    private void addAttribute0(ResourceClass rc, AttributeDefinition[] attr, 
                               Map values, Connection conn)
        throws ValueRequiredException, SQLException, ConstraintViolationException
    {
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery(
            "SELECT resource_id FROM coral_resource WHERE resource_class_id = "+
            rc.getIdString());

        // if there are resources to modify, check if values for all REQUIRED
        // attributes are present 
        if(rs.isBeforeFirst())
        {
            for(int i=0; i<attr.length; i++)
            {
                if((attr[i].getFlags() & AttributeFlags.BUILTIN) != 0)
                {
                    continue;
                }
                Object value = values.get(attr[i]);
                if(value == null)
                {
                    if((attr[i].getFlags() & AttributeFlags.REQUIRED) != 0)
                    {
                        throw new ValueRequiredException("value for a REQUIRED attribute "+
                                                         attr[i].getName()+" is missing");
                    }
                }
                else
                {
                    value = attr[i].getAttributeClass().getHandler().toAttributeValue(value);
                    attr[i].getAttributeClass().getHandler().
                        checkDomain(attr[i].getDomain(), value);
                    values.put(attr[i], value);
                }
            }
        }
        
        while(rs.next())
        {
            long resId = rs.getLong(1);
            for(int i=0; i<attr.length; i++)
            {
                Object value = values.get(attr[i]);
                if(value != null)
                {
                    long atId = attr[i].getAttributeClass().getHandler().create(value, conn);
                    stmt2.execute(
                        "INSERT INTO coral_generic_resource "+
                        "(resource_id, attribute_definition_id, data_key) "+
                        "VALUES ("+resId+", "+attr[i].getIdString()+", "+atId+")"
                    );
                }
            }
        }
        revert(rc, conn);
    }

    /**
     * Remove attribute from all existing resources of a specific class.
     *
     * @param rc the resource class to modify.
     * @param attr the attribute to remove.
     * @param conn the JDBC connection to use.
     */
    private void deleteAttribute0(ResourceClass rc, AttributeDefinition attr, 
                                  Connection conn)
        throws SQLException
    {
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery(
            "SELECT coral_resource.resource_id, data_key FROM "+
            "coral_resource, coral_generic_resource "+
            "WHERE resource_class_id = "+rc.getIdString()+
            " AND attribute_definition_id = "+attr.getIdString()+
            " AND coral_resource.resource_id = coral_generic_resource.resource_id"
        );
        while(rs.next())
        {
            long resId = rs.getLong(1);
            long dataId = rs.getLong(2);
            try
            {
                attr.getAttributeClass().getHandler().delete(dataId, conn);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new BackendException("internal error", e);
            }
            stmt2.execute(
                "DELETE FROM coral_generic_resource "+
                "WHERE resource_id = "+resId+
                "AND attribute_definition_id = "+attr.getIdString()
            );
        }
        revert(rc, conn);
    }    

    /**
     * Remove attributes from all existing resources of a specific class.
     *
     * @param rc the resource class to modify.
     * @param attrs the attributes to remove.
     * @param conn the JDBC connection to use.
     */
    private void deleteAttribute0(ResourceClass rc, AttributeDefinition[] attrs, 
                                  Connection conn)
        throws SQLException
    {
        Map atMap = new HashMap();
        for(int i=0; i<attrs.length; i++)
        {
            atMap.put(attrs[i].getIdObject(), attrs[i]);
        }
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery(
            "SELECT coral_resource.resource_id, attribute_definition_id, data_key FROM "+
            "coral_resource, coral_generic_resource "+
            "WHERE resource_class_id = "+rc.getIdString()+
            " AND coral_resource.resource_id = coral_generic_resource.resource_id"
        );
        while(rs.next())
        {
            long resId = rs.getLong(1);
            long atId = rs.getLong(2);
            long dataId = rs.getLong(3);
            AttributeDefinition attr = (AttributeDefinition)atMap.get(new Long(atId));
            if(attr != null)
            {
                try
                {
                    attr.getAttributeClass().getHandler().delete(dataId, conn);
                }
                catch(EntityDoesNotExistException e)
                {
                    throw new BackendException("internal error", e);
                }
                stmt2.execute(
                    "DELETE FROM coral_generic_resource "+
                    "WHERE resource_id = "+resId+
                    "AND attribute_definition_id = "+atId
                );
            }
        }
        revert(rc, conn);
    }

    /**
     * Retrieve the data keys.
     * 
     * @param delegate the delegate resource.
     * @param conn the connection.
     * @return the map of data keys.
     * @throws SQLException if happens.
     */    
    public Object getData(Resource delegate, Connection conn)
        throws SQLException
    {       
        Map keyMap = new HashMap();
        Map dataKeys = new HashMap();
        keyMap.put(delegate.getIdObject(), dataKeys);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT attribute_definition_id, data_key FROM coral_generic_resource WHERE "+
            "resource_id = "+delegate.getIdString()
        );
        try
        {
            while(rs.next())
            {
                dataKeys.put(coralSchema.getAttribute(rs.getLong(1)), 
                    new Long(rs.getLong(2)));
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("corrupted data", e);
        }
        finally
        {
            rs.close();
            stmt.close();
        }
        return keyMap;
    }

    /**
     * {@inheritDoc}
     */
    public Object getData(ResourceClass rc, Connection conn)
        throws SQLException
    {
        Map keyMap = new HashMap();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT resource_id, attribute_definition_id, data_key FROM coral_generic_resource "+
            "ORDER BY resource_id"
        );
        Map dataKeys = null;
        Long resId = null;
        try
        {
            while(rs.next())
            {
                if(resId == null || resId.longValue() != rs.getLong(1))
                {
                    resId = new Long(rs.getLong(1));
                    dataKeys = new HashMap();
                    keyMap.put(resId, dataKeys);
                }
                dataKeys.put(coralSchema.getAttribute(rs.getLong(2)), 
                    new Long(rs.getLong(3)));
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("corrupted data", e);
        }
        finally
        {
            rs.close();
            stmt.close();
        }
        return keyMap;        
    }

    /**
     * {@inheritDoc}
     */
    public Map getData(Connection conn)
        throws SQLException
    {
        Map keyMap = new HashMap();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT resource_id, attribute_definition_id, data_key FROM arl_generic_resource "+
            "ORDER BY resource_id"
        );
        Map dataKeys = null;
        Long resId = null;
        try
        {
            while(rs.next())
            {
                if(resId == null || resId.longValue() != rs.getLong(1))
                {
                    resId = new Long(rs.getLong(1));
                    dataKeys = new HashMap();
                    keyMap.put(resId, dataKeys);
                }
                dataKeys.put(coralSchema.getAttribute(rs.getLong(2)), 
                    new Long(rs.getLong(3)));
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("corrupted data", e);
        }
        finally
        {
            rs.close();
            stmt.close();
        }
        return keyMap;        
    }
}
