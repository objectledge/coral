package org.objectledge.coral.datatypes;

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
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Handles persistence of {@link GenericResource} objects.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResourceHandler.java,v 1.2 2004-03-08 09:17:28 fil Exp $
 */
public class GenericResourceHandler
    extends ResourceHandlerBase
{
    // Member objects ////////////////////////////////////////////////////////

    /** resource sets, keyed by resource class. Resources are kept through
    weak  references. */
    private Map cache = new HashMap();

    /**
     * Constructor.
     * 
     * @param coralSecurity the coralSecurity.
     * @param resourceClass the resource class.
     */
    public GenericResourceHandler(CoralSchema coralSchema, CoralSecurity coralSecurity,
                                   ResourceClass resourceClass)
    {
        super(coralSchema, coralSecurity, resourceClass);
    }

    // Resource handler interface ////////////////////////////////////////////

    /**
     * Saves a new resource in the persistent storage.
     *
     * @param delegate the security delegate {@link Resource} object.
     * @param attributes the initial values of the attributes, keyed with
     *        {@link AttributeDefinition} objects.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the new resource.
     * @throws ValueRequiredException if not all obligatory attributes are
     *         provided.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
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
     * Removes the resource from the persistent storage.
     *
     * @param resource the resource
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void delete(Resource resource, Connection conn)
        throws SQLException
    {
        checkResource(resource);
        ((GenericResource)resource).delete(conn);
    }

    /**
     * Retrives a resource from the persistent storage.
     *
     * @param delegate the security delegate {@link Resource} object.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the resource.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
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
     * Retrives a resource from the persistent storage.
     *
     * @param delegate the security delegate {@link Resource} object.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the resource.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
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
     * Reverts the state of a resource from the persistent storage.
     *
     * @param resource the resource.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
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
     * Reverts the state of a resource from the persistent storage.
     *
     * @param resource the resource.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void revert(Resource resource, Connection conn, Map dataKeyMap)
        throws SQLException
    {
        Resource delegate = resource.getDelegate();
        checkDelegate(delegate);
        ((GenericResource)resource).revert(resourceClass, conn, dataKeyMap);        
    }

    /**
     * Updates the contents of the resource in the persistent storage.
     *
     * @param resource the resource.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void update(Resource resource, Connection conn)
        throws SQLException
    {
        checkResource(resource);
        ((GenericResource)resource).update(conn);
    }

    /**
     * Called when an attribute is added to a structured resource class.
     *
     * <p>Concrete resource classes will probably deny this operation by
     * throwing <code>UnsupportedOperationException</code>.</p>
     *
     * @param attribute the new attribute.
     * @param value the initial value to be set for existing instances of this
     *        class.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     * @throws ValueRequiredException if <code>null</code> value was provided
     *         for a REQUIRED attribute.
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
     * Called when an attribute is removed from a structured resource class.
     *
     * <p>Concrete resource classes will probably deny this operation by
     * throwing <code>UnsupportedOperationException</code>.</p>
     *
     * @param attribute the removed attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
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
     * Called when a parent class is added to a sturctured resource class.
     *
     * <p>Concrete resource classes will probably deny this operation by
     * throwing <code>UnsupportedOperationException</code>.</p>
     *
     * @param parent the new parent class.
     * @param attributes the initial values of the attributes. Values are
     *        keyed with {@link AttributeDefinition} objects.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     * @throws ValueRequiredException if values for any of parent class
     *         REQUIRED attributes are missing from <code>attributes</code>
     *         map. 
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
     * Called when a parent class is removed from a structured resource class.
     *
     * <p>Concrete resource classes will probably deny this operation by
     * throwing <code>UnsupportedOperationException</code>.</p>
     *
     * @param parent the new parent class.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
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
     * Instantiate an implementation object.
     *
     * @param rClass the resource class to be instantiated
     * @return implementation object.
     */
    private GenericResource instantiate(ResourceClass rClass)
        throws BackendException
    {
        GenericResource res;
        try
        {
            res = (GenericResource)rClass.getJavaClass().newInstance();
        }
        catch(VirtualMachineError e)
        {
            throw e;
        }
        catch(ThreadDeath e)
        {
            throw e;
        }
        catch(Throwable e)
        {
            throw new BackendException("failed to instantiate "+
                                        rClass.getName(), e);
        }
        return res;
    }

    /**
     * Chekcks if the passed delegate object specifies {@link GenericResource}
     * as the javaClass.
     *
     * @param delegate the delegate to check.
     */
    private void checkDelegate(Resource delegate)
    {
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
            "SELECT resource_id FROM arl_resource WHERE resource_class_id = "+
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
                        "INSERT INTO arl_generic_resource "+
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
            "SELECT resource_id FROM arl_resource WHERE resource_class_id = "+
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
                        "INSERT INTO arl_generic_resource "+
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
            "SELECT arl_resource.resource_id, data_key FROM "+
            "arl_resource, arl_generic_resource "+
            "WHERE resource_class_id = "+rc.getId()+
            "AND attribute_definition_id = "+attr.getId()+
            "AND arl_resource.resource_id = arl_generic_resource.resource_id"
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
                "DELETE FROM arl_generic_resource "+
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
            "SELECT arl_resource.resource_id, attribute_definition_id, data_key FROM "+
            "arl_resource, arl_generic_resource "+
            "WHERE resource_class_id = "+rc.getId()+
            "AND arl_resource.resource_id = arl_generic_resource.resource_id"
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
                    "DELETE FROM arl_generic_resource "+
                    "WHERE resource_id = "+resId+
                    "AND attribute_definition_id = "+atId
                );
            }
        }
        revert(rc, conn);
    }
    
    public Map getDataKeys(Resource delegate, Connection conn)
        throws SQLException
    {       
        Map keyMap = new HashMap();
        Map dataKeys = new HashMap();
        keyMap.put(new Long(delegate.getId()), dataKeys);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT attribute_definition_id, data_key FROM arl_generic_resource WHERE "+
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
    
    public Map getDataKeys(Connection conn)
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
