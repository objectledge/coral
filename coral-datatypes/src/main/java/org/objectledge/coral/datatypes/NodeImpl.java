package org.objectledge.coral.datatypes;

import java.util.HashMap;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;

/**
 * An implementation of <code>coral.Node</code> Coral resource class.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: NodeImpl.java,v 1.4 2004-07-22 16:35:46 zwierzem Exp $
 */
public class NodeImpl
    extends GenericResource
    implements Node
{
    // initialization /////////////////////////////////////////////////////////

    /**
     * Creates a blank <code>coral.Node</code> resource wrapper.
     *
     * <p>This constructor should be used by the GenericResourceHandler class
     * only. Use <code>load()</code> and <code>create()</code> methods to create
     * instances of the wrapper in your application code.</p>
     *
     * @param coralSchema the coral schema.
     * @param database the database.
     * @param logger the logger.
     */
    public NodeImpl(CoralSchema coralSchema, Database database, Logger logger)
    {
        super(database, logger);
    }

    // static methods ////////////////////////////////////////////////////////

    /**
     * Retrieves a <code>coral.Node</code> resource instance from the store.
     *
     * <p>This is a simple wrapper of StoreService.getResource() method plus
     * the typecast.</p>
     *
     * @param coralStore the coral store.
     * @param id the id of the object to be retrieved
     * @return the node.
     * @throws EntityDoesNotExistException if cannot retrieve resource. 
     */
    public static Node retrieveNodeResource(CoralStore coralStore, long id)
        throws EntityDoesNotExistException
    {
        Resource res = coralStore.getResource(id);
        if(!(res instanceof Node))
        {
            throw new IllegalArgumentException("resource #"+id+" is "+
                                               res.getResourceClass().getName()+
                                               " not coral.Node");
        }
        return (Node)res;
    }

    /**
     * Creates a new <code>coral.Node</code> resource instance.
     *
     * @param coralStore the coral store.
     * @param coralSchema the coral schema.
     * @param name the name of the new resource
     * @param parent the parent resource.
     * @return a new Node instance.
     * @throws ValueRequiredException if happens.
     */
    public static Node createNodeResource(CoralStore coralStore, 
                                                   CoralSchema coralSchema,
                                                   String name, Resource parent)
        throws ValueRequiredException
    {
        try
        {
            ResourceClass rc = coralSchema.getResourceClass("coral.Node");
            Map attrs = new HashMap();
            Resource res = coralStore.createResource(name, parent, rc, attrs);
            if(!(res instanceof Node))
            {
                throw new BackendException("incosistent schema: created object is "+
                                           res.getClass().getName());
            }
            return (Node)res;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("incompatible schema change", e);
        }
    }

    // @custom methods ///////////////////////////////////////////////////////
}
