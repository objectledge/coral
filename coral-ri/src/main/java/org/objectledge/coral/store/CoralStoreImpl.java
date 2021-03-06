package org.objectledge.coral.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Feature;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.PreloadingParticipant;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityFactory;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Subject;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.database.persistence.PersistentFactory;

/**
 * Manages resource instances.
 * 
 * @version $Id: CoralStoreImpl.java,v 1.33 2009-01-30 13:44:14 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralStoreImpl
    implements CoralStore, PreloadingParticipant
{
    // Constants /////////////////////////////////////////////////////////////////////////////////

    private static final String ILLEGAL_CHARACTERS = "/";

    private static final Pattern ILLEGAL_NAME_PATTERN = Pattern.compile("[" + ILLEGAL_CHARACTERS
        + "]");

    // Instance variables ////////////////////////////////////////////////////////////////////////

    private Persistence persistence;

    private CoralEventHub coralEventHub;

    private CoralCore coral;

    private Logger log;

    /**
     * The <code>PersistentFactory</code> for <code>Resource</code> objects.
     */
    private PersistentFactory<ResourceImpl> resourceFactory;

    /** <code>Long</code> id -&gt; <code>Resource</code>. */
    private Map<Long, Resource> resourceById;

    /** <code>String</code> name -&gt; <code>Set</code> of <code>Resource</code>. */
    private Map<String, Set<Resource>> resourceByName;

    /** <code>Resource</code> parent -&gt; <code>Set</code> of children. */
    private Map<Object, Set<ResourceRef>> resourceByParent;

    /**
     * <code>Resource</code> parent -&gt; <code>Map</code> of <code>String</code> name -&gt ;
     * <code>Set</code> of <code>Resource</code> .
     */
    private Map<Object, Map<String, Set<ResourceRef>>> resourceByParentAndName;

    /** All resources in the system. */
    private Map<String, Set<Resource>> resourceSet;

    private final Object lock = new Object();

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs the {@link CoralStore} implementation.
     * 
     * @param cacheFactory the cache factory.
     * @param persistence the persistence subsystem.
     * @param coralEventHub the event hub.
     * @param instantiator the instantiator.
     * @param coral the component hub.
     * @param log the logger.
     * @throws ConfigurationException if the cache is not configured properly.
     */
    public CoralStoreImpl(CacheFactory cacheFactory, Persistence persistence,
        CoralEventHub coralEventHub, Instantiator instantiator, CoralCore coral, Logger log)
        throws ConfigurationException
    {
        this.persistence = persistence;
        this.coral = coral;
        this.coralEventHub = coralEventHub;
        this.resourceFactory = instantiator.getPersistentFactory(ResourceImpl.class);
        this.log = log;
        setupCache(cacheFactory, "resource");
        resourceByParent = new WeakHashMap<Object, Set<ResourceRef>>();
        resourceByParentAndName = new WeakHashMap<Object, Map<String, Set<ResourceRef>>>();
        cacheFactory.registerForPeriodicExpunge((WeakHashMap<?, ?>)resourceByParent);
        cacheFactory.registerForPeriodicExpunge((WeakHashMap<?, ?>)resourceByParentAndName);
    }

    /**
     * Initializes the caches.
     * 
     * @param cacheFactory the cache factory.
     * @param kind the cache configuration infix.
     * @throws ConfigurationException if the cache is not configured properly.
     */
    private void setupCache(CacheFactory cacheFactory, String kind)
        throws ConfigurationException
    {
        resourceById = cacheFactory.getInstance("coral." + kind + ".byId", "coral.byId");
        resourceByName = cacheFactory.getInstance("coral." + kind + ".byName", "coral.byName");
        resourceSet = cacheFactory.getInstance("coral." + kind + ".all", "coral.all");
    }

    // Resources /////////////////////////////////////////////////////////////

    /**
     * Returns all resources present in the system.
     * <p>
     * Note that this method should never be used during normal operation of the system, because it
     * is potentialy extremely time and memory consuming. It might be even used to launch a DOS
     * attack on the system if it was made availabe to non-privileged users. This method is provided
     * for system assembly and testing purposes only.
     * </p>
     * 
     * @return all resources in the system.
     */
    public Resource[] getResource()
    {
        Set<Resource> rs;
        Resource[] result = null;
        synchronized(lock)
        {
            rs = resourceSet.get("all");
            if(rs != null)
            {
                result = new Resource[rs.size()];
                rs.toArray(result);
            }
        }

        if(rs == null)
        {
            Connection conn = null;
            try
            {
                conn = persistence.getDatabase().getConnection();
                List<ResourceImpl> list = persistence.load(resourceFactory);
                rs = instantiate(list, conn);
            }
            catch(Exception e)
            {
                throw new BackendException("failed to load resource objects", e);
            }
            finally
            {
                DatabaseUtils.close(conn);
            }
            result = new Resource[rs.size()];
            rs.toArray(result);

            synchronized(lock)
            {
                resourceSet.put("all", rs);
            }
        }
        return result;
    }

    /**
     * Returns all sub-resources of a specific {@link Resource}.
     * 
     * @param parent the resource to find children of, or <code>null</code> to find top level
     *        resources.
     * @return the sub-resources of a specific {@link Resource}.
     */
    public Resource[] getResource(Resource parent)
    {
        Set<Resource> rs = null;
        Set<ResourceRef> rrs = null;
        Collection<ResourceRef> rrsc = null;

        synchronized(lock)
        {
            rrs = (Set<ResourceRef>)resourceByParent.get(parent);
            if(rrs != null)
            {
                rrsc = new ArrayList<ResourceRef>(rrs);
            }
        }

        if(rrsc != null)
        {
            rs = deref(rrsc);
        }
        else
        {
            Connection conn = null;
            try
            {
                conn = persistence.getDatabase().getConnection();
                List<ResourceImpl> list;
                if(parent != null)
                {
                    list = persistence.load(resourceFactory, "parent = ?", parent.getId());
                }
                else
                {
                    list = persistence.load(resourceFactory, "parent IS NULL");
                }
                rs = instantiate(list, conn);
                rrs = ref(rs);
            }
            catch(Exception e)
            {
                throw new BackendException("failed to load resource objects", e);
            }
            finally
            {
                DatabaseUtils.close(conn);
            }

            synchronized(lock)
            {
                resourceByParent.put(parent, rrs);
            }
        }

        Resource[] result = new Resource[rs.size()];
        rs.toArray(result);
        return result;
    }

    /**
     * Retrieves the resource with the given identifier.
     * 
     * @param id the identifier.
     * @return the <code>Resource</code>.
     * @throws EntityDoesNotExistException if the <code>Resource</code> with the specified
     *         identifier does not exist.
     */
    public Resource getResource(long id)
        throws EntityDoesNotExistException
    {
        Long idObj = Long.valueOf(id);
        Resource res;

        synchronized(lock)
        {
            res = (Resource)resourceById.get(idObj);
        }

        if(res == null)
        {
            Connection conn = null;
            try
            {
                conn = persistence.getDatabase().getConnection();
                res = (Resource)persistence.load(resourceFactory, id);
                if(res != null)
                {
                    res = res.getResourceClass().getHandler().retrieve(res, conn, null);
                }
                else
                {
                    throw new EntityDoesNotExistException("resource #" + id + " does not exist");
                }
            }
            catch(Exception e)
            {
                if(e instanceof EntityDoesNotExistException)
                {
                    throw (EntityDoesNotExistException)e;
                }
                else
                {
                    throw new BackendException("failed to load resource #" + id, e);
                }
            }
            finally
            {
                DatabaseUtils.close(conn);
            }

            synchronized(lock)
            {
                resourceById.put(idObj, res);
            }
        }
        return res;
    }

    /**
     * Returns all resourcees with the specified name.
     * 
     * @param name the name.
     * @return all <code>Resource</code>es with the given name.
     */
    public Resource[] getResource(String name)
    {
        Resource[] result = null;
        Set<Resource> rs;
        synchronized(lock)
        {
            rs = resourceByName.get(name);
            if(rs != null)
            {
                result = new Resource[rs.size()];
                rs.toArray(result);
            }
        }

        if(rs == null)
        {
            Connection conn = null;
            try
            {
                conn = persistence.getDatabase().getConnection();
                List<ResourceImpl> list = persistence.load(resourceFactory, "name = ?", name);
                rs = instantiate(list, conn);
            }
            catch(Exception e)
            {
                throw new BackendException("failed to load resource objects", e);
            }
            finally
            {
                DatabaseUtils.close(conn);
            }
            result = new Resource[rs.size()];
            rs.toArray(result);

            synchronized(lock)
            {
                resourceByName.put(name, rs);
            }
        }
        return result;
    }

    /**
     * Returns the resource with the specifed name.
     * 
     * @param name the name.
     * @return the resource
     * @throws IllegalStateException if the name denotes multiple resources, or does not exist.
     */
    public Resource getUniqueResource(String name)
        throws IllegalStateException
    {
        Resource[] resources = getResource(name);
        if(resources.length == 0)
        {
            throw new IllegalStateException("resource " + name + " does not exist");
        }
        if(resources.length > 1)
        {
            throw new IllegalStateException("resource " + name + " is not unique");
        }
        return resources[0];
    }

    /**
     * Returns all resources with the specified name among sub-resources of the given resource.
     * 
     * @param parent the parent resource.
     * @param name the name.
     * @return all <code>Resource</code>es with the given name.
     */
    public Resource[] getResource(Resource parent, String name)
    {
        Set<ResourceRef> rrs = null;
        Collection<ResourceRef> rrsc = null;
        Set<Resource> rs = null;
        Map<String, Set<ResourceRef>> nameMap;

        synchronized(lock)
        {
            nameMap = resourceByParentAndName.get(parent);
            if(nameMap != null)
            {
                rrs = nameMap.get(name);
                if(rrs != null)
                {
                    rrsc = new ArrayList<ResourceRef>(rrs);
                }
            }
        }

        if(rrsc != null)
        {
            rs = deref(rrsc);
        }
        else
        {
            Connection conn = null;
            try
            {
                conn = persistence.getDatabase().getConnection();
                List<ResourceImpl> list = persistence.load(resourceFactory,
                    "parent = ? AND name = ?", parent.getId(), name);
                rs = instantiate(list, conn);
                rrs = ref(rs);
            }
            catch(Exception e)
            {
                throw new BackendException("failed to load resource objects", e);
            }
            finally
            {
                DatabaseUtils.close(conn);
            }

            synchronized(lock)
            {
                nameMap = resourceByParentAndName.get(parent);
                if(nameMap == null)
                {
                    nameMap = new HashMap<String, Set<ResourceRef>>();
                    resourceByParentAndName.put(parent, nameMap);
                }
                nameMap.put(name, rrs);
            }
        }

        Resource[] result = new Resource[rs.size()];
        rs.toArray(result);
        return result;
    }

    /**
     * Returns the resource with the specifed name and parent.
     * 
     * @param parent the parent resource.
     * @param name the name.
     * @return the resource.
     * @throws IllegalStateException if the name denotes multiple resources, or does not exist.
     */
    public Resource getUniqueResource(Resource parent, String name)
        throws IllegalStateException
    {
        Resource[] resources = getResource(parent, name);
        if(resources.length == 0)
        {
            throw new IllegalStateException("resource " + name + " does not exist");
        }
        if(resources.length > 1)
        {
            throw new IllegalStateException("resource " + name + " is not unique");
        }
        return resources[0];
    }

    /**
     * Lookup resources denoted by a pathname.
     * <p>
     * The pathname is composed of / separated resource names. If the pathname starts with a / the
     * lookup starts at the 'root' resouce #1, otherwise with all resources with names mathich the
     * first pathname component. If any of the pathname components is not unique, the lookup will
     * fork as neccessary, and all leaf resources mathching the pathname will be returned in the
     * results. You can use wildcard character * as a pathname component, that will match any
     * resource name. The wildcard character does not work recursively!
     * </p>
     * 
     * @param path resource pathname
     * @return resources dentoted by the pathname
     */
    public Resource[] getResourceByPath(String path)
    {
        StringTokenizer st = new StringTokenizer(path, "/");
        List<Resource> in = new ArrayList<Resource>();
        List<Resource> out = new ArrayList<Resource>();
        Resource r;
        Resource[] ra;
        if(st.hasMoreTokens())
        {
            if(path.startsWith("/"))
            {
                try
                {
                    in.add(getResource(ROOT_RESOURCE));
                }
                catch(EntityDoesNotExistException e)
                {
                    throw new BackendException("Failed to lookup root resource", e);
                }
            }
            else
            {
                ra = getResource(st.nextToken());
                for(int i = 0; i < ra.length; i++)
                {
                    in.add(ra[i]);
                }
            }
            while(st.hasMoreTokens())
            {
                String elem = st.nextToken();
                while(!in.isEmpty())
                {
                    r = (Resource)in.remove(0);
                    if(elem.equals("*"))
                    {
                        ra = getResource(r);
                    }
                    else
                    {
                        ra = getResource(r, elem);
                    }
                    for(int i = 0; i < ra.length; i++)
                    {
                        out.add(ra[i]);
                    }
                }
                if(st.hasMoreTokens())
                {
                    in.addAll(out);
                    out.clear();
                }
            }
            ra = new Resource[out.size()];
            out.toArray(ra);
            return ra;
        }
        else
        {
            return new Resource[0];
        }
    }

    /**
     * Returns an unique resource denoted by a pathname.
     * 
     * @param path a pathname
     * @return a resource
     * @throws EntityDoesNotExistException if the path denotes no resources.
     * @throws AmbigousEntityNameException if the path denotes more that one resource.
     */
    public Resource getUniqueResourceByPath(String path)
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        Resource[] res = getResourceByPath(path);
        if(res.length == 0)
        {
            throw new EntityDoesNotExistException(path + " does not exist");
        }
        if(res.length > 1)
        {
            throw new AmbigousEntityNameException("pathname " + path + " is not unique");
        }
        return res[0];
    }

    /**
     * Lookup resource descendatns denoted by a pathname.
     * <p>
     * The pathname is composed of / separated resource names. The pathname is considered to be
     * relative to the resource <code>start</code> If any of the pathname components is not unique,
     * the lookup will fork as neccessary, and all leaf resources mathching the pathname will be
     * returned in the results. You can use wildcard character * as a pathname component, that will
     * match any resource name. The wildcard character does not work recursively!
     * </p>
     * 
     * @param start the resource where the lookup should start.
     * @param path resource pathname.
     * @return resources dentoted by the pathname
     */
    public Resource[] getResourceByPath(Resource start, String path)
    {
        StringTokenizer st = new StringTokenizer(path, "/");
        List<Resource> in = new ArrayList<Resource>();
        in.add(start);
        List<Resource> out = new ArrayList<Resource>();
        Resource r;
        Resource[] ra;
        if(st.hasMoreTokens())
        {
            while(st.hasMoreTokens())
            {
                String elem = st.nextToken();
                while(!in.isEmpty())
                {
                    r = (Resource)in.remove(0);
                    if(elem.equals("*"))
                    {
                        ra = getResource(r);
                    }
                    else
                    {
                        ra = getResource(r, elem);
                    }
                    for(int i = 0; i < ra.length; i++)
                    {
                        out.add(ra[i]);
                    }
                }
                if(st.hasMoreTokens())
                {
                    in.addAll(out);
                    out.clear();
                }
            }
            ra = new Resource[out.size()];
            out.toArray(ra);
            return ra;
        }
        else
        {
            return new Resource[0];
        }
    }

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
        throws EntityDoesNotExistException, AmbigousEntityNameException
    {
        String combinedPath = start.getPath() + (path.startsWith("/") ? "" : "/") + path;
        Resource[] res = getResourceByPath(start, path);
        if(res.length == 0)
        {
            throw new EntityDoesNotExistException(combinedPath + " does not exist");
        }
        if(res.length > 1)
        {
            throw new AmbigousEntityNameException("pathname " + combinedPath + " is not unique");
        }
        return res[0];
    }

    /**
     * Creates a resource image in the persistent storage.
     * 
     * @param name the name of the new resource.
     * @param parent the parent of the new resource (may be <code>null</code>).
     * @param resourceClass the class of the new resource.
     * @param attributes the mapping of {@link AttributeDefinition} objects into initial values of
     *        the attributes.
     * @return the newly created resource.
     * @throws UnknownAttributeException if the <code>attribute</code> map contains a key that does
     *         not belong to <code>resourceClass</code> attributes.
     * @throws ValueRequiredException if a value of a REQIRED attribute is defined present in
     *         <code>attributes</code>.
     * @throws InvalidResourceNameException if the name contains invalid chracters.
     */
    public <T extends Resource> T createResource(String name, Resource parent,
        ResourceClass<T> resourceClass, Map<AttributeDefinition<?>, Object> attributes)
        throws InvalidResourceNameException, UnknownAttributeException, ValueRequiredException
    {
        if(name == null || name.equals(""))
        {
            throw new ValueRequiredException("resource name cannot be NULL or empty");
        }
        checkNameValidity(name);
        Subject creator = coral.getCurrentSubject();
        if((resourceClass.getFlags() & ResourceClassFlags.ABSTRACT) != 0)
        {
            throw new IllegalArgumentException("cannot instantiate ABSTRACT class "
                + resourceClass.getName());
        }
        Connection conn = null;
        boolean shouldCommit = false;
        T res = null;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            conn = persistence.getDatabase().getConnection();
            Resource delegate = new ResourceImpl(persistence, coral, coralEventHub, name,
                resourceClass, parent, creator);
            persistence.save((Persistent)delegate);
            res = resourceClass.getHandler().create(delegate, attributes, conn);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(BackendException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(SQLException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to create resource", e);
        }
        finally
        {
            DatabaseUtils.close(conn);
        }

        synchronized(lock)
        {
            resourceById.put(res.getIdObject(), res);
            Set<Resource> rs = resourceByName.get(res.getName());
            if(rs != null)
            {
                rs.add(res);
            }
            if(parent != null)
            {
                Set<ResourceRef> rrs = resourceByParent.get(parent);
                if(rrs != null)
                {
                    rrs.add(new ResourceRef(res, coral));
                }
                Map<String, Set<ResourceRef>> nameMap = resourceByParentAndName.get(parent);
                if(nameMap != null)
                {
                    rrs = nameMap.get(res.getName());
                    if(rrs != null)
                    {
                        rrs.add(new ResourceRef(res, coral));
                    }
                }
            }
            rs = resourceSet.get("all");
            if(rs != null)
            {
                rs.add(res);
            }
        }

        // inform listeners about new resource
        coralEventHub.getGlobal().fireResourceCreationEvent(res);

        return res;
    }

    /**
     * Removes a resource.
     * 
     * @param resource the resource to remove.
     * @throws EntityInUseException if the resource has subresources.
     */
    public void deleteResource(Resource resource)
        throws EntityInUseException
    {
        // inform listeners that the resource is about to be deleted
        coralEventHub.getGlobal().fireResourceDeletionEvent(resource);

        Connection conn = null;
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            conn = persistence.getDatabase().getConnection();
            int count = 0;
            Set<ResourceRef> children;

            synchronized(lock)
            {
                children = resourceByParent.get(resource);
                if(children != null)
                {
                    count = children.size();
                }
            }

            if(children == null)
            {
                count = persistence.count("coral_resource", "parent = " + resource.getIdString());
            }
            if(count != 0)
            {
                throw new EntityInUseException("resource #" + resource.getIdString() + " has "
                    + count + " children");
            }
            Set<PermissionAssignment> assignments = coral.getRegistry().getPermissionAssignments(
                resource);
            for(PermissionAssignment pa : assignments)
            {
                coral.getRegistry().deletePermissionAssignment(pa);
            }
            resource.getResourceClass().getHandler().delete(resource, conn);
            persistence.delete((Persistent)resource.getDelegate());

            synchronized(lock)
            {
                resourceById.remove(resource.getIdObject());
                Set<Resource> rs = resourceByName.get(resource.getName());
                if(rs != null)
                {
                    rs.remove(resource);
                }
                Set<ResourceRef> rrs = resourceByParent.get(resource.getParent());
                ResourceRef ref = new ResourceRef(resource, coral);
                if(rrs != null)
                {
                    rrs.remove(ref);
                }
                Map<String, Set<ResourceRef>> nameMap = resourceByParentAndName.get(resource
                    .getParent());
                if(nameMap != null)
                {
                    rrs = nameMap.get(resource.getName());
                    if(rrs != null)
                    {
                        rrs.remove(ref);
                    }
                }
                Set<Resource> all = resourceSet.get("all");
                if(all != null)
                {
                    all.remove(resource);
                }
            }

            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(BackendException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(EntityInUseException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            if(e instanceof SQLException)
            {
                SQLException sqle = (SQLException)e;
                log.error("resource removal: SQLState:" + sqle.getSQLState() + " error code:"
                    + sqle.getErrorCode(), e);
            }
            throw new BackendException("failed to delete resource #" + resource.getIdString(), e);
        }
        finally
        {
            DatabaseUtils.close(conn);
        }
    }

    /**
     * Delete a subtree of resources recursively.
     * <p>
     * There are two situations where this method may fail. First, a cycle made of non-clearable
     * reference attributes may exists among the resources in the tree. This will be detected and
     * reported, no partial delete will occur. Second, resources may be references by resources from
     * the outside of the tree. This situation will NOT be detected in the current implementation
     * and will result in partial delete - the resources will disappear from their place though and
     * will end up in /tmp where the administrator may remove them manually. Detecting this requires
     * explicit reference tracking.
     * </p>
     * 
     * @param res the root of the tree to delete.
     * @return the number of deleted resources.
     * @throws EntityInUseException if resources in the selected tree are referenced from outside of
     *         the tree and cannot be deleted.
     */
    public int deleteTree(Resource res)
        throws EntityInUseException
    {
        Deque<Resource> stack = new LinkedList<Resource>();
        List<Resource> order = new ArrayList<Resource>();
        stack.push(res);
        while(stack.size() > 0)
        {
            Resource r = stack.pop();
            Resource[] children = getResource(r);
            for(int i = 0; i < children.length; i++)
            {
                stack.push(children[i]);
            }
            order.add(r);
        }
        // check integrity prequisites - checks for loops of the
        // dependencies that cannot be handled by this implementation
        checkDeletionIntegrityPrequisites(order);

        // call all parties interested in cleanup based on resource hierarchy
        coralEventHub.getGlobal().fireResourceTreeDeletionEvent(res);

        // sort the resources so that referential dependency trees
        // (REQUIRED and READONLY scalar references only) are deleted
        // in the proper order
        int count = order.size();
        List<Resource> newOrder = new ArrayList<Resource>(order.size());
        while(!order.isEmpty())
        {
            Resource r = order.get(0);
            List<Resource> refTree = getRefTree(r, order);
            order.removeAll(refTree);
            Collections.reverse(refTree);
            newOrder.addAll(refTree);
        }
        if(count != newOrder.size())
        {
            throw new BackendException("sorting error " + count + " before, " + newOrder.size()
                + " after");
        }
        order = newOrder;
        // break constraints based on composite reference attributes
        // and non-REQUIRED/non-READONLY scalar reference attributes
        for(Resource r : order)
        {
            r.getResourceClass().getHandler().clearResourceReferences(r);
        }
        // break structural constraints
        Resource tmp = getTempNode();
        for(Resource r : order)
        {
            try
            {
                setParent(r, tmp);
            }
            catch(Exception e)
            {
                throw new BackendException("unexpected exception", e);
            }
        }
        // perform the deletion
        while(order.size() > 0)
        {
            Resource r = order.remove(order.size() - 1);
            deleteResource(r);
        }
        return count;
    }

    private void checkDeletionIntegrityPrequisites(List<Resource> resources)
        throws EntityInUseException
    {
        Deque<Resource> stack = new LinkedList<Resource>();
        Set<Resource> set = new HashSet<Resource>();
        for(Resource r : resources)
        {
            stack.push(r);
            set.clear();
            set.add(r);
            while(!stack.isEmpty())
            {
                r = stack.pop();
                Resource[] deps = r.getResourceClass().getHandler().getResourceReferences(r, false);
                for(int j = 0; j < deps.length; j++)
                {
                    if(set.contains(deps[j]) && resources.contains(deps[j]))
                    {
                        throw new EntityInUseException("referential dependency loop on #"
                            + r.getIdString());
                    }
                    stack.push(deps[j]);
                    set.add(deps[j]);
                }
            }
        }
    }

    private Resource getTempNode()
    {
        try
        {
            return getUniqueResourceByPath("/tmp");
        }
        catch(EntityDoesNotExistException e)
        {
            try
            {
                Resource rootResource = getResource(1);
                ResourceClass<Resource> rc = coral.getSchema().getResourceClass("coral.Node",
                    Resource.class);
                return createResource("tmp", rootResource, rc,
                    Collections.<AttributeDefinition<?>, Object> emptyMap());
            }
            catch(Exception ee)
            {
                throw new BackendException("could not create /tmp node", e);
            }
        }
        catch(AmbigousEntityNameException e)
        {
            throw new BackendException("more than one /tmp resource exits", e);
        }
    }

    private List<Resource> getRefTree(Resource r, List<Resource> all)
    {
        Deque<Resource> stack = new LinkedList<Resource>();
        List<Resource> tree = new ArrayList<Resource>();
        stack.push(r);
        tree.add(r);
        while(!stack.isEmpty())
        {
            r = stack.pop();
            Resource[] deps = r.getResourceClass().getHandler().getResourceReferences(r, false);
            for(int j = 0; j < deps.length; j++)
            {
                stack.push(deps[j]);
                tree.add(deps[j]);
            }
        }
        // prune nodes outside of the deletion tree
        tree.retainAll(all);
        return tree;
    }

    /**
     * Renames the resource.
     * 
     * @param resource the resource to rename.
     * @param name the new name of the resource.
     * @throws InvalidResourceNameException if the name contains invalid chracters.
     */
    public void setName(Resource resource, String name)
        throws InvalidResourceNameException
    {
        checkNameValidity(name);
        String oldName = resource.getName();
        ResourceImpl delegate = (ResourceImpl)resource.getDelegate();
        try
        {
            ((ResourceImpl)delegate).setResourceName(name);
            persistence.save(delegate);

            synchronized(lock)
            {
                Set<Resource> rs = resourceByName.get(oldName);
                if(rs != null)
                {
                    rs.remove(resource);
                }
                rs = resourceByName.get(name);
                if(rs != null)
                {
                    rs.add(resource);
                }
                Resource parent = resource.getParent();
                if(parent != null)
                {
                    Map<String, Set<ResourceRef>> nameMap = resourceByParentAndName.get(parent);
                    if(nameMap != null)
                    {
                        Set<ResourceRef> rrs = nameMap.get(oldName);
                        ResourceRef ref = new ResourceRef(resource, coral);
                        if(rrs != null)
                        {
                            rrs.remove(ref);
                        }
                        rrs = nameMap.get(name);
                        if(rrs != null)
                        {
                            rrs.add(ref);
                        }
                    }
                }
            }

            coralEventHub.getOutbound().fireResourceChangeEvent(resource, null);
        }
        catch(SQLException e)
        {
            delegate.setResourceName(oldName);
            throw new BackendException("failed to update the resource object", e);
        }
    }

    /**
     * Creates a parent -- child relationship among two resources.
     * 
     * @param child the child resource.
     * @param parent the parent resource.
     * @throws CircularDependencyException if the <code>child</code> is actually a parent of
     *         <code>parent</code>.
     */
    public void setParent(Resource child, Resource parent)
        throws CircularDependencyException
    {
        if(parent == null)
        {
            throw new IllegalArgumentException(
                "use unsetParent() to delete parent-child relationships");
        }
        if(parent.equals(child))
        {
            throw new CircularDependencyException("cannot make a resource the parent of itself");
        }
        if(isAncestor(child, parent))
        {
            throw new CircularDependencyException("the resource : '" + child.getPath()
                + "' is an ancestor of the resource: '" + parent.getPath() + "'");
        }
        Resource oldParent = child.getParent();
        ResourceImpl delegate = (ResourceImpl)child.getDelegate();
        try
        {
            delegate.setParent(parent);
            persistence.save(delegate);

            synchronized(lock)
            {
                Set<ResourceRef> rrs = resourceByParent.get(oldParent);
                ResourceRef ref = new ResourceRef(child, coral);
                if(rrs != null)
                {
                    rrs.remove(ref);
                }
                rrs = resourceByParent.get(parent);
                if(rrs != null)
                {
                    rrs.add(ref);
                }

                String name = child.getName();
                Map<String, Set<ResourceRef>> nameMap = resourceByParentAndName.get(oldParent);
                if(nameMap != null)
                {
                    rrs = nameMap.get(name);
                    if(rrs != null)
                    {
                        rrs.remove(ref);
                    }
                }
                nameMap = resourceByParentAndName.get(parent);
                if(nameMap != null)
                {
                    rrs = nameMap.get(name);
                    if(rrs != null)
                    {
                        rrs.add(ref);
                    }
                }
            }

            if(oldParent != null)
            {
                coralEventHub.getGlobal().fireResourceTreeChangeEvent(
                    new ResourceInheritanceImpl(oldParent, child), false);
            }
            coralEventHub.getGlobal().fireResourceTreeChangeEvent(
                new ResourceInheritanceImpl(parent, child), true);
        }
        catch(SQLException e)
        {
            delegate.setParent(oldParent);
            throw new BackendException("failed to update the resource object", e);
        }
    }

    /**
     * Removes a parent -- child relationship among two resources.
     * 
     * @param child the child resource.
     */
    public void unsetParent(Resource child)
    {
        Resource oldParent = child.getParent();
        ResourceImpl delegate = (ResourceImpl)child.getDelegate();
        try
        {
            delegate.setParent(null);
            persistence.save(delegate);

            synchronized(lock)
            {
                Set<ResourceRef> rrs = resourceByParent.get(oldParent);
                ResourceRef ref = new ResourceRef(child, coral);
                if(rrs != null)
                {
                    rrs.remove(ref);
                }

                String name = child.getName();
                Map<String, Set<ResourceRef>> nameMap = resourceByParentAndName.get(oldParent);
                if(nameMap != null)
                {
                    rrs = nameMap.get(name);
                    if(rrs != null)
                    {
                        rrs.remove(ref);
                    }
                }
            }

            coralEventHub.getGlobal().fireResourceTreeChangeEvent(
                new ResourceInheritanceImpl(oldParent, child), false);
        }
        catch(SQLException e)
        {
            delegate.setParent(oldParent);
            throw new BackendException("failed to update the resource object", e);
        }
    }

    /**
     * Changes the owner of the resource.
     * 
     * @param resource the resource.
     * @param owner the new owner.
     */
    public void setOwner(Resource resource, Subject owner)
    {
        Subject oldOwner = resource.getOwner();
        ResourceImpl delegate = (ResourceImpl)resource.getDelegate();
        try
        {
            delegate.setOwner(owner);
            persistence.save((Persistent)delegate);
            coralEventHub.getGlobal().fireResourceOwnershipChangeEvent(
                new ResourceOwnershipImpl(oldOwner, resource), false);
            coralEventHub.getGlobal().fireResourceOwnershipChangeEvent(
                new ResourceOwnershipImpl(owner, resource), true);
        }
        catch(SQLException e)
        {
            delegate.setOwner(oldOwner);
            throw new BackendException("failed to update the resource object", e);
        }
    }

    // private ///////////////////////////////////////////////////////////////

    /**
     * Instantiates concrete resource objects.
     * 
     * @param list the list of security delegate objects.
     * @param conn the JDBC connection to use.
     * @return a set of concrete resource objects.
     */
    private Set<Resource> instantiate(List<ResourceImpl> list, Connection conn)
        throws SQLException
    {
        Set<Resource> result = new HashSet<Resource>();
        Set<ResourceImpl> toLoad = new HashSet<ResourceImpl>();
        Map<Long, Resource> loaded = new HashMap<Long, Resource>();

        synchronized(lock)
        {
            for(ResourceImpl rd : list)
            {
                Long key = rd.getIdObject();
                Resource cached = (Resource)resourceById.get(key);
                if(cached != null)
                {
                    result.add(cached);
                }
                else
                {
                    toLoad.add(rd);
                }
            }
        }

        for(ResourceImpl rd : toLoad)
        {
            ResourceHandler<?> handler = rd.getResourceClass().getHandler();
            Resource r = handler.retrieve(rd, conn, null);
            result.add(r);
            loaded.put(rd.getIdObject(), r);
        }

        synchronized(lock)
        {
            resourceById.putAll(loaded);
        }

        return result;
    }

    private Set<ResourceRef> ref(Set<Resource> set)
    {
        Set<ResourceRef> rset = new HashSet<ResourceRef>(set.size());
        for(Resource r : set)
        {
            rset.add(new ResourceRef(r, coral));
        }
        return rset;
    }

    private Set<Resource> deref(Collection<ResourceRef> rset)
    {
        try
        {
            Set<Resource> set = new HashSet<Resource>(rset.size());
            for(ResourceRef rr : rset)
            {
                set.add(rr.get());
            }
            return set;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("inconsistent resource tree", e);
        }
    }

    /**
     * Creates a copy of a resource.
     * 
     * @param source the resource to copy.
     * @param destinationParent the parent resource of the copy.
     * @param destinationName the name of the copy.
     * @return the copy.
     * @throws InvalidResourceNameException if the name contains invalid chracters.
     */
    public Resource copyResource(Resource source, Resource destinationParent, String destinationName)
        throws InvalidResourceNameException
    {
        Map<AttributeDefinition<?>, Object> attrs = new HashMap<AttributeDefinition<?>, Object>();
        AttributeDefinition<?>[] atDefs = source.getResourceClass().getAllAttributes();
        for(int i = 0; i < atDefs.length; i++)
        {
            attrs.put(atDefs[i], source.get(atDefs[i]));
        }
        try
        {
            return createResource(destinationName, destinationParent, source.getResourceClass(),
                attrs);
        }
        catch(ValueRequiredException e)
        {
            throw new BackendException("inconsitent resource data", e);
        }
        catch(UnknownAttributeException e)
        {
            throw new BackendException("inconsitent schema", e);
        }
    }

    /**
     * Copies a resource tree to another location.
     * <p>
     * Non-Resource attribute values, and Resource attrbute values poiting to Resources outside the
     * source tree are copied by value. Resource attributes pointig to Resources inside the source
     * tree are treated as relative, and will be converted to point to the corresponding resources
     * in the destination tree. Resolution of relative resource references is done in a separate
     * pass after cloning the resources themselves. Any relative reference being a READONLY
     * attribute will be left poining to a resource in the source tree, and a warning will be issued
     * to the log.
     * </p>
     * 
     * @param sourceRoot the root of the source tree.
     * @param destinationParent the parent of root node of the destination tree.
     * @param destinationName the name of root node of the destination tree.
     * @throws InvalidResourceNameException if the name contains invalid chracters.
     * @throws CircularDependencyException if the destination parent is a child of source root.
     */
    public void copyTree(Resource sourceRoot, Resource destinationParent, String destinationName)
        throws InvalidResourceNameException, CircularDependencyException
    {
        if(isAncestor(sourceRoot, destinationParent))
        {
            throw new CircularDependencyException("the tree with parent: '" + sourceRoot.getPath()
                + "' cannot be copied " + "under his descendant: '" + destinationParent.getPath());
        }
        String srcBasePath = sourceRoot.getPath();
        String dstBasePath = destinationParent.getPath() + "/" + destinationName;
        Deque<Resource> srcStack = new LinkedList<Resource>();
        Deque<Resource> dstStack = new LinkedList<Resource>();
        Resource dstRoot = copyResource(sourceRoot, destinationParent, destinationName);
        srcStack.push(sourceRoot);
        dstStack.push(dstRoot);
        while(srcStack.size() > 0)
        {
            Resource src = srcStack.pop();
            Resource dst = dstStack.pop();
            Resource[] srcChildren = getResource(src);
            for(int i = 0; i < srcChildren.length; i++)
            {
                srcStack.push(srcChildren[i]);
                dstStack.push(copyResource(srcChildren[i], dst, srcChildren[i].getName()));
            }
        }

        dstStack.add(dstRoot);
        while(dstStack.size() > 0)
        {
            Resource dst = dstStack.pop();
            AttributeDefinition<?>[] atDefs = dst.getResourceClass().getAllAttributes();
            boolean changed = false;
            for(int i = 0; i < atDefs.length; i++)
            {
                Object attr = dst.get(atDefs[i]);
                if(attr instanceof Resource)
                {
                    Resource res = (Resource)attr;
                    if(res.getPath().startsWith(srcBasePath))
                    {
                        String targetName = dstBasePath
                            + res.getPath().substring(srcBasePath.length());
                        Resource[] target = getResourceByPath(targetName);
                        if(target.length != 1)
                        {
                            log.warn(
                                "leaving resource #" + dst.getIdString() + " (" + dst.getPath()
                                    + ") attribute " + atDefs[i].getName()
                                    + " pointing to resource #" + res.getIdString() + " ("
                                    + res.getPath() + ") because " + targetName
                                    + " is an ambigous resource path", null);
                        }
                        else if((atDefs[i].getFlags() & AttributeFlags.READONLY) != 0)
                        {
                            log.warn(
                                "leaving resource #" + dst.getIdString() + " (" + dst.getPath()
                                    + ") attribute " + atDefs[i].getName()
                                    + " pointing to resource #" + res.getIdString() + " ("
                                    + res.getPath() + ") because " + atDefs[i].getName()
                                    + " is a READONLY attribute", null);
                        }
                        else
                        {
                            try
                            {
                                @SuppressWarnings("unchecked")
                                AttributeDefinition<Resource> resAtDef = (AttributeDefinition<Resource>)atDefs[i];
                                dst.set(resAtDef, target[0]);
                                changed = true;
                            }
                            catch(Exception e)
                            {
                                log.warn(
                                    "leaving resource #" + dst.getIdString() + " (" + dst.getPath()
                                        + ") attribute " + atDefs[i].getName()
                                        + " pointing to resource #" + res.getIdString() + " ("
                                        + res.getPath() + ") because of "
                                        + "an unexpected exception", e);
                            }
                        }
                    }
                }
            }
            if(changed)
            {
                dst.update();
            }
            Resource[] children = getResource(dst);
            for(int i = 0; i < children.length; i++)
            {
                dstStack.push(children[i]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAncestor(Resource ancestor, Resource descendant)
    {
        if(ancestor == null || descendant == null)
        {
            return false;
        }
        Resource parent = descendant.getParent();
        while(parent != null)
        {
            if(parent.equals(ancestor))
            {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Checks if a given string is a valid resource name.
     * 
     * @param name the string to be checked.
     * @return <code>true</code> if the name conatins no illegal characters.
     */
    public boolean isValidResourceName(String name)
    {
        return !ILLEGAL_NAME_PATTERN.matcher(name).find();
    }

    /**
     * Returns a string that contains all characters from a given string that are not allowed inside
     * a resource name.
     * 
     * @param name the string to be checked.
     * @return a string that contains the illegal characters. If the string is a valid resource name
     *         empty string is returned. Each invalid character appers once in the output.
     */
    public String getInvalidResourceNameCharacters(String name)
    {
        StringBuilder buff = new StringBuilder();
        Matcher matcher = ILLEGAL_NAME_PATTERN.matcher(name);
        while(matcher.find())
        {
            String found = matcher.group();
            if(!buff.toString().contains(found))
            {
                buff.append(found);
            }
        }
        return buff.toString();
    }

    /**
     * Checks if given resource name is valid, throws exception if not.
     * 
     * @param name the resource name.
     * @throws InvalidResourceNameException if the given resource name is invalid.
     */
    protected void checkNameValidity(String name)
        throws InvalidResourceNameException
    {
        if(coral.isEnabled(Feature.RESOURCE_NAME_VALIDITY_ENFORCEMENT)
            && !isValidResourceName(name))
        {
            String characters = getInvalidResourceNameCharacters(name);
            throw new InvalidResourceNameException("invalid characters " + characters
                + " in resource name", characters);
        }
    }

    public void replaceSubjectReferences(Subject fromSubject, Subject toSubject)
        throws ModificationNotPermitedException
    {
        try
        {
            Map<AttributeDefinition<Subject>, long[]> m1 = getResouceBySubjectMetadata(fromSubject);
            for(AttributeClass<?> ac : coral.getSchema().getAllAttributeClasses())
            {
                if(Subject.class.isAssignableFrom(ac.getJavaClass()))
                {
                    AttributeHandler<Subject> h = (AttributeHandler<Subject>)ac.getHandler();
                    m1.putAll(h.getResourcesByValue(fromSubject));
                }
            }
            Map<Long, Set<AttributeDefinition<Subject>>> m2 = invert(m1);
            for(Map.Entry<Long, Set<AttributeDefinition<Subject>>> entry : m2.entrySet())
            {
                long id = entry.getKey();
                try
                {
                    final Resource r = getResource(id);
                    final ResourceImpl delegate = (ResourceImpl)r.getDelegate();
                    final Date modificationTime = r.getModificationTime();
                    Subject modifiedBy = r.getModifiedBy();
                    for(AttributeDefinition<Subject> ad : entry.getValue())
                    {
                        if(ad.getName().equals("created_by"))
                        {
                            delegate.setCreaedBy(toSubject);
                        }
                        else if(ad.getName().equals("modified_by"))
                        {
                            modifiedBy = toSubject;
                        }
                        else if(ad.getName().equals("owner"))
                        {
                            delegate.setOwner(toSubject);
                        }
                        else
                        {
                            r.set(ad, toSubject);
                        }
                    }
                    r.update();
                    delegate.setModified(modifiedBy, modificationTime);
                    persistence.save(delegate);
                }
                catch(EntityDoesNotExistException e)
                {
                    // skip
                }
            }
        }
        catch(UnknownAttributeException | ValueRequiredException e)
        {
            throw new BackendException("concurrent schema change", e);
        }
        catch(SQLException e)
        {
            throw new BackendException("database operation failed", e);
        }
    }

    private Map<Long, Set<AttributeDefinition<Subject>>> invert(
        Map<AttributeDefinition<Subject>, long[]> in)
    {
        Map<Long, Set<AttributeDefinition<Subject>>> result = new HashMap<>();
        for(Map.Entry<AttributeDefinition<Subject>, long[]> entry : in.entrySet())
        {
            AttributeDefinition<Subject> ad = entry.getKey();
            for(long id : entry.getValue())
            {
                Set<AttributeDefinition<Subject>> ads = result.get(id);
                if(ads == null)
                {
                    ads = new HashSet<>();
                    result.put(id, ads);
                }
                ads.add(ad);
            }
        }
        return result;
    }

    public Map<AttributeDefinition<Subject>, long[]> getResouceBySubjectMetadata(Subject subject)
    {
        Map<String, AttributeDefinition<Subject>> builtins = getBuiltinSubjectAttributes();
        String query = resourceByMetadataClause("created_by", builtins) + "\nUNION ALL\n"
            + resourceByMetadataClause("modified_by", builtins) + "\nUNION ALL\n"
            + resourceByMetadataClause("owned_by", builtins);
        try(Connection conn = persistence.getDatabase().getConnection();
            PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setLong(1, subject.getId());
            stmt.setLong(2, subject.getId());
            stmt.setLong(3, subject.getId());
            try(ResultSet rset = stmt.executeQuery())
            {
                List<Long> cList = new ArrayList<>();
                List<Long> mList = new ArrayList<>();
                List<Long> oList = new ArrayList<>();
                while(rset.next())
                {
                    final long id = rset.getLong(2);
                    final String col = rset.getString(1);
                    if(col.trim().equals("created_by"))
                    {
                        cList.add(id);
                    }
                    else if(col.trim().equals("modified_by"))
                    {
                        mList.add(id);
                    }
                    else if(col.trim().equals("owned_by"))
                    {
                        oList.add(id);
                    }
                }
                Map<AttributeDefinition<Subject>, long[]> result = new HashMap<>();
                result.put(builtins.get("created_by"), pack(cList));
                result.put(builtins.get("modified_by"), pack(mList));
                result.put(builtins.get("owner"), pack(oList));
                return result;
            }
        }
        catch(SQLException e)
        {
            throw new BackendException("failed to retrieve resource data", e);
        }
    }

    private long[] pack(List<Long> l)
    {
        long[] result = new long[l.size()];
        for(int i = 0; i < l.size(); i++)
        {
            result[i] = l.get(i);
        }
        return result;
    }

    private String resourceByMetadataClause(String attribute,
        Map<String, AttributeDefinition<Subject>> builtins)
    {
        return "SELECT '" + attribute + "', resource_id FROM coral_resource WHERE "
            + attribute + " = ?";
    }

    private Map<String, AttributeDefinition<Subject>> getBuiltinSubjectAttributes()
    {
        Map<String, AttributeDefinition<Subject>> result = new HashMap<>();
        for(AttributeDefinition<?> attr : coral.getSchema().getAllAttributes())
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) != 0
                && Subject.class.isAssignableFrom(attr.getAttributeClass().getJavaClass()))
            {
                result.put(attr.getName(), (AttributeDefinition<Subject>)attr);
            }
        }
        return result;
    }

    // startup //////////////////////////////////////////////////////////////////////////////////

    private static final int[] STARTUP_PHASES = {};

    /**
     * {@inheritDoc}
     */
    public int[] getPhases()
    {
        return STARTUP_PHASES;
    }

    /**
     * {@inheritDoc}
     */
    public void preloadData(int phase)
        throws Exception
    {
    }

    @Override
    public EntityFactory<Resource> getResourceFactory()
    {
        return new EntityFactory<Resource>()
            {
                @Override
                public Resource getEntity(long id)
                    throws EntityDoesNotExistException
                {
                    return getResource(id);
                }
            };
    }

    @Override
    public <T> T getResource(long id, Class<T> clazz)
        throws EntityDoesNotExistException
    {
        final Resource resource = getResource(id);
        return clazz.cast(resource);
    }
}
