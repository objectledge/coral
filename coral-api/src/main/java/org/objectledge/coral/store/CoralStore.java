package org.objectledge.coral.store;

import java.util.Map;

import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.Subject;

/**
 * Manages resource instances.
 *
 * @version $Id: CoralStore.java,v 1.3 2004-03-08 09:17:31 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface CoralStore
{
    // constants /////////////////////////////////////////////////////////////

    /** The identifier of the root resource */
    public static final long ROOT_RESOURCE = 1L;

    // public interface //////////////////////////////////////////////////////

    /**
     * Returns all resources present in the system.
     *
     * <p>Note that this method should never be used during normal operation
     * of the system, because it is potentialy extremely time and memory
     * consuming. It might be even used to launch a DOS attack on the system
     * if it was made availabe to non-privileged users. This method is
     * provided for system assembly and testing purposes only.</p>
     *
     * @return all resources in the system.
     */
    public Resource[] getResource();

    /**
     * Returns all sub-resources of a specific {@link Resource}.
     *
     * @param parent the resource to find children of, or <code>null</code> to
     *        find top level resources.
     * @return the sub-resources of a specific {@link Resource}.
     */
    public Resource[] getResource(Resource parent);

    /**
     * Retrieves the resource  with the given identifier.
     *
     * @param id the identifier.
     * @return the <code>Resource</code>.
     * @throws EntityDoesNotExistException if the <code>Resource</code>
     *         with the specified identifier does not exist.
     */
    public Resource getResource(long id)
        throws EntityDoesNotExistException;
    
    /**
     * Returns all resourcees with the specified name.
     *
     * @param name the name.
     * @return all <code>Resource</code>es with the given name.
     */
    public Resource[] getResource(String name);
    
    /**
     * Returns the resource with the specifed name.
     *
     * @param name the name.
     * @return the resource
     * @throws IllegalStateException if the name denotes multiple resources,
     *         or does not exist.
     */
    public Resource getUniqueResource(String name)
        throws IllegalStateException;

    /**
     * Returns all resources with the specified name among sub-resources
     * of the given resource.
     *
     * @param parent the parent resource.
     * @param name the name.
     * @return all <code>Resource</code>es with the given name.
     */
    public Resource[] getResource(Resource parent, String name);

    /**
     * Returns the resource with the specifed name and parent.
     *
     * @param parent the parent resource.
     * @param name the name.
     * @return the resource.
     * @throws IllegalStateException if the name denotes multiple resources,
     *         or does not exist.
     */
    public Resource getUniqueResource(Resource parent, String name)
        throws IllegalStateException;

    /**
     * Lookup resources denoted by a pathname.
     *
     * <p>The pathname is composed of / separated resource names. If the
     * pathname starts with a / the lookup starts at all resources that have
     * null parents (in the most common case the 'root' resouce #1),
     * otherwise with all resources with names mathich the first pathname
     * component. If any of the pathname components is not unique, the lookup
     * will fork as neccessary, and all leaf resources mathching the pathname
     * will be returned in the results. You can use wildcard character * as
     * a pathname component, that will match any resource name. The wildcard
     * character does not work recursively!</p>
     *
     * @param path resource pathname
     * @return resources dentoted by the pathname
     */
    public Resource[] getResourceByPath(String path);

    /**
     * Returns an unique resource denoted by a pathname.
     * 
     * @param path a pathname
     * @return a resource
     * @throws EntityDoesNotExistException if the resource does not exist.
     * @throws AmbigousEntityNameException if more than one resource is denoted by given pathname.
     */
    public Resource getUniqueResourceByPath(String path)
        throws EntityDoesNotExistException, AmbigousEntityNameException;
        
    /**
     * Lookup resource descendatns denoted by a pathname.
     *
     * <p>The pathname is composed of / separated resource names. The pathname
     * is considered to be relative to the resource <code>start</code>
     * If any of the pathname components is not unique, the lookup
     * will fork as neccessary, and all leaf resources mathching the pathname
     * will be returned in the results. You can use wildcard character * as
     * a pathname component, that will match any resource name. The wildcard
     * character does not work recursively!</p>
     *
     * @param start the resource where the lookup should start.
     * @param path resource pathname.
     * @return resources dentoted by the pathname
     */
    public Resource[] getResourceByPath(Resource start, String path);
        
    /**
     * Returns an unique descendant of a resource denoted by a pathname.
     * 
     * @param start resource where the lookup should start
     * @param path a pathname of a resource.
     * @return a resource.
     * @throws EntityDoesNotExistException if the resource does not exist.
     * @throws AmbigousEntityNameException if the path is ambigous.
     */
    public Resource getUniqueResourceByPath(Resource start, String path)
        throws EntityDoesNotExistException, AmbigousEntityNameException;

    /**
     * Creates a resource image in the persistent storage.
     *
     * @param name the name of the new resource.
     * @param parent the parent of the new resource (may be
     * <coed>null</code>).
     * @param resourceClass the class of the new resource.
     * @param attributes the mapping of {@link AttributeDefinition} objects
     *        into initial values of the attributes.
     * @return the newly created resource.
     * @throws UnknownAttributeException if the <code>attribute</code> map
     *         contains a key that does not belong to
     *         <code>resourceClass</code> attributes.
     * @throws ValueRequiredException if a value of a REQIRED attribute is defined
     *         present in <code>attributes</code>.
     */
    public Resource createResource(String name, Resource parent, ResourceClass resourceClass, 
                                   Map attributes)
        throws UnknownAttributeException,
               ValueRequiredException;

    /**
     * Removes a resource.
     *
     * @param resource the resource to remove.
     * @throws EntityInUseException if the resource has subresources.
     * @throws IllegalArgumentException if the resource has not been saved in
     *         the persistent storage yet.
     */
    public void deleteResource(Resource resource)
        throws EntityInUseException, IllegalArgumentException;

    /**
     * Delete a subtree of resources recursively.
     * 
     * <p>There are two situations where this method may fail. First, a cycle
     * made of non-clearable reference attributes may exists among the 
     * resources in the tree. This will be detected and reported, no partial
     * delete will occur. Second, resources may be references by resources
     * from the outside of the tree. This situation will NOT be detected in
     * the current implementation and will result in partial delete - the
     * resources will disappear from their place though and will end up in
     * /tmp where the administrator may remove them manually. Detecting this
     * requires explicit reference tracking.</p> 
     * 
     * @param res the root of the tree to delete.
     * @return the number of deleted resources.
     * @throws EntityInUseException if one or more of the resources to be deleted is referenced
     *         from other resources.
     */
    public int deleteTree(Resource res)
        throws EntityInUseException;
        
    /**
     * Renames the resource.
     *
     * @param resource the resource to rename.
     * @param name the new name of the resource.
     */
    public void setName(Resource resource, String name);
    
    /**
     * Creates a parent -- child relationship among two resources.
     *
     * @param child the child resource.
     * @param parent the parent resource.
     * @throws CircularDependencyException if the <code>child</code> is
     *         actually a parent of <code>parent</code>.
     */
    public void setParent(Resource child, Resource parent)
        throws CircularDependencyException;
    
    /**
     * Removes a parent -- child relationship among two resources.
     * 
     * @param child the child resource.
     */
    public void unsetParent(Resource child);

    /**
     * Changes the owner of the resource.
     *
     * @param resource the resource.
     * @param owner the new owner.
     */
    public void setOwner(Resource resource, Subject owner);

    /**
     * Creates a copy of a resource.
     *
     * @param source the resource to copy.
     * @param destinationParent the parent resource of the copy.
     * @param destinationName the name of the copy.
     * @return the copy.
     */
    public Resource copyResource(Resource source, Resource destinationParent, 
                                 String destinationName);

    /**
     * Copies a resource tree to another location.
     *
     * <p>Non-Resource attribute values, and Resource attrbute values poiting
     * to Resources outside the source tree are copied by value. Resource
     * attributes pointig to Resources inside the source tree are treated as
     * relative, and will be converted to point to the corresponding resources
     * in the destination tree. Resolution of relative resource references is
     * done in a separate pass after cloning the resources themselves. Any
     * relative reference being a READONLY attribute will be left poining to a
     * resource in the source tree, and a warning will be issued to the log.</p>
     *
     * @param sourceRoot the root of the source tree.
     * @param destinationParent the parent of root node of the destination
     * tree. 
     * @param destinationName the name of root node of the destination tree.
     */
    public void copyTree(Resource sourceRoot, Resource destinationParent, 
                         String destinationName);
}
