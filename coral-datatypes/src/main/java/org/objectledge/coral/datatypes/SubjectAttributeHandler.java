package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link Subject} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SubjectAttributeHandler.java,v 1.2 2004-04-01 08:54:27 fil Exp $
 */
public class SubjectAttributeHandler
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
    public SubjectAttributeHandler(Database database, CoralStore coralStore,
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
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, ref) VALUES ("+
            id+", "+((Subject)value).getId()+")"
        );
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
            "SELECT ref FROM "+getTable()+" WHERE data_key = "+id
        );
        if(!rs.next())
        {
            throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "+
                getTable());
        }
        return coralSecurity.getSubject(rs.getLong(1));
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
        checkExists(id, stmt);
        stmt.execute(
            "UPDATE "+getTable()+" SET ref = "+
            ((Subject)value).getId()+
            " WHERE data_key = "+id
        );
    }

    // meta information //////////////////////////////////////////////////////
    
    /**
     * Provides information about comparison operations supported by the
     * attribute type.
     *
     * <p>The returned value is a bitwise sum of the CONDITION_*
     * constants.</p>
     * @return information about comparison operations supported by the
     * attribute type.
     */
    public int getSupportedConditions()
    {
        return CONDITION_EQUALITY;
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * Converts a string into an attribute object.
     *
     * <p>If the string starts with a number, it is considered to be subject
     * id. Otherwise it is considered subject name. If the name is ambigous,
     * an exception will be thrown.</p>
     * 
     * @param string the string to convert.
     * @return the attribute object, or <code>null</code> if conversion not
     *         supported. 
     */
    protected Object fromString(String string)
    {
        if(Character.isDigit(string.charAt(0)))
        {
            long id = Long.parseLong(string);
            try
            {
                return coralSecurity.getSubject(id);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new IllegalArgumentException("subject #"+id+" not found");
            }
        }
        else
        {
            try
            {
                return coralSecurity.getSubject(string);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new IllegalArgumentException("subject '"+string+"' not found");
            }
        }
    }
}
