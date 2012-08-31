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
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;

/**
 * Handles persistency of <code>java.util.List</code> objects containing Resources.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceListAttributeHandler.java,v 1.10 2005-02-21 11:52:20 rafal Exp $
 */
public class ResourceListAttributeHandler<T extends Resource, L extends ResourceList<T>>
    extends AttributeHandlerBase<L>
{
    /** preloading cache. */
    protected ResourceList<T>[] cache; 
    
    /** the CoralSessionFactory. */
    protected CoralSessionFactory coralSessionFactory;

    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the schema.
     * @param coralSessionFactory the session factory.
     * @param attributeClass the attribute class.
     */
    public ResourceListAttributeHandler(Database database, CoralStore coralStore,
        CoralSecurity coralSecurity, CoralSchema coralSchema,
        CoralSessionFactory coralSessionFactory, AttributeClass<L> attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
        this.coralSessionFactory = coralSessionFactory;
    }
    
    // AttributeHandler interface ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void preload(Connection conn)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet result = null;
        try
        {
            result = stmt.executeQuery("SELECT max(data_key) from " + getTable());
            result.next();
            int count = result.getInt(1);
            cache = new ResourceList[count + 1];
            result = stmt.executeQuery(
                "SELECT data_key, ref FROM "+getTable()+" ORDER BY data_key, pos"
            );
            if(result.next())
            {
                List<Long> temp = new ArrayList<Long>();
                long lastId;
                do
                {
                    do
                    {
                        lastId = result.getLong(1);
                        temp.add(new Long(result.getLong(2)));
                    }
                    while(result.next() && result.getLong(1) == lastId);
                    ResourceList<T> value = instantiate(temp);
                    value.clearModified();
                    cache[(int)lastId] = value;
                    temp.clear();
                }
                while(!result.isAfterLast());
            }
        }
        finally
        {
            DatabaseUtils.close(result);
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public long create(L value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO "+getTable()+"(data_key, pos, ref) VALUES (?, ?, ?)"
        );
        try
        {
            if(value instanceof ResourceList)
            {
                long[] ids = value.getIds();
                int size = value.size();
                for(int i=0; i<size; i++)
                {
                    pstmt.setLong(1, id);
                    pstmt.setInt(2, i);
                    pstmt.setLong(3, ids[i]);
                    pstmt.addBatch();
                }
                value.clearModified();
            }
            else
            {
                Iterator<T> i = ((List<T>)value).iterator();
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
        finally
        {
            DatabaseUtils.close(pstmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public L retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length)
        {
            L value = (L)cache[(int)id];
            if(value != null)
            {
                return value;
            }
        }
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try
        {
            rs = stmt.executeQuery(
                "SELECT ref FROM "+getTable()+" WHERE data_key = "+id+
                " ORDER BY pos"
            );
            List<Long> temp = new ArrayList<Long>();
            while(rs.next())
            {
                temp.add(new Long(rs.getLong(1)));
            }
            L value = instantiate(temp);
            if(cache != null && id < cache.length)
            {
                cache[(int)id] = value;
            }
            value.clearModified();
            return value;
        }
        finally
        {
            DatabaseUtils.close(rs);
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void update(long id, L value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length)
        {
            if(value instanceof ResourceList)
            {
                cache[(int)id] = value;
            }
            else
            {
                cache[(int)id] = instantiate(value);
            }
        }
        Statement stmt = conn.createStatement();
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO "+getTable()+"(data_key, pos, ref) VALUES (?, ?, ?)"
        );
        boolean doInsert = false;
        try
        {
            if(value instanceof ResourceList)
            {
                long[] ids = value.getIds();
                int size = value.size();
                doInsert = size > 0;
                for(int i=0; i<size; i++)
                {
                    pstmt.setLong(1, id);
                    pstmt.setInt(2, i);
                    pstmt.setLong(3, ids[i]);
                    pstmt.addBatch();
                }
                value.clearModified();
            }
            else
            {
                Iterator<T> i = ((List<T>)value).iterator();
                doInsert = i.hasNext();
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
            stmt.execute(
                "DELETE FROM "+getTable()+
                " WHERE data_key = "+id
            );
            if(doInsert)
            {
                pstmt.executeBatch();
            }
        }
        finally
        {
            DatabaseUtils.close(pstmt);
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void delete(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = null;
        }
        Statement stmt = conn.createStatement();
        try
        {
            stmt.execute(
                "DELETE FROM "+getTable()+" WHERE data_key = "+id
            );
            releaseId(id);
        }
        finally
        {
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */    
    public boolean isModified(L value)
    {
        return value.isModified();
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
    public Resource[] getResourceReferences(L value)
    {
        List<T> list = value;
        Resource[] result = new Resource[list.size()];
        for(int i = 0; i< list.size(); i++)
        {
            result[i] = list.get(i);        
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean clearResourceReferences(L value)
    {
        value.removeRange(0, value.size());
        return false;
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    protected L fromString(String string)
    {
        if(string == null || string.length() == 0)
        {
            return null;
        }
        if(string.equals("@empty"))
        {
            return instantiate(Collections.EMPTY_LIST);
        }
        List<T> list = new ArrayList<T>();
        StringTokenizer st = new StringTokenizer(string);
        while(st.hasMoreTokens())
        {
            String token = st.nextToken();
            if(Character.isDigit(token.charAt(0)))
            {
                long id = Long.parseLong(token);
                try
                {
                    list.add((T)coralStore.getResource(id));
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
                list.add((T)res[0]);
                continue;
            }        
        }
        return instantiate(list);
    }

    @Override
    protected L fromObject(Object object)
    {
        if(object instanceof List)
        {
            return instantiate((List<?>)object);
        }
        if(object.getClass().isArray()
            && Resource.class.isAssignableFrom(object.getClass().getComponentType()))
        {
            return instantiate(Arrays.asList(object));
        }
        return null;
    }

    /**
     * Instantiates a resource list.
     * 
     * @param list list items.
     * @return a ResourceList instance.
     */
    protected L instantiate(List<?> list)
    {
        return (L)new ResourceList<T>(coralSessionFactory, list);
    }
}
