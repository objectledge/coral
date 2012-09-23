package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.DatabaseUtils;


public class GenericSchemaHandler<T extends Resource>
{
    private final ResourceClass<T> resourceClass;

    public GenericSchemaHandler(ResourceClass<T> resourceClass)
    {
        this.resourceClass = resourceClass;
    }

    /**
     * {@inheritDoc}
     */
    public <A> void addAttribute(AttributeDefinition<A> attribute, A value, Connection conn)
        throws ValueRequiredException, SQLException
    {
        addAttribute0(resourceClass, attribute, value, conn);
        for(ResourceClass<?> child : resourceClass.getDirectChildClasses())
        {
            child.getHandler().addAttribute(attribute, value, conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAttribute(AttributeDefinition<?> attribute, Connection conn)
        throws SQLException
    {
        deleteAttribute0(resourceClass, attribute, conn);
        for(ResourceClass<?> child : resourceClass.getDirectChildClasses())
        {
            child.getHandler().deleteAttribute(attribute, conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addParentClass(ResourceClass<?> parent, Map<AttributeDefinition<?>, ?> values,
        Connection conn)
        throws ValueRequiredException, SQLException
    {
        AttributeDefinition<?>[] attrs = parent.getAllAttributes();
        addAttribute0(resourceClass, attrs, values, conn);
        for(ResourceClass<?> child : resourceClass.getDirectChildClasses())
        {
            child.getHandler().addParentClass(parent, values, conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteParentClass(ResourceClass<?> parent, Connection conn)
        throws SQLException
    {
        AttributeDefinition<?>[] attrs = parent.getAllAttributes();
        deleteAttribute0(resourceClass, attrs, conn);
        for(ResourceClass<?> child : resourceClass.getDirectChildClasses())
        {
            for(AttributeDefinition<?> attr : attrs)
            {
                child.getHandler().deleteAttribute(attr, conn);
            }
        }
    }

    // private ///////////////////////////////////////////////////////////////

    /**
     * Add attribute to all existing resources of a specific class.
     * 
     * @param rc the resource class to modify.
     * @param attr the attribute to add.
     * @param value the initial value of the attribute (may be <code>null</code>).
     * @param conn the JDBC connection to use.
     */
    private <A> void addAttribute0(ResourceClass<T> rc, AttributeDefinition<A> attr, A value,
        Connection conn)
        throws ValueRequiredException, SQLException, ConstraintViolationException
    {
        if((attr.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return;
        }
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs = null;
        try
        {

            rs = stmt1
                .executeQuery("SELECT resource_id FROM coral_resource WHERE resource_class_id = "
                    + rc.getIdString());

            // if there are resources to modify, and the attribute is REQUIRED
            // make sure that a value is present.
            if(rs.isBeforeFirst())
            {
                if(value == null)
                {
                    if((attr.getFlags() & AttributeFlags.REQUIRED) != 0)
                    {
                        throw new ValueRequiredException("value for a REQUIRED attribute "
                            + attr.getName() + " is missing");
                    }
                }
                else
                {
                    value = attr.getAttributeClass().getHandler().toAttributeValue(value);
                    attr.getAttributeClass().getHandler().checkDomain(attr.getDomain(), value);
                    while(rs.next())
                    {
                        long resId = rs.getLong(1);
                        long atId = attr.getAttributeClass().getHandler().create(value, conn);
                        stmt2.execute("INSERT INTO coral_generic_resource "
                            + "(resource_id, attribute_definition_id, data_key) " + "VALUES ("
                            + resId + ", " + attr.getIdString() + ", " + atId + ")");
                    }
                }
            }
        }
        finally
        {
            DatabaseUtils.close(stmt2);
            DatabaseUtils.close(rs);
            DatabaseUtils.close(stmt1);
        }
    }

    /**
     * Add attributes to all existing resources of a specific class.
     * 
     * @param rc the resource class to modify.
     * @param attr the attributes to add.
     * @param values the initial values of the attributes, keyed with AttributeDefinition object (
     *        <code>null</code> values permitted).
     * @param conn the JDBC connection to use.
     */
    private void addAttribute0(ResourceClass<T> rc, AttributeDefinition<?>[] attrs,
        Map<AttributeDefinition<?>, ?> values, Connection conn)
        throws ValueRequiredException, SQLException, ConstraintViolationException
    {
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs = null;
        try
        {
            rs = stmt1
                .executeQuery("SELECT resource_id FROM coral_resource WHERE resource_class_id = "
                    + rc.getIdString());

            // if there are resources to modify, check if values for all REQUIRED
            // attributes are present
            if(rs.isBeforeFirst())
            {
                for(AttributeDefinition<?> attr : attrs)
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
                            throw new ValueRequiredException("value for a REQUIRED attribute "
                                + attr.getName() + " is missing");
                        }
                    }
                    else
                    {
                        checkDomain(attr, value);
                    }
                }
            }

            while(rs.next())
            {
                long resId = rs.getLong(1);
                for(AttributeDefinition<?> attr : attrs)
                {
                    Object value = values.get(attr);
                    if(value != null)
                    {
                        long atId = storeValue(attr, value, conn);
                        stmt2.execute("INSERT INTO coral_generic_resource "
                            + "(resource_id, attribute_definition_id, data_key) " + "VALUES ("
                            + resId + ", " + attr.getIdString() + ", " + atId + ")");
                    }
                }
            }
        }
        finally
        {
            DatabaseUtils.close(stmt2);
            DatabaseUtils.close(rs);
            DatabaseUtils.close(stmt1);
        }
    }

    protected <A> A checkDomain(AttributeDefinition<A> attr, Object value)
    {
        final AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
        A attrValue = handler.toAttributeValue(value);
        handler.checkDomain(attr.getDomain(), attrValue);
        return attrValue;
    }

    protected <A> long storeValue(AttributeDefinition<A> attr, Object value, Connection conn)
        throws SQLException
    {
        final AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
        A attrValue = handler.toAttributeValue(value);
        return handler.create(attrValue, conn);
    }

    /**
     * Remove attribute from all existing resources of a specific class.
     * 
     * @param rc the resource class to modify.
     * @param attr the attribute to remove.
     * @param conn the JDBC connection to use.
     */
    private <A> void deleteAttribute0(ResourceClass<T> rc, AttributeDefinition<A> attr,
        Connection conn)
        throws SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = conn.createStatement();
            String sql = "SELECT count(*) from coral_generic_resource g, coral_resource r WHERE "
                + " g.attribute_definition_id = " + attr.getIdString()
                + " AND r.resource_class_id = " + rc.getIdString();
            rs = stmt.executeQuery(sql);
            rs.next();
            rs = stmt.executeQuery("SELECT coral_resource.resource_id, data_key FROM "
                + "coral_resource, coral_generic_resource " + "WHERE resource_class_id = "
                + rc.getIdString() + " AND attribute_definition_id = " + attr.getIdString()
                + " AND coral_resource.resource_id = coral_generic_resource.resource_id");
            while(rs.next())
            {
                long dataId = rs.getLong(2);
                attr.getAttributeClass().getHandler().delete(dataId, conn);
            }
            sql = "DELETE FROM coral_generic_resource "
                + "WHERE (resource_id, attribute_definition_id) "
                + "IN (SELECT coral_resource.resource_id, attribute_definition_id" + " FROM "
                + "coral_resource, coral_generic_resource" + " WHERE resource_class_id = "
                + rc.getIdString() + " AND attribute_definition_id = " + attr.getIdString()
                + " AND coral_resource.resource_id = coral_generic_resource.resource_id)";
            stmt.execute(sql);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("internal error", e);
        }
        finally
        {
            DatabaseUtils.close(rs);
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * Remove attributes from all existing resources of a specific class.
     * 
     * @param rc the resource class to modify.
     * @param attrs the attributes to remove.
     * @param conn the JDBC connection to use.
     */
    private void deleteAttribute0(ResourceClass<T> rc, AttributeDefinition<?>[] attrs,
        Connection conn)
        throws SQLException
    {
        Map<Long, AttributeDefinition<?>> atMap = new HashMap<Long, AttributeDefinition<?>>();
        for(AttributeDefinition<?> attr : attrs)
        {
            atMap.put(attr.getIdObject(), attr);
        }
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try
        {
            rs = stmt
                .executeQuery("SELECT coral_resource.resource_id, attribute_definition_id, data_key FROM "
                    + "coral_resource, coral_generic_resource "
                    + "WHERE resource_class_id = "
                    + rc.getIdString()
                    + " AND coral_resource.resource_id = coral_generic_resource.resource_id");
            while(rs.next())
            {
                long atId = rs.getLong(2);
                long dataId = rs.getLong(3);
                AttributeDefinition<?> attr = atMap.get(new Long(atId));
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
            for(int i = 0; i < attrs.length; i++)
            {
                stmt.execute("DELETE FROM coral_generic_resource "
                    + "WHERE attribute_definition_id = " + attrs[i].getIdString()
                    + " AND resource_id IN (SELECT resource_id "
                    + "FROM coral_resource WHERE resource_class_id = " + rc.getIdString() + ")");
            }
        }
        finally
        {
            DatabaseUtils.close(rs);
            DatabaseUtils.close(stmt);
        }
    }
}
