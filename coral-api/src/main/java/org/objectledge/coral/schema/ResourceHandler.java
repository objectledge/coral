package org.objectledge.coral.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Manages persistency of resources belonging to a specific class .
 *
 * @version $Id: ResourceHandler.java,v 1.5 2004-06-29 09:26:23 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceHandler
{
    // NOTE ResoureClass needs to be passed to the constructor.

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
        throws ValueRequiredException, SQLException;

    /**
     * Removes the resource from the persistent storage.
     *
     * @param resource the resource.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void delete(Resource resource, Connection conn)
        throws SQLException;

    /**
     * Retrives a resource from the persistent storage.
     *
     * @param delegate the security delegate {@link Resource} object.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @param data implemntation dependent data passed across recursive calls to this method. 
     *        An implementation must accept <code>null</code> value of this parameter.  
     * @return the resource object.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public Resource retrieve(Resource delegate, Connection conn, Object data)
        throws SQLException;


    /**
     * Reverts the state of a resource from the persistent storage.
     *
     * @param resource the resource.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @param data implemntation dependent data passed across recursive calls to this method. 
     *        An implementation must accept <code>null</code> value of this parameter.  
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void revert(Resource resource, Connection conn, Object data)
        throws SQLException;

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
        throws SQLException;

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
        throws ValueRequiredException, SQLException;
    
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
        throws SQLException;

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
        throws ValueRequiredException, SQLException;
    
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
        throws SQLException;
        
     // integrity constraints ///////////////////////////////////////////////
     
    /**
     * Returns all resources referenced by this resource through it's 
     * attributes.
     * 
     * <p>The references mad throgh non-REQUIRED nor READONLY refernces
     * may be auto-cleared by the handler with 
     * {@link #clearResourceReferernces()} method. These references
     * may or may not be included in the result, depending on the clearable 
     * parameter.</p> 
     * 
     * @param resource the resource.
     * @param clearable references that may be auto-cleared are included in 
     *        the results if <code>true</code>
     * @return resources referenced by this resource.
     */
    public Resource[] getResourceReferences(Resource resource, boolean clearable);
    
    /**
     * Removes all resource references that can be auto-cleared 
     * from this resource.
     * 
     * <p>This method may be called during deletion of a group of 
     * interdependant resources.</p>
     * 
     * @param resource the resource to clear.
     */
    public void clearResourceReferences(Resource resource);
}
