package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;

/**
 * Handles persistency of <code>java.util.List</code> 
 * objects containing weak reference to resources.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: WeakResourceListAttributeHandler.java,v 1.1 2004-03-02 09:51:01 pablo Exp $
 */
public class WeakResourceListAttributeHandler
    extends ResourceListAttributeHandler
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
    public WeakResourceListAttributeHandler(Database database, CoralStore coralStore,
                                CoralSecurity coralSecurity, CoralSchema coralSchema,
                                AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
        
    // AttributeHandler interface ////////////////////////////////////////////

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
        return new WeakResourceList(coralStore, temp);
    }

    // integrity constraints ////////////////////////////////////////////////    

    /**
     * Checks if the attributes of this type can impose integrity constraints 
     * on the data store.
     * 
     * @return <code>true</code> if the attribute can impose constraints on the
     * data store. 
     */
    public boolean containsResourceReferences()
    {
        return false;
    }
    
    /**
     * Returns the resources referenced by this attribute.
     * 
     * @param value the attribute value.
     * @return resources referenced by this attribute.
     * */
    public Resource[] getResourceReferences(Object value)
    {
        return new Resource[0];
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
            return new WeakResourceList(coralStore);
        }
        return null;
    }
}
