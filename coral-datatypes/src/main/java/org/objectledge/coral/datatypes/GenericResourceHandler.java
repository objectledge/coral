package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.jcontainer.dna.Logger;
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
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;

/**
 * Handles persistence of {@link GenericResource} objects.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResourceHandler.java,v 1.18 2005-06-20 08:20:22 rafal Exp $
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
     * @param resourceClass the resource class.
     * @param database the database.
     * @param instantiator the instantiator.
     * @param logger the logger.
     */
    public GenericResourceHandler(CoralSchema coralSchema, CoralSecurity coralSecurity,
        ResourceClass resourceClass, Database database, Instantiator instantiator, Logger logger)
    {
        super(coralSchema, coralSecurity, instantiator, resourceClass, database, logger);
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
        for(ResourceClass child : resourceClass.getChildClasses())
        {
            child.getHandler().addAttribute(attribute, value, conn);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAttribute(AttributeDefinition attribute, Connection conn)
        throws SQLException
    {
        deleteAttribute0(resourceClass, attribute, conn);
        for(ResourceClass child : resourceClass.getChildClasses())
        {
            deleteAttribute0(child, attribute, conn);
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
        for(ResourceClass child : resourceClass.getChildClasses())
        {
            child.getHandler().addParentClass(parent, values, conn);
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
        for(ResourceClass child : resourceClass.getChildClasses())
        {
            deleteAttribute0(child, attrs, conn);
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
    private void addAttribute0(ResourceClass rc, AttributeDefinition[] attrs, 
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
            for(AttributeDefinition attr: attrs)
            {
                if((attr.getFlags() & AttributeFlags.BUILTIN) != 0)
                {
                    continue;
                }
                Object value = values.get(attr);
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
                    value = attr.getAttributeClass().getHandler().toAttributeValue(value);
                    attr.getAttributeClass().getHandler().
                        checkDomain(attr.getDomain(), value);
                    values.put(attr, value);
                }
            }
        }
        
        while(rs.next())
        {
            long resId = rs.getLong(1);
            for(AttributeDefinition attr : attrs)
            {
                Object value = values.get(attr);
                if(value != null)
                {
                    long atId = attr.getAttributeClass().getHandler().create(value, conn);
                    stmt2.execute(
                        "INSERT INTO coral_generic_resource "+
                        "(resource_id, attribute_definition_id, data_key) "+
                        "VALUES ("+resId+", "+attr.getIdString()+", "+atId+")"
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
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT count(*) from coral_generic_resource WHERE " +
                "attribute_definition_id = " + attr.getIdString());
            rs.next();
            System.out.println("deleting attribute "+attr.getName()+" from "+rc.getName()+": "+
                rs.getInt(1)+" items");
            rs = stmt.executeQuery("SELECT coral_resource.resource_id, data_key FROM "
                + "coral_resource, coral_generic_resource " + "WHERE resource_class_id = "
                + rc.getIdString() + " AND attribute_definition_id = " + attr.getIdString()
                + " AND coral_resource.resource_id = coral_generic_resource.resource_id");
            int cnt = 0;
            while(rs.next())
            {
                long resId = rs.getLong(1);
                long dataId = rs.getLong(2);
                try
                {
                    System.out.print(".");
                    cnt++;
                    if(cnt % 100 == 0)
                    {
                        System.out.println(" "+cnt+" ");
                    }
                    attr.getAttributeClass().getHandler().delete(dataId, conn);
                }
                catch(EntityDoesNotExistException e)
                {
                    throw new BackendException("internal error", e);
                }
            }
            stmt.execute("DELETE FROM coral_generic_resource "
                + "WHERE attribute_definition_id = " + attr.getIdString());
            System.out.println();
        }
        finally
        {
            DatabaseUtils.close(null, stmt, rs);
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
        for(AttributeDefinition attr : attrs)
        {
            atMap.put(attr.getIdObject(), attr);
        }
        Statement stmt = conn.createStatement();
        ResultSet rs;
        rs = stmt.executeQuery(
            "SELECT coral_resource.resource_id, attribute_definition_id, data_key FROM "+
            "coral_resource, coral_generic_resource "+
            "WHERE resource_class_id = "+rc.getIdString()+
            " AND coral_resource.resource_id = coral_generic_resource.resource_id"
        );
        int cnt = 0;
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
            }
        }
        for(int i=0; i<attrs.length; i++)
        {
            stmt.execute(
                "DELETE FROM coral_generic_resource "+
                "WHERE attribute_definition_id = "+attrs[i].getIdString()
            );
        }
        stmt.close();
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
        Map<Long,Map<AttributeDefinition,Long>> keyMap = 
            new HashMap<Long,Map<AttributeDefinition,Long>>();
        Map<AttributeDefinition,Long> dataKeys = new HashMap<AttributeDefinition,Long>();
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
        Map<Long,Map<AttributeDefinition,Long>> keyMap = 
            new HashMap<Long,Map<AttributeDefinition,Long>>();
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
        Map<Long,Map<AttributeDefinition,Long>> keyMap = 
            new HashMap<Long,Map<AttributeDefinition,Long>>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT resource_id, attribute_definition_id, data_key FROM coral_generic_resource "+
            "ORDER BY resource_id"
        );
        Map<AttributeDefinition,Long> dataKeys = null;
        Long resId = null;
        try
        {
            while(rs.next())
            {
                if(resId == null || resId.longValue() != rs.getLong(1))
                {
                    resId = new Long(rs.getLong(1));
                    dataKeys = new HashMap<AttributeDefinition,Long>();
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
