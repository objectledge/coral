package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.persistence.DefaultInputRecord;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;

/**
 * An implementation of {@ResourceHandler} interface using the
 * <code>PersistenceService</code>.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResourceHandler.java,v 1.19 2008-01-01 22:36:16 rafal Exp $
 */
public class PersistentResourceHandler
    extends AbstractResourceHandler
{
    // instance variables ////////////////////////////////////////////////////

    /** The persistence. */
    private Persistence persistence;
    
    private String[] keyColumns;
    
    /**
     * Constructor.
     * 
     * @param coralSchema the coral schema.
     * @param resourceClass the resource class.
     * @param database the database.
     * @param persistence the persistence.
     * @param instantiator the instantiator.
     * @param cacheFactory the cache factory.
     * @param logger the logger.
     * @throws Exception if there is a problem instantiating an resource object.
     */
    public PersistentResourceHandler(CoralSchema coralSchema, CoralSecurity coralSecurity,
        ResourceClass resourceClass, Database database, Persistence persistence,
        Instantiator instantiator, CacheFactory cacheFactory, Logger logger)
        throws Exception
    {
        super(coralSchema, instantiator, resourceClass, database, cacheFactory, logger);
        this.persistence = persistence;
        keyColumns = new String[1];
        keyColumns[0] = resourceClass.getDbTable()+"_id";      
    }

    // schema operations (not supported) /////////////////////////////////////

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
    public void addAttribute(AttributeDefinition attribute, Object value, 
                             Connection conn)
        throws ValueRequiredException, SQLException
    {
        // throw new UnsupportedOperationException("schema modifications are not supported"+
        //                                         " for this resource class");
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
        throw new UnsupportedOperationException("schema modifications are not supported"+
                                                " for this resource class");
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
    public void addParentClass(ResourceClass parent, Map attributes, Connection conn)
        throws ValueRequiredException, SQLException
    {
        // throw new UnsupportedOperationException("schema modifications are not supported"+
        //                                         " for this resource class");
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
        throw new UnsupportedOperationException("schema modifications are not supported"+
                                                " for this resource class");
    }

    // implementation ////////////////////////////////////////////////////////

    /**
     * Checks if the passed resource is really a {@link Persistent}. implemenation
     *
     * @param resource the resource to check.
     */
    protected void checkResource(Resource resource)
    {
        if(!(resource instanceof PersistentResource))
        {
            throw new ClassCastException("PersistenceResourceHanler won't operate on "+
                                         resource.getClass().getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected Object getData(Resource delegate, Connection conn) 
        throws SQLException
    {
        PreparedStatement statement = null;
        ResultSet rs = null;
    	try
		{
    		Persistent instance = (Persistent)instantiator.
                newInstance(resourceClass.getJavaClass());
    		((AbstractResource)instance).setDelegate(delegate);
            Map<Long, InputRecord> data = new HashMap<Long, InputRecord>();
            if(delegate.getResourceClass().getDbTable() != null)
            {
                statement = DefaultInputRecord.getSelectStatement(
                    "resource_id = " + delegate.getIdString(), instance, conn);
                rs = statement.executeQuery();
                InputRecord record = new DefaultInputRecord(rs);
                if(!rs.next())
                {
                    throw new SQLException("missing data for "
                        + delegate.getResourceClass().getName() + " WHERE resource_id = "
                        + delegate.getIdString());
                }
                data.put(delegate.getIdObject(), record);
            }
            return data;
		}
    	catch(org.objectledge.coral.InstantiationException e)
		{
    		throw (SQLException)new SQLException("failed to instantiate helper instance").
                initCause(e);
		}
    	finally
    	{
    	    DatabaseUtils.close(rs);
    	    DatabaseUtils.close(statement);
    	}
    }

    /**
     * {@inheritDoc}
     */
    protected Object getData(ResourceClass rc, Connection conn) throws SQLException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getData(Connection conn) throws SQLException
    {
        return null;
    }
    
    /**
     * @return Returns the persistence.
     */
    Persistence getPersistence()
    {
        return persistence;
    }    
    
    String[] getKeyColumns()
    {
        return keyColumns;
    }
    
    /**
     * {@inheritDoc}
     */
    public Class<?> getFallbackResourceImplClass()
    {
        return PersistentResource.class;
    }
}