package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;

/**
 * Handles persistency of <code>java.util.List</code> objects containing Resources.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceListAttributeHandler.java,v 1.3 2004-03-15 16:35:42 fil Exp $
 */
public class ResourceListAttributeHandler
    extends AttributeHandlerBase
{
    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     */
    public ResourceListAttributeHandler(Database database, CoralStore coralStore,
                                         CoralSecurity coralSecurity, CoralSchema coralSchema,
                                         AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    // AttributeHandler interface ////////////////////////////////////////////

    /**
     * Creates a new attribute instance.
     *
     * @param value the value of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the identifier of the new attribute.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public long create(Object value, Connection conn)
        throws SQLException
    {
        long id = database.getNextId(getTable());
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO "+getTable()+"(data_key, pos, ref) VALUES ("+
            id+", ?, ?)"
        );
        if(value instanceof ResourceList)
        {
            long[] ids = ((ResourceList)value).getIds();
            int size = ((ResourceList)value).size();
            for(int i=0; i<size; i++)
            {
                pstmt.setInt(1, i);
                pstmt.setLong(2, ids[i]);
                pstmt.addBatch();
            }
        }
        else
        {
            Iterator i = ((List)value).iterator();
            int position = 0;
            while(i.hasNext())
            {
                Object v = i.next();
                if(v instanceof Resource)
                {
                    pstmt.setInt(1, position++);
                    pstmt.setLong(2, ((Resource)v).getId());
                    pstmt.addBatch();
                } 
                else if(v instanceof Long)
                {
                    pstmt.setInt(1, position++);
                    pstmt.setLong(2, ((Long)v).longValue());
                    pstmt.addBatch();
                }
                else
                {
                    throw new ClassCastException(v.getClass().getName());
                }
            }
        }
        pstmt.executeBatch();
        return id;
    }

    /**
     * Retrieves an attribute value.
     *
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public Object retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT ref FROM "+getTable()+" WHERE data_key = "+id+
            " ORDER BY pos"
        );
        ArrayList temp = new ArrayList();
        while(rs.next())
        {
            temp.add(new Long(rs.getLong(1)));
        }
        return new ResourceList(coralStore, temp);
    }

    /**
     * Modifies an existing attribute.
     *
     * @param id the identifier of the attribute.
     * @param value the value of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void update(long id, Object value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO "+getTable()+"(data_key, pos, ref) VALUES ("+
            id+", ?, ?)"
        );
        boolean nonEmpty = false;
        if(value instanceof ResourceList)
        {
            long[] ids = ((ResourceList)value).getIds();
            int size = ((ResourceList)value).size();
            for(int i=0; i<size; i++)
            {
                nonEmpty = true;
                pstmt.setInt(1, i);
                pstmt.setLong(2, ids[i]);
                pstmt.addBatch();
            }
        }
        else
        {
            Iterator i = ((List)value).iterator();
            int position = 0;
            while(i.hasNext())
            {
                nonEmpty = true;
                Object v = i.next();
                if(v instanceof Resource)
                {
                    pstmt.setInt(1, position++);
                    pstmt.setLong(2, ((Resource)v).getId());
                    pstmt.addBatch();
                }
                else if(v instanceof Long)
                {
                    pstmt.setInt(1, position++);
                    pstmt.setLong(2, ((Long)v).longValue());
                    pstmt.addBatch();
                }
                else
                {
                    throw new ClassCastException(v.getClass().getName());
                }
            }
        }
        stmt.execute(
            "DELETE FROM "+getTable()+
            " WHERE data_key = "+id
        );
        if(nonEmpty)
        {
            pstmt.executeBatch();
        }
    }

    /**
     * Removes an existing attribute.
     *
     * <p>This method is implemented here because it is identical for all
     * generic attributes.</p>
     *
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     */
    public void delete(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        stmt.execute(
            "DELETE FROM "+getTable()+" WHERE data_key = "+id
        );
    }

    // integrity constraints ////////////////////////////////////////////////    

    /**
     * Is a composite attribute (wraps the actual values iside an object).
     */
    public boolean isComposite()
    {
        return true;
    }

    /**
     * Checks if the attributes of this type can impose integrity constraints 
     * on the data store.
     * 
     * @return <code>true</code> if the attribute can impose constraints on the
     * data store. 
     */
    public boolean containsResourceReferences()
    {
        return true;
    }
    
    /**
     * Returns the resources referenced by this attribute.
     * 
     * @param value the attribute value.
     * @return resources referenced by this attribute.
     * */
    public Resource[] getResourceReferences(Object value)
    {
        List list = (List)value;
        Resource[] result = new Resource[list.size()];
        for(int i = 0; i< list.size(); i++)
        {
            result[i] = (Resource)list.get(i);        
        }
        return result;
    }
    
    /**
     * Removes all resource attributes from the attribute value.
     * 
     * <p>This method may be called during deletion of a group of 
     * interdependant resources.</p>
     * 
     * @param value attribute value.
     * @return <code>true</code> if the attribute value should be
     *         removed form the resource.
     */
    public boolean clearResourceReferences(Object value)
    {
        ((ResourceList)value).removeRange(0, ((ResourceList)value).size());
        return false;
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * Converts a string into an attribute object.
     *
     * <p>This implementation convers the string <code>@empty</code> into a
     * new, empty ParameterContainer implementation.
     * 
     * @param string the string to convert.
     * @return the attribute object, or <code>null</code> if conversion not
     *         supported. 
     */
    protected Object fromString(String string)
    {
        if(string.equals("@empty"))
        {
            return new ResourceList(coralStore);
        }
        return null;
    }
}
