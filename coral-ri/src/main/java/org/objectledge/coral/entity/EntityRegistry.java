package org.objectledge.coral.entity;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.Instantiator;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.database.persistence.PersistentFactory;

/**
 * This class manages 'by id' 'by name' and 'all' registires for a specific
 * entity type.
 */
public class EntityRegistry<E extends Persistent & Entity>
{
    /** The persistence service. */
    private Persistence persistence;
    
    /** The logger. */
    private Logger log;
    
    /** The factory of the entity objects. */
    private PersistentFactory<E> factory;
    
    /** The type of entities */
    private Class<E> type;

    /** The semantic name of the entity type used in error messages. */
    private String kind;

    /** The semantic name of the entity type used in error messages in the 
     *  plural form. */
    private String kindPlural;

    /** The identifier to entity map. */
    private Map<Long, E> byId;
    
    /** The name to entity set map. */
    private Map<String, Set<E>> byName;
    
    /** The "all" to entity set map. */
    private Map<String, Set<E>> all;
    
    /** Objects added through addSynthetic() */
    private Set<E> synthetics;

    /** The under which the value set is stored in 'all' map */
    private static final String ALL_KEY = "all";

    /** The setName(String) method. */
    private Method setName;

    /**
     * Creates a registry for the specific entity type.
     *
     * @param persistence the Persistence subsystem.
     * @param cacheFactory the CacheFactory.
     * @param instantiator the component instantiator.
     * @param log the Logger to use.
     * @param kind the semantic name of the entity type.
     * @param type the entity implementation class.
     * @throws ConfigurationException if the cache is not configured properly.
     */
    public EntityRegistry(Persistence persistence, CacheFactory cacheFactory, 
        Instantiator instantiator, Logger log, 
        String kind, final Class<E> type)
        throws ConfigurationException
    {
        this.persistence = persistence;
        this.log = log;
        this.kind = kind;
        this.kindPlural = (kind.charAt(kind.length()-1) == 's') ? kind+"s" : kind+"es";
        this.type = type;

        if(!Entity.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException(type.getName()+
                " does not implement Entity interface");
        }
        if(!Persistent.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException(type.getName()+
                " does not implement Persistent intreface");
        }
        
        this.factory = instantiator.getPersistentFactory(type);

        Class<?> cl = type;
        if(AbstractEntity.class.isAssignableFrom(type))
        {
            // AbstractEntity has setName() that is local to our package
            cl = AbstractEntity.class;
        }
        while(setName == null && cl != null)
        {
            try
            {
                setName = cl.getDeclaredMethod("setName", new Class[] { String.class });
            }
            catch(NoSuchMethodException e)
            {
                cl = cl.getSuperclass();
            }       
        }
        setupCache(cacheFactory, kind);
    }
    
    private void setupCache(CacheFactory caching, String kind)
        throws ConfigurationException
    {
        if(kind.indexOf(' ') > 0)
        {
            StringTokenizer st = new StringTokenizer(kind, " ");
            StringBuilder buff = new StringBuilder();
            buff.append(st.nextToken());
            while(st.hasMoreTokens())
            {
                String t = st.nextToken();
                buff.append(Character.toUpperCase(t.charAt(0)));
                buff.append(t.substring(1));
            }
            kind = buff.toString();
        }
        byId = caching.getInstance("coral."+kind+".byId", "coral.byId");
        byName = caching.getInstance("coral."+kind+".byName", "coral.byName");
        all = caching.getInstance("coral."+kind+".all", "coral.all");
    }

    /**
     * Returns all entities of the specified type.
     *
     * @return all entities of the specified type.
     */
    public synchronized Set<E> get()
    {
        Set<E> es = all.get(ALL_KEY);
        if(es == null)
        {
            es = new HashSet<E>();
            all.put(ALL_KEY, es);
            try
            {
                List<E> items = persistence.load(null, factory);
                resolve(items, es);
                if(synthetics != null)
                {
                    es.addAll(synthetics);
                }
            }
            catch(PersistenceException ex)
            {
                throw new BackendException("failed to load "+kindPlural, ex);
            }
        }
        return es;
    }
    
    /**
     * Returns the entity with the specific id.
     *
     * @param id the id of the entity
     * @return the entity with the specific id.
     * @throws EntityDoesNotExistException if the entity with the specified
     *         id does not exist.
     */
    public Entity get(long id)
        throws EntityDoesNotExistException
    {
        Long idObj = new Long(id);
        E e = byId.get(idObj);
        if(e == null)
        {
            try
            {
                e = persistence.load(id, factory);
            }
            catch(PersistenceException ex)
            {
                throw new BackendException("failed to load "+kind+" #"+id, ex);
            }
            if(e == null)
            {
                throw new EntityDoesNotExistException(kind+" #"+id+
                    " does not exist");
            }
            byId.put(idObj, e);
        }
        return e;
    }

    /**
     * Returns all entities with the specific name.
     *
     * @param name the name of the entities.
     * @return all entities with the specific name.
     */
    public Set<E> get(String name)
    {
        Set<E> es = byName.get(name);
        if(es == null)
        {
            es = new HashSet<E>();
            byName.put(name, es);
            try
            {
                List<E> items = persistence.load("name = '"+DatabaseUtils.escapeSqlString(name)+"'", 
                    factory);
                resolve(items, es);
            }
            catch(PersistenceException ex)
            {
                throw new BackendException("failed to load "+kindPlural, ex);
            }
        }
        return es;
    }

    /**
     * Returns the entity with the specific name.
     *
     * @param name the name of the entity. 
     * @return the entity with the specific name.
     * @throws EntityDoesNotExistException if the entity with the specified
     *         id does not exist.
     * @throws AmbigousEntityNameException if the name specified denotes more than
     *         one entity
     */
    public Entity getUnique(String name)
        throws AmbigousEntityNameException, EntityDoesNotExistException
    {
        Set<E> es = get(name);
        if(es.size() == 0)
        {
            throw new EntityDoesNotExistException(kind+" '"+name+"' does not exist");
        }
        if(es.size() > 1)
        {
            throw new AmbigousEntityNameException("'"+name+"' denotes more than one "+kind);
        }
        return (Entity)es.toArray()[0];
    }

    /**
     * Adds an entity to the registry.
     *
     * @param entity the entity.
     */
    public void add(E entity)
    {
        try
        {
            persistence.save(entity);
        }
        catch(PersistenceException ex)
        {
            throw new BackendException("failed to save "+type+" to storage", ex);
        }
        byId.put(entity.getIdObject(), entity);
        Set<E> es = byName.get(entity.getName());
        if(es != null)
        {
            es.add(entity);
        }
        es = all.get(ALL_KEY);
        if(es != null)
        {
            es.add(entity);
        }                
    }
    
    /**
     * Adds a synthetic entity to the directly.
     * 
     * <p>This method does not attempt to save the entity to storage - should be used for 
     * synthetic entities.</p> 
     * 
     * @param entity the entity to add.
     */
    public void addSynthetic(E entity)
    {
        byId.put(entity.getIdObject(), entity);
        Set<E> es = byName.get(entity.getName());
        if(es == null)
        {
            es = new HashSet<E>();
            byName.put(entity.getName(), es);
        }
        es.add(entity);
        es = all.get(ALL_KEY);
        if(es != null)
        {
            es.add(entity);
        }
        if(synthetics == null)
        {
            synthetics = new HashSet<E>();
        }
        synthetics.add(entity);
    }
    
    /**
     * Adds an entity to the registry.
     *
     * @param entity the entity.
     * @throws EntityExistsException if an entity with the same name as the
     *         specified already exists in the system.
     */
    public void addUnique(E entity)
        throws EntityExistsException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            if(persistence.exists(((Persistent)entity).getTable(),
                   "name = '"+entity.getName()+"'"))
            {
                throw new EntityExistsException(kind+" '"+entity.getName()+
                    "' already exists");
            }
            add(entity); 
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(PersistenceException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to check name uniqueness", ex);
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
        catch(EntityExistsException e)
        {
        	throw e;
        }
        catch(Exception ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to add "+kind, ex);
        }   
    }

    /**
     * Deletes an entity from the registry.
     *
     * @param entity the entity.
     */
    public void delete(E entity)
    {
        try
        {
            persistence.delete(entity);
        }
        catch(PersistenceException ex)
        {
            throw new BackendException("failed to delete "+kind+" #"+entity.getIdString(), ex);
        }
        byId.remove(entity.getIdObject());
        Set<E> es = byName.get(entity.getName());
        if(es != null)
        {
            es.remove(entity);
        }
        es = all.get(ALL_KEY);
        if(es != null)
        {
            es.remove(entity);
        }
    }

    /**
     * Renames an entity.
     *
     * @param entity the entity. 
     * @param name the new name.
     */
    public void rename(E entity, String name)
    {
        String oldName = entity.getName();
        if(setName == null)
        {
            throw new UnsupportedOperationException("rename operation is not supported, "+
                "because "+type.getName()+" does not have an accessible setName(String) method");
        }
        try
        {
            setName.invoke(entity, new Object[] { name });
            try
            {
                persistence.save(entity);

                Set<E> es = byName.get(oldName);
                if(es != null)
                {
                    es.remove(entity);
                }
                es = byName.get(name);
                if(es != null)
                {
                    es.add(entity);
                }
            }
            catch(PersistenceException exx)
            {
                throw new BackendException("failed to save "+kind, exx);
            }            
        }
        catch(Exception ex)
        {
            throw new BackendException("failed to invoke setName method", ex);
        }
    }

    /**
     * Renames an entity.
     *
     * @param entity the entity. 
     * @param name the new name. 
     * @throws EntityExistsException if an entity with the same name as the
     *         specified already exists in the system.
     */
    public void renameUnique(E entity, String name)
        throws EntityExistsException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            if(persistence.exists(((Persistent)entity).getTable(),
                   "name = '"+name+"'"))
            {
                throw new EntityExistsException(kind+" '"+name+
                    "' already exists");
            }
            rename(entity, name);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(EntityExistsException ex)
        {
        	throw ex;
        }
        catch(PersistenceException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to check name uniqueness", ex);
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
        catch(Exception ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to add "+kind, ex);
        }   
    }
    
    /**
     * Resolves a set of loaded objects against the caches.
     * 
     * @param in entities loaded from the db
     * @param out equivalent entity set with new objects replaced with cached ones if possible.
     */
    public synchronized void resolve(List<E> in, Set<E> out)
    {
        Iterator<E> i = in.iterator();
        while(i.hasNext())
        {
            E e = i.next();
            Long id = e.getIdObject();
            E ee = byId.get(id);
            if(ee == null)
            {
                byId.put(id, e);
                Set<E> nameSet = byName.get(e.getName());
                if(nameSet == null)
                {
                    nameSet = new HashSet<E>();
                    byName.put(e.getName(), nameSet);
                }
                nameSet.add(e);
                out.add(e);
            }
            else
            {
                out.add(ee);
            }
        }
    }
}
