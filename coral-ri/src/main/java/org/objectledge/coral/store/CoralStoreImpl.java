package org.objectledge.coral.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Subject;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.database.persistence.PersistentFactory;

/**
 * Manages resource instances.
 *
 * @version $Id: CoralStoreImpl.java,v 1.1 2004-03-03 14:46:22 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralStoreImpl
    implements CoralStore
{
    // Instance variables ////////////////////////////////////////////////////////////////////////

    private Database database;
    
    private Persistence persistence;
    
    private CoralEventHub coralEventHub;
    
    private CoralRegistry coralRegistry;
    
    private CoralSchema coralSchema;
    
    private CoralSecurity coralSecurity;
    
    private Logger log;

    /** The <code>PersistentFactory</code> for <code>Resource</code>
     * objects. */
    protected PersistentFactory resourceFactory;
    
    /** <code>Long</code> id -&gt; <code>Resource<code> */
    protected Map resourceById;
    
    /** <code>String</code> name -&gt; <code>Set</code> of <code>Resource</code> */
    protected Map resourceByName;
    
    /** <code>Resource</code> parent -&gt; <code>Set</code> of children */
    protected Map resourceByParent;

    /** <code>Resource</code> parent -&gt; <code>Map</code> of
        <code>String</code> name -&gt ; <code>Set</code> of <code>Resource</code> */
    protected Map resourceByParentAndName;

    /** All resources in the system. */
    protected Map resourceSet;

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs the {@link StoreService} implementation.
     * 
     * @param cacheFactory the cache factory.
     * @param database the database.
     * @param persistence the persistence subsystem.
     * @param coralRegistry the CoralRegistry.
     * @param coralEventHub the event hub.
     * @param coralSchema the coral schema.
     * @param coralSecurity the coral security.
     * @param log the logger.
     * @throws ConfigurationException if the cache is not configured properly.
     */
    public CoralStoreImpl(CacheFactory cacheFactory, Database database, Persistence persistence,
        CoralRegistry coralRegistry, CoralEventHub coralEventHub, CoralSchema coralSchema,
        CoralSecurity coralSecurity, Logger log)
        throws ConfigurationException
    {
        this.database = database;
        this.persistence = persistence;
        this.coralRegistry = coralRegistry;
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
        this.coralRegistry = coralRegistry;
        this.log = log;
        setupCache(cacheFactory, "resource");
        resourceByParent = new WeakHashMap();
        resourceByParentAndName = new WeakHashMap();
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
        resourceById = cacheFactory.getInstance("arl."+kind+".byId", "arl.byId");
        resourceByName = cacheFactory.getInstance("arl."+kind+".byName", "arl.byName");
        resourceSet = cacheFactory.getInstance("arl."+kind+".all", "arl.all");
    }
    
    // Resources /////////////////////////////////////////////////////////////

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
    public Resource[] getResource()
    {
        synchronized(resourceSet)
        {
            Set rs = (Set)resourceSet.get("all");
            if(rs == null)
            {
                Connection conn = null;
                boolean shouldCommit = false;
                try
                {
                    conn = database.getConnection();
                    shouldCommit = database.beginTransaction();
                    List list = persistence.load(" true ORDER BY resource_id", resourceFactory);
                    rs = instantiate(list, conn);
                    database.commitTransaction(shouldCommit);
                }
                catch(BackendException ex)
                {
                    try
                    {
                        database.rollbackTransaction(shouldCommit);
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
                        database.rollbackTransaction(shouldCommit);
                    }
                    catch(SQLException ee)
                    {
                        log.error("rollback failed", ee);
                    }
                    throw new BackendException("failed to load resource objects", e);
                }
                finally
                {
                    DatabaseUtils.close(conn);
                }
                resourceSet.put("all", rs);
            }
            Resource[] result = new Resource[rs.size()];
            rs.toArray(result);
            return result;
        }
    }

    /**
     * Returns all sub-resources of a specific {@link Resource}.
     *
     * @param parent the resource to find children of, or <code>null</code> to
     *        find top level resources.
     * @return the sub-resources of a specific {@link Resource}.
     */
    public Resource[] getResource(Resource parent)
    {
        synchronized(resourceByParent)
        {
            Set rs = (Set)resourceByParent.get(parent);
            if(rs == null)
            {
                Connection conn = null;
                boolean shouldCommit = false;
                try
                {
                    conn = database.getConnection();
                    shouldCommit = database.beginTransaction();
                    List list;
                    if(parent != null)
                    {
                        list = persistence.load("parent = "+parent.getId(),
                                                    resourceFactory);
                    }
                    else
                    {
                        list = persistence.load("parent IS NULL",
                                                    resourceFactory);
                    }
                    
                    rs = instantiate(list, conn);
                    database.commitTransaction(shouldCommit);
                }
                catch(BackendException ex)
                {
                    try
                    {
                        database.rollbackTransaction(shouldCommit);
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
                        database.rollbackTransaction(shouldCommit);
                    }
                    catch(SQLException ee)
                    {
                        log.error("rollback failed", ee);
                    }
                    throw new BackendException("failed to load resource objects", e);
                }
                finally
                {
                    DatabaseUtils.close(conn);
                }
                resourceByParent.put(parent, rs);
            }
            Resource[] result = new Resource[rs.size()];
            rs.toArray(result);
            return result;
        }
    }

    /**
     * Retrieves the resource  with the given identifier.
     *
     * @param id the identifier.
     * @return the <code>Resource</code>.
     * @throws EntityDoesNotExistException if the <code>Resource</code>
     *         with the specified identifier does not exist.
     */
    public Resource getResource(long id)
        throws EntityDoesNotExistException
    {
        synchronized(resourceById)
        {
            Long idObj = new Long(id);
            Resource res = (Resource)resourceById.get(idObj);
            if(res == null)
            {
                Connection conn = null;
                boolean shouldCommit = false;
                try
                {
                    conn = database.getConnection();
                    shouldCommit = database.beginTransaction();
                    res = (Resource)persistence.load(id,resourceFactory);
                    if(res != null)
                    {
                        res = res.getResourceClass().getHandler().
                            retrieve(res, conn);
                    }
                    else
                    {
                        throw new EntityDoesNotExistException("resource #"+id+" does not exist");
                    }
                    database.commitTransaction(shouldCommit);
                }
                catch(BackendException ex)
                {
                    try
                    {
                        database.rollbackTransaction(shouldCommit);
                    }
                    catch(SQLException ee)
                    {
                        log.error("rollback failed", ee);
                    }
                    throw ex;
                }
                catch(EntityDoesNotExistException ex)
                {
                    try
                    {
                        database.rollbackTransaction(shouldCommit);
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
                        database.rollbackTransaction(shouldCommit);
                    }
                    catch(SQLException ee)
                    {
                        log.error("rollback failed", ee);
                    }
                    throw new BackendException("failed to load resource #"+id, e);
                }
                finally
                {
                    DatabaseUtils.close(conn);
                }
                resourceById.put(idObj, res);
            }
            return res;
        }
    }
    
    /**
     * Returns all resourcees with the specified name.
     *
     * @param name the name.
     * @return all <code>Resource</code>es with the given name.
     */
    public Resource[] getResource(String name)
    {
        synchronized(resourceByName)
        {
            Set rs = (Set)resourceByName.get(name);
            if(rs == null)
            {
                Connection conn = null;
                boolean shouldCommit = false;
                try
                {
                    conn = database.getConnection();
                    shouldCommit = database.beginTransaction();
                    List list = persistence.load("name = '"+name+"'",
                                                     resourceFactory);
                    rs = instantiate(list, conn);
                    database.commitTransaction(shouldCommit);
                }
                catch(BackendException ex)
                {
                    try
                    {
                        database.rollbackTransaction(shouldCommit);
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
                        database.rollbackTransaction(shouldCommit);
                    }
                    catch(SQLException ee)
                    {
                        log.error("rollback failed", ee);
                    }
                    throw new BackendException("failed to load resource objects", e);
                }
                finally
                {
                    DatabaseUtils.close(conn);
                }
                resourceByName.put(name, rs);
            }
            Resource[] result = new Resource[rs.size()];
            rs.toArray(result);
            return result;
        }
    }

    /**
     * Returns the resource with the specifed name.
     *
     * @param name the name.
     * @return the resource
     * @throws IllegalStateException if the name denotes multiple resources,
     *         or does not exist.
     */
    public Resource getUniqueResource(String name)
        throws IllegalStateException
    {
        Resource[] resources = getResource(name);
        if(resources.length == 0)
        {
            throw new IllegalStateException("resource "+name+" does not exist");
        }
        if(resources.length > 1)
        {
            throw new IllegalStateException("resource "+name+" is not unique");
        }
        return resources[0];
    }
    
    /**
     * Returns all resources with the specified name among sub-resources
     * of the given resource.
     *
     * @param parent the parent resource.
     * @param name the name.
     * @return all <code>Resource</code>es with the given name.
     */
    public Resource[] getResource(Resource parent, String name)
    {
        synchronized(resourceByParentAndName)
        {
            Map nameMap = (Map)resourceByParentAndName.get(parent);
            if(nameMap == null)
            {
                nameMap = new HashMap();
                resourceByParentAndName.put(parent, nameMap);
            }
            Set rs = (Set)nameMap.get(name);
            if(rs == null)
            {
                Connection conn = null;
                boolean shouldCommit = false;
                try
                {
                    conn = database.getConnection();
                    shouldCommit = database.beginTransaction();
                    List list = persistence.load("parent = "+parent.getId()+
                                                     " AND name = '"+name+"'",
                                                     resourceFactory);
                    rs = instantiate(list, conn);
                    database.commitTransaction(shouldCommit);
                }
                catch(BackendException ex)
                {
                    try
                    {
                        database.rollbackTransaction(shouldCommit);
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
                        database.rollbackTransaction(shouldCommit);
                    }
                    catch(SQLException ee)
                    {
                        log.error("rollback failed", ee);
                    }
                    throw new BackendException("failed to load resource objects", e);
                }
                finally
                {
                    DatabaseUtils.close(conn);
                }
                nameMap.put(name, rs);
            }
            Resource[] result = new Resource[rs.size()];
            rs.toArray(result);
            return result;
        }
    }

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
        throws IllegalStateException
    {
        Resource[] resources = getResource(parent, name);
        if(resources.length == 0)
        {
            throw new IllegalStateException("resource "+name+" does not exist");
        }
        if(resources.length > 1)
        {
            throw new IllegalStateException("resource "+name+" is not unique");
        }
        return resources[0];
    }
    
    /**
     * Lookup resources denoted by a pathname.
     *
     * <p>The pathname is composed of / separated resource names. If the
     * pathname starts with a / the lookup starts at the 'root' resouce #1,
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
    public Resource[] getResourceByPath(String path)
    {
        StringTokenizer st = new StringTokenizer(path, "/");
        List in = new ArrayList();
        List out = new ArrayList();
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
                for(int i=0; i<ra.length; i++)
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
                    for(int i=0; i<ra.length; i++)
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
            throw new EntityDoesNotExistException(path+" does not exist");
        }
        if(res.length > 1)
        {
            throw new AmbigousEntityNameException("pathname "+path+" is not unique");
        }
        return res[0];
    }

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
    public Resource[] getResourceByPath(Resource start, String path)
    {
        StringTokenizer st = new StringTokenizer(path, "/");
        List in = new ArrayList();
        in.add(start);
        List out = new ArrayList();
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
                    for(int i=0; i<ra.length; i++)
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
        String combinedPath = start.getPath()+(path.startsWith("/") ? "" : "/")+
            path;
        Resource[] res = getResourceByPath(start, path);
        if(res.length == 0)
        {
            throw new EntityDoesNotExistException(combinedPath+" does not exist");
        }
        if(res.length > 1)
        {
            throw new AmbigousEntityNameException("pathname "+combinedPath+" is not unique");
        }
        return res[0];
    }

    /**
     * Creates a resource image in the persistent storage.
     *
     * @param name the name of the new resource.
     * @param parent the parent of the new resource (may be
     * <coed>null</code>).
     * @param resourceClass the class of the new resource.
     * @param attributes the mapping of {@link AttributeDefinition} objects
     *        into initial values of the attributes.
     * @param creator the subject that creates the resource.
     * @return the newly created resource.
     * @throws UnknownAttributeException if the <code>attribute</code> map
     *         contains a key that does not belong to
     *         <code>resourceClass</code> attributes.
     * @throws ValueRequiredException if a value of a REQIRED attribute is defined
     *         present in <code>attributes</code>.
     */
    public Resource createResource(String name, Resource parent, ResourceClass resourceClass,
                                   Map attributes, Subject creator)
        throws UnknownAttributeException,
               ValueRequiredException
    {
        if((resourceClass.getFlags() & ResourceClassFlags.ABSTRACT) != 0)
        {
            throw new IllegalArgumentException("cannot instantiate ABSTRACT class "+
                                               resourceClass.getName());
        }
        Connection conn = null;
        boolean shouldCommit = false;
        Resource res = null;
        try
        {
            conn = database.getConnection();
            shouldCommit = database.beginTransaction();
            Resource delegate = new ResourceImpl(persistence, this, coralSchema, coralSecurity,
            coralRegistry, coralEventHub, name, resourceClass, parent, creator);
            persistence.save((Persistent)delegate);
            res = delegate.getResourceClass().getHandler().
                create(delegate, attributes, conn);
            database.commitTransaction(shouldCommit);
        }
        catch(BackendException ex)
        {
            try
            {
                database.rollbackTransaction(shouldCommit);
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
                database.rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to create resource", e);
        }
        catch(PersistenceException e)
        {
            try
            {
                database.rollbackTransaction(shouldCommit);
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
        synchronized(resourceById)
        {
            resourceById.put(new Long(res.getId()), res);
        }
        synchronized(resourceByName)
        {
            Set rs = (Set)resourceByName.get(res.getName());
            if(rs != null)
            {
                rs.add(res);
            }
        }
        if(parent != null)
        {
            synchronized(resourceByParent)
            {
                Set children = (Set)resourceByParent.get(parent);
                if(children != null)
                {
                    children.add(res);
                }
            }
            synchronized(resourceByParentAndName)
            {
                Map nameMap = (Map)resourceByParentAndName.get(parent);
                if(nameMap != null)
                {
                    Set rs = (Set)nameMap.get(res.getName());
                    if(rs != null)
                    {
                        rs.add(res);
                    }
                }
            }
        }                    
        synchronized(resourceSet)
        {
            Set rs = (Set)resourceSet.get("all");
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
    	
        synchronized(resourceById)
        {
            synchronized(resourceByName)
            {
                synchronized(resourceByParent)
                {
                    synchronized(resourceByParentAndName)
                    {
                        synchronized(resourceSet)
                        {
                            Connection conn = null;
                            boolean shouldCommit = false;
                            try
                            {
                                conn = database.getConnection();
                                shouldCommit = database.beginTransaction();
                                int count;
                                Set children = (Set)resourceByParent.get(resource);
                                if(children != null)
                                {
                                    count = children.size();
                                }
                                else
                                {
                                    count = persistence.count("arl_resource", "parent = "+
                                                                  resource.getId());
                                }
                                if(count != 0)
                                {
                                    throw new EntityInUseException("resource #"+resource.getId()+
                                                                   " has "+count+" children");
                                }
                                Set assignments = coralRegistry.getPermissionAssignments(resource);
                                Iterator i = assignments.iterator();
                                while(i.hasNext())
                                {
                                    PermissionAssignment pa = (PermissionAssignment)i.next();
                                    coralRegistry.deletePermissionAssignment(pa);           
                                }
                                resource.getResourceClass().getHandler().
                                    delete(resource, conn);
                                persistence.delete((Persistent)resource.getDelegate());
                                resourceById.remove(new Long(resource.getId()));
                                Set rs = (Set)resourceByName.get(resource.getName());
                                if(rs != null)
                                {
                                    rs.remove(resource);
                                }
                                rs = (Set)resourceByParent.get(resource.getParent());
                                if(rs != null)
                                {
                                    rs.remove(resource);
                                }
                                Map nameMap = (Map)resourceByParentAndName.
                                    get(resource.getParent());
                                if(nameMap != null)
                                {
                                    rs = (Set)nameMap.get(resource.getName());
                                    if(rs != null)
                                    {
                                        rs.remove(resource);
                                    }
                                }
                                Set all = (Set)resourceSet.get("all");
                                if(all != null)
                                {
                                    all.remove(resource);
                                }
                                database.commitTransaction(shouldCommit);
                            }
                            catch(BackendException ex)
                            {
                                try
                                {
                                    database.rollbackTransaction(shouldCommit);
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
                                    database.rollbackTransaction(shouldCommit);
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
                                    database.rollbackTransaction(shouldCommit);
                                }
                                catch(SQLException ee)
                                {
                                    log.error("rollback failed", ee);
                                }
                                if(e instanceof SQLException)
                                {
                                    SQLException sqle = (SQLException)e;
                                    log.error("resource removal: SQLState:"+
                                              sqle.getSQLState()+
                                              " error code:"+sqle.getErrorCode(), e);
                                }
                                throw new BackendException("failed to delete resource #"+
                                    resource.getId(), e);
                            }
                            finally
                            {
                                DatabaseUtils.close(conn);
                            }
                        }
                    }
                }
            }
        }
    }

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
     * @throws EntityInUseException if resources in the selected tree are referenced from outside
     *         of the tree and cannot be deleted.
     */
    public int deleteTree(Resource res)
        throws EntityInUseException
    {
        List stack = new ArrayList();
        List order = new ArrayList();
        stack.add(res);
        while(stack.size() > 0)
        {
            Resource r = (Resource)stack.remove(stack.size()-1);
            Resource[] children = getResource(r);
            for(int i=0; i<children.length; i++)
            {
                stack.add(children[i]);
            }
            order.add(r);
        }
        // check integrity prequisites - checks for loops of the
        // dependencies that cannot be handled by this implementation
        checkDeletionIntegrityPrequisites(order);
        
        // call all parties interested in cleanup based on resource hierarchy
		coralEventHub.getGlobal().fireResourceTreeDeletionEvent(res);
		
        // sort the resources so that referntial dependency trees
        // (REQUIRED and READONLY scalar references only) are deleted
        // in the proper order
        int count = order.size();
        List newOrder = new ArrayList(order.size());
        while(!order.isEmpty())
        {
            Resource r = (Resource)order.get(0);
            List refTree = getRefTree(r, order);
            order.removeAll(refTree);
            Collections.reverse(refTree);
            newOrder.addAll(refTree);
        }
        if(count != newOrder.size())
        {
            throw new BackendException("sorting error "+count+" before, "+newOrder.size()+" after");
        }
        order = newOrder;
        // break constraints based on composite reference attributes
        // and non-REQUIRED/non-READONLY scalar reference attributes
        Iterator i = order.iterator();
        while(i.hasNext())
        {
            Resource r = (Resource)i.next();
            r.getResourceClass().getHandler().clearResourceReferences(r);
        }
        // break structural constraints
        Resource tmp = getTempNode();
        i = order.iterator();
        while(i.hasNext())
        {
            Resource r = (Resource)i.next();
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
            Resource r = (Resource)order.remove(order.size()-1);
            deleteResource(r);
        }
        return count;
    }

    private void checkDeletionIntegrityPrequisites(List resources)
        throws EntityInUseException
    {
        Iterator i = resources.iterator();
        List stack = new ArrayList();
        Set set = new HashSet();
        while(i.hasNext())
        {
            Resource r = (Resource)i.next();
            stack.add(r);
            set.clear();
            set.add(r);
            while(!stack.isEmpty())
            {
                r = (Resource)stack.remove(stack.size()-1);
                Resource[] deps = r.getResourceClass().getHandler().getResourceReferences(r, false);
                for(int j=0; j<deps.length; j++)
                {
                    if(set.contains(deps[j]) && resources.contains(deps[j]))
                    {
                        throw new EntityInUseException("referential dependency loop on #"+
                            r.getId());            
                    }
                    stack.add(deps[j]);
                    set.add(deps[j]);
                }
            }
        }
    }
    
    private synchronized Resource getTempNode()
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
                Subject rootSubject = coralSecurity.getSubject(Subject.ROOT);
                ResourceClass rc = coralSchema.getResourceClass("node");
                return createResource("tmp", rootResource, rc, new HashMap(), rootSubject);
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

    private List getRefTree(Resource r, List all)
    {
        List stack = new ArrayList();
        List tree = new ArrayList();
        stack.add(r);
        tree.add(r);
        while(!stack.isEmpty())
        {
            r = (Resource)stack.remove(stack.size()-1);
            Resource[] deps = r.getResourceClass().getHandler().getResourceReferences(r, false);
            for(int j=0; j<deps.length; j++)
            {
                stack.add(deps[j]);
                tree.add(deps[j]);
            }
        }
        // prune nodes outsite of the deletion tree
        tree.retainAll(all);
        return tree;
    }

    /**
     * Renames the resource.
     *
     * @param resource the resource to rename.
     * @param name the new name of the resource.
     */
    public void setName(Resource resource, String name)
    {
        synchronized(resourceByName)
        {
            synchronized(resourceByParentAndName)
            {
                String oldName = resource.getName();
                ResourceImpl delegate = (ResourceImpl)resource.getDelegate();
                try
                {
                    ((ResourceImpl)delegate).setName(name);
                    persistence.save(delegate);
                    Set rs = (Set)resourceByName.get(oldName);
                    if(rs != null)
                    {
                        rs.remove(resource);
                    }
                    rs = (Set)resourceByName.get(name);
                    if(rs != null)
                    {
                        rs.add(resource);
                    }
                    Resource parent = resource.getParent();
                    if(parent != null)
                    {
                        Map nameMap = (Map)resourceByParentAndName.get(parent);
                        if(nameMap != null)
                        {
                            rs = (Set)nameMap.get(oldName);
                            if(rs != null)
                            {
                                rs.remove(resource);
                            }
                            rs = (Set)nameMap.get(name);
                            if(rs != null)
                            {
                                rs.add(resource);
                            }
                        }
                    } 
                    coralEventHub.getOutbound().fireResourceChangeEvent(resource, null);
                }
                catch(PersistenceException e)
                {
                    delegate.setName(oldName);
                    throw new BackendException("failed to update the resource object");
                }
            }
        }
    }
    
    /**
     * Creates a parent -- child relationship among two resources.
     *
     * @param child the child resource.
     * @param parent the parent resource.
     * @throws CircularDependencyException if the <code>child</code> is
     *         actually a parent of <code>parent</code>.
     */
    public void setParent(Resource child, Resource parent)
        throws CircularDependencyException
    {
        if(parent == null)
        {
            throw new IllegalArgumentException(
                "use unsetParent() to delete parent-child relationships");
        }
        synchronized(resourceByParent)
        {
            synchronized(resourceByParentAndName)
            {
                Resource oldParent = child.getParent();
                ResourceImpl delegate = (ResourceImpl)child.getDelegate();
                try
                {
                    delegate.setParent(parent);
                    persistence.save(delegate);

                    Set rs = (Set)resourceByParent.get(oldParent);
                    if(rs != null)
                    {
                        rs.remove(child);
                    }
                    rs = (Set)resourceByParent.get(parent);
                    if(rs != null)
                    {
                        rs.add(child);
                    }

                    String name = child.getName();
                    Map nameMap = (Map)resourceByParentAndName.get(oldParent);
                    if(nameMap != null)
                    {
                        rs = (Set)nameMap.get(name);
                        if(rs != null)
                        {
                            rs.remove(child);
                        }
                    }
                    nameMap = (Map)resourceByParentAndName.get(parent);
                    if(nameMap != null)
                    {
                        rs = (Set)nameMap.get(name);
                        if(rs != null)
                        {
                            rs.add(child);
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
                catch(PersistenceException e)
                {
                    delegate.setParent(oldParent);
                    throw new BackendException("failed to update the resource object");
                }
            }
        }
    }
    
    /**
     * Removes a parent -- child relationship among two resources.
     * 
     * @param child the child resource.
     */
    public void unsetParent(Resource child)  
    {
        synchronized(resourceByParent)
        {
            synchronized(resourceByParentAndName)
            {
                Resource oldParent = child.getParent();
                ResourceImpl delegate = (ResourceImpl)child.getDelegate();
                try
                {
                    delegate.setParent(null);
                    persistence.save(delegate);

                    Set rs = (Set)resourceByParent.get(oldParent);
                    if(rs != null)
                    {
                        rs.remove(child);
                    }

                    String name = child.getName();
                    Map nameMap = (Map)resourceByParentAndName.get(oldParent);
                    if(nameMap != null)
                    {
                        rs = (Set)nameMap.get(name);
                        if(rs != null)
                        {
                            rs.remove(child);
                        }
                    }

                    coralEventHub.getGlobal().fireResourceTreeChangeEvent(
                        new ResourceInheritanceImpl(oldParent, child), false);
                }
                catch(PersistenceException e)
                {
                    delegate.setParent(oldParent);
                    throw new BackendException("failed to update the resource object");
                }
            }
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
        catch(PersistenceException e)
        {
            delegate.setOwner(oldOwner);
            throw new BackendException("failed to update the resource object");
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
    private Set instantiate(List list, Connection conn)
        throws SQLException
    {
        Iterator i = list.iterator();
        Set result = new HashSet();
        while(i.hasNext())
        {
            Resource res = (Resource)i.next();
            Long key = new Long(res.getId());
            Resource cached = (Resource)resourceById.get(key);
            if(cached == null)
            {
                ResourceHandler handler = res.getResourceClass().getHandler();
                res = handler.retrieve(res, conn);
                result.add(res);
                resourceById.put(key, res);
            }
            else
            {
                result.add(cached);
            }
        }
        return result;
    }

    /**
     * Creates a copy of a resource.
     *
     * @param source the resource to copy.
     * @param destinationParent the parent resource of the copy.
     * @param destinationName the name of the copy.
     * @param subject the subject performing the operation.
     * @return the copy.
     */
    public Resource copyResource(Resource source, Resource destinationParent, 
                                 String destinationName, Subject subject)
    {
        HashMap attrs = new HashMap();
        AttributeDefinition[] atDefs = source.getResourceClass().getAllAttributes();
        for(int i=0; i<atDefs.length; i++)
        {
            attrs.put(atDefs[i], source.get(atDefs[i]));
        }
        try
        {
            return createResource(destinationName, destinationParent, 
                                  source.getResourceClass(), attrs, subject);
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
     * @param subject the subject performing the operation.
     */
    public void copyTree(Resource sourceRoot, Resource destinationParent, 
                         String destinationName, Subject subject)
    {
        String srcBasePath = sourceRoot.getPath();
        String dstBasePath = destinationParent.getPath()+"/"+destinationName;
        ArrayList srcStack = new ArrayList();
        ArrayList dstStack = new ArrayList();
        Resource dstRoot = copyResource(sourceRoot, destinationParent, 
                                        destinationName, subject);
        srcStack.add(sourceRoot);
        dstStack.add(dstRoot);
        while(srcStack.size() > 0)
        {
            Resource src = (Resource)srcStack.remove(srcStack.size()-1);
            Resource dst = (Resource)dstStack.remove(dstStack.size()-1);
            Resource[] srcChildren = getResource(src);
            for(int i=0; i<srcChildren.length; i++)
            {
                srcStack.add(srcChildren[i]);
                dstStack.add(copyResource(srcChildren[i], dst, 
                                          srcChildren[i].getName(), subject));
            }
        }

        dstStack.add(dstRoot);
        while(dstStack.size() > 0)
        {
            Resource dst = (Resource)dstStack.remove(dstStack.size()-1);
            AttributeDefinition[] atDefs = dst.getResourceClass().getAllAttributes();
            boolean changed = false;
            for(int i=0; i<atDefs.length; i++)
            {
                Object attr = dst.get(atDefs[i]);
                if(attr instanceof Resource)
                {
                    Resource res = (Resource)attr;
                    if(res.getPath().startsWith(srcBasePath))
                    {
                        String targetName = dstBasePath+res.getPath().
                            substring(srcBasePath.length());
                        Resource[] target = getResourceByPath(targetName);
                        if(target.length != 1)
                        {
                            log.warn("leaving resource #"+dst.getId()+" ("+
                                        dst.getPath()+") attribute "+atDefs[i].getName()+
                                        " pointing to resource #"+res.getId()+" ("+
                                        res.getPath()+") because "+targetName+
                                        " is an ambigous resource path", null);
                        } 
                        else if((atDefs[i].getFlags() & AttributeFlags.READONLY) != 0)
                        {
                            log.warn("leaving resource #"+dst.getId()+" ("+
                                        dst.getPath()+") attribute "+atDefs[i].getName()+
                                        " pointing to resource #"+res.getId()+" ("+
                                        res.getPath()+") because "+atDefs[i].getName()+
                                        " is a READONLY attribute", null);
                        }
                        else
                        {
                            try
                            {
                                dst.set(atDefs[i], target[0]);
                                changed = true;
                            }
                            catch(Exception e)
                            {
                                log.warn("leaving resource #"+dst.getId()+" ("+
                                            dst.getPath()+") attribute "+atDefs[i].getName()+
                                            " pointing to resource #"+res.getId()+" ("+
                                            res.getPath()+") because of "+
                                            "an unexpected exception",e);
                            }   
                        }
                    }
                }
            }
            if(changed)
            {
                dst.update(subject);
            }
            Resource[] children = getResource(dst);
            for(int i=0; i<children.length; i++)
            {
                dstStack.add(children[i]);
            }
        }
    }
}
