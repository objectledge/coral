package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;
import org.objectledge.parameters.DefaultParameters;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.db.DBParameters;
import org.objectledge.parameters.db.DBParametersException;
import org.objectledge.parameters.db.DBParametersManager;

/**
 * Handles persistency of objects supporting
 * <code>labeo.util.configuration.ParameterContainer</code>.
 * 
 * <p>This implementation depends on
 * <coded>PersistentParameterContainerService</code>, currenty in
 * labeo-experimental.</p>
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ParametersAttributeHandler.java,v 1.3 2004-05-06 13:01:24 pablo Exp $
 */
public class ParametersAttributeHandler
    extends AttributeHandlerBase
{
    // instance variables ////////////////////////////////////////////////////

    private DBParametersManager dbParametersManager;

    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     * @param dbParametersManager the parameters manager.
     */
    public ParametersAttributeHandler(Database database, CoralStore coralStore,
                                   CoralSecurity coralSecurity, CoralSchema coralSchema,
                                   AttributeClass attributeClass, 
                                   DBParametersManager dbParametersManager)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
        this.dbParametersManager = dbParametersManager;
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
        try
        {
            DBParameters parameters = (DBParameters)dbParametersManager.createContainer();
            parameters.add((Parameters)value, true);
            return parameters.getId();  
        }
        catch(DBParametersException e)
        {
            ///CLOVER:OFF
            throw new BackendException("failed to create container", e);
            ///CLOVER:ON
        }
    }

    /**
     * Retrieves an attribute value.
     *
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the retrieved attribute object.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public Object retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        try
        {
            return dbParametersManager.getParameters(id);
        }
        catch(DBParametersException e)
        {
            ///CLOVER:OFF
            throw new BackendException("failed to retrieve container", e);
            ///CLOVER:ON
        }
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
        try
        {
            Parameters parameters = dbParametersManager.getParameters(id);
            synchronized(parameters)
            {
                parameters.remove();
                parameters.add((Parameters)value, true);
            }   
        }
        catch(DBParametersException e)
        {
            ///CLOVER:OFF
            throw new BackendException("failed to update container", e);
            ///CLOVER:ON
        }
    }

    /**
     * Removes an existing attribute.
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
        try
        {
            dbParametersManager.deleteParameters(id);
        }
        catch(DBParametersException e)
        {
            ///CLOVER:OFF
            throw new BackendException("failed to delete container");
            ///CLOVER:ON
        }
    }

    // meta information //////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public boolean shouldRetrieveAfterCreate()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isComposite()
    {
        return true;
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
            return new DefaultParameters();
        }
        else
        {
            return new DefaultParameters(string);
        }
    }

    /**
     * Converts an attribute value into a human readable string.
     *
     * @param value the value to convert.
     * @return a human readable string.
     */
    public String toPrintableString(Object value)
    {
        checkValue(value);
        return ((Parameters)value).toString();
    }
}

