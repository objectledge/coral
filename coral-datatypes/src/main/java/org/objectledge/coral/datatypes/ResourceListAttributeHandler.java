package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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
 * @version $Id: ResourceListAttributeHandler.java,v 1.8 2005-01-19 07:34:06 rafal Exp $
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
     * {@inheritDoc}
     */
    public long create(Object value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
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
     * {@inheritDoc}
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
        return instantiate(temp);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public void delete(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        stmt.execute(
            "DELETE FROM "+getTable()+" WHERE data_key = "+id
        );
        releaseId(id);
    }

    // integrity constraints ////////////////////////////////////////////////    

    /**
     * {@inheritDoc}
     */
    public boolean isComposite()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsResourceReferences()
    {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
    public boolean clearResourceReferences(Object value)
    {
        ((ResourceList)value).removeRange(0, ((ResourceList)value).size());
        return false;
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    protected Object fromString(String string)
    {
        if(string == null || string.length() == 0)
        {
            return null;
        }
        if(string.equals("@empty"))
        {
            return instantiate(Collections.EMPTY_LIST);
        }
        List list = new ArrayList();
        StringTokenizer st = new StringTokenizer(string);
        while(st.hasMoreTokens())
        {
            String token = st.nextToken();
            if(Character.isDigit(token.charAt(0)))
            {
                long id = Long.parseLong(token);
                try
                {
                    list.add(coralStore.getResource(id));
                    continue;
                }
                catch(EntityDoesNotExistException e)
                {
                    throw new IllegalArgumentException("resource #"+id+" not found");
                }
            }
            else
            {
                Resource[] res = coralStore.getResourceByPath(token);
                if(res.length == 0)
                {
                    throw new IllegalArgumentException("resource '"+token+"' not found");
                }
                if(res.length > 1)
                {
                    throw new IllegalArgumentException("resource name '"+token+"' is ambigous");
                }
                list.add(res[0]);
                continue;
            }        
        }
        return instantiate(list);
    }

    /**
     * Instantiates a resource list.
     * 
     * @param list list items.
     * @return a ResourceList instance.
     */
    protected ResourceList instantiate(List list)
    {
        return new ResourceList(coralStore, list);
    }
}
