package org.objectledge.coral.datatypes;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
 * @version $Id: GenericResourceHandler.java,v 1.7 2004-05-06 10:55:25 pablo Exp $
 */
public class GenericResourceHandler
    extends ResourceHandlerBase
{
    // Member objects ////////////////////////////////////////////////////////

    /** resource sets, keyed by resource class. Resources are kept through
    weak  references. */
    private Map cache = new HashMap();

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
    public Resource create(Resource delegate, Map attributes, Connection conn)
        throws ValueRequiredException, SQLException
    {
        checkDelegate(delegate);
        GenericResource res = instantiate(resourceClass);
        res.create(delegate, resourceClass, attributes, conn);
        addToCache(res);
        return res;
    }

    /**
     * {@inheritDoc}
     */
    public void delete(Resource resource, Connection conn)
        throws SQLException
    {
        checkResource(resource);
        ((GenericResource)resource).delete(conn);
    }

    /**
     * {@inheritDoc}
     */
    public Resource retrieve(Resource delegate, Connection conn)
        throws SQLException
    {
        checkDelegate(delegate);
        GenericResource res = instantiate(resourceClass);
        Map dataKeyMap = getDataKeys(delegate, conn);
        res.retrieve(delegate, resourceClass, conn, dataKeyMap);
        addToCache(res);
        return res;
    }

    /**
     * {@inheritDoc}
     */
    public Resource retrieve(Resource delegate, Connection conn, Map dataKeyMap)
        throws SQLException
    {
        checkDelegate(delegate);
        GenericResource res = instantiate(resourceClass);
        res.retrieve(delegate, resourceClass, conn, dataKeyMap);
        addToCache(res);
        return res;
    }

    /**
     * {@inheritDoc}
     */
    public void revert(Resource resource, Connection conn)
        throws SQLException
    {
        Resource delegate = resource.getDelegate();
        checkDelegate(delegate);
        Map dataKeyMap = getDataKeys(delegate, conn);
        ((GenericResource)resource).revert(resourceClass, conn, dataKeyMap);        
    }

    /**
     * {@inheritDoc}
     */
    public void revert(Resource resource, Connection conn, Map dataKeyMap)
        throws SQLException
    {
        Resource delegate = resource.getDelegate();
        checkDelegate(delegate);
        ((GenericResource)resource).revert(resourceClass, conn, dataKeyMap);        
    }

    /**
     * {@inheritDoc}
     */
    public void update(Resource resource, Connection conn)
        throws SQLException
    {
        checkResource(resource);
        ((GenericResource)resource).update(conn);
    }

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
     * Checks if the passed resource is really a {@link GenericResource}.
     *
     * @param resource the resource to check.
     */
    private void checkResource(Resource resource)
    {
        if(!(resource instanceof GenericResource))
        {
            throw new ClassCastException("GenericResourceHanler won't operate on "+
                                         resource.getClass().getName());
        }
    }

    /**
     * Chekcks if the passed delegate object specifies {@link GenericResource}
     * as the javaClass.
     *
     * @param delegate the delegate to check.
     */
    private void checkDelegate(Resource delegate)
    {
        if((delegate.getResourceClass().getJavaClass().getModifiers() & Modifier.INTERFACE) != 0)
        {
            throw new ClassCastException(delegate.getResourceClass().getName()+" specifies "+
                delegate.getResourceClass().getJavaClass()+" as implementation class");
        }
        if(!GenericResource.class.isAssignableFrom(delegate.getResourceClass().getJavaClass()))
        {
            throw new ClassCastException("GenericResourceHandler won't operate on "+
                                         delegate.getResourceClass().getName());
        }
    }

    /**
     * Adds the loaded/created resource to the internal cache.
     *
     * @param res the resource to add to the cache.
     */
    private void addToCache(GenericResource res)
    {
        // we use WeakHashMap to emulate WeakSet
        Map rset = (Map)cache.get(res.getResourceClass0());
        if(rset == null)
        {
            rset = new WeakHashMap();
            cache.put(res.getResourceClass(), rset);
        }
        rset.put(res, null);
    }

    /**
     * Reverts all cached resources of the specific class.
     *
     * @param rc the resource class to revert.
     * @param conn the JDBC connection to use.
     */
    private synchronized void revert(ResourceClass rc, Connection conn)
        throws SQLException
    {
        Map rset = (Map)cache.get(rc);
        if(rset != null)
        {
            // we'll get too much but this shouldn't be a problem.
            Map dataKeyMap = getDataKeys(conn);
            Set orig = new HashSet(rset.keySet());
            Iterator i = orig.iterator();
            while(i.hasNext())
            {
                GenericResource r = (GenericResource)i.next();
                r.revert(r.getResourceClass0(), conn, dataKeyMap);
            }
        }
    }

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
            rc.getId());
        
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
                        "VALUES ("+resId+", "+attr.getId()+", "+atId+")"
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
            rc.getId());

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
                        "VALUES ("+resId+", "+attr[i].getId()+", "+atId+")"
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
            "WHERE resource_class_id = "+rc.getId()+
            " AND attribute_definition_id = "+attr.getId()+
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
                "AND attribute_definition_id = "+attr.getId()
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
            atMap.put(new Long(attrs[i].getId()), attrs[i]);
        }
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs = stmt1.executeQuery(
            "SELECT coral_resource.resource_id, attribute_definition_id, data_key FROM "+
            "coral_resource, coral_generic_resource "+
            "WHERE resource_class_id = "+rc.getId()+
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
    public Map getDataKeys(Resource delegate, Connection conn)
        throws SQLException
    {       
        Map keyMap = new HashMap();
        Map dataKeys = new HashMap();
        keyMap.put(new Long(delegate.getId()), dataKeys);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT attribute_definition_id, data_key FROM coral_generic_resource WHERE "+
            "resource_id = "+delegate.getId()
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
     * Retrieve the data keys.
     * 
     * @param conn the connection.
     * @return the map of data keys.
     * @throws SQLException if happens.
     */    
    public Map getDataKeys(Connection conn)
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
}
