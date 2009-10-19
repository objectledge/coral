package org.objectledge.coral.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Collection of helper methods for manipulating Entities.
 * 
 * @author rafal
 */
public class EntityUtils
{
    /**
     * A private constructor to prevent instantiation.
     */
    private EntityUtils()
    {
    }

    /**
     * Converts a collection of entities into a comma separated string of entity ids.
     * <p>
     * Note that information about Entity class is not included in the string.
     * </p>
     * 
     * @param <E> Entity subclass.
     * @param entities a Collection of Entities.
     * @return comma separated String of Entity ids.
     */
    public static <E extends Entity> String entitiesToIds(Collection<E> entities)
    {
        StringBuilder sb = new StringBuilder();
        Iterator<E> i = entities.iterator();
        while(i.hasNext())
        {
            sb.append(i.next().getIdString());
            if(i.hasNext())
            {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Converts an array of Entity ids into a list of Entities using specified
     * factory.
     * 
     * @param <E> Entity subclass.
     * @param ids array of Entity id's
     * @param factory an Entity factory.
     * @return a list of Entities.
     * @throws EntityDoesNotExistException if the factory is unable to provide Entities with
     *         specified ids
     */
    public static <E extends Entity> List<E> idsToEntityList(String[] ids, EntityFactory<E> factory)
        throws EntityDoesNotExistException
    {
        List<E> entities = new ArrayList<E>(ids.length);
        for (String id : ids)
        {
            entities.add(factory.getEntity(Long.parseLong(id.trim())));
        }
        return entities;
    }

    /**
     * Converts a comma separated string of Entity ids into a list of Entities using specified
     * factory.
     * 
     * @param <E> Entity subclass.
     * @param idString comma separated string of Entity id's
     * @param factory an Entity factory.
     * @return a list of Entities.
     * @throws NumberFormatException if the string contains numbers that cannot be parsed correctly
     * @throws EntityDoesNotExistException if the factory is unable to provide Entities with
     *         specified ids
     */
    public static <E extends Entity> List<E> idsToEntityList(String idString,
        EntityFactory<E> factory)
        throws NumberFormatException, EntityDoesNotExistException
    {
        if(idString.trim().length() > 0)
        {
            return idsToEntityList(idString.split(","), factory);
        }
        else
        {
            return new ArrayList<E>(0);
        }
    }

    /**
     * Converts an array of Entity ids into a set of Entities using specified
     * factory.
     * 
     * @param <E> Entity subclass.
     * @param ids array of Entity id's
     * @param factory an Entity factory.
     * @return a set of Entities.
     * @throws EntityDoesNotExistException if the factory is unable to provide Entities with
     *         specified ids
     */
    public static <E extends Entity> Set<E> idsToEntitySet(String[] ids, EntityFactory<E> factory)
        throws EntityDoesNotExistException
    {
        Set<E> entities = new HashSet<E>(ids.length);
        for (String id : ids)
        {
            entities.add(factory.getEntity(Long.parseLong(id.trim())));
        }
        return entities;
    }

    /**
     * Converts a comma separated string of Entity ids into a set of Entities using specified
     * factory.
     * 
     * @param <E> Entity subclass.
     * @param idString comma separated string of Entity id's
     * @param factory an Entity factory.
     * @return a set of Entities.
     * @throws NumberFormatException if the string contains numbers that cannot be parsed correctly
     * @throws EntityDoesNotExistException if the factory is unable to provide Entities with
     *         specified ids
     */
    public static <E extends Entity> Set<E> idsToEntitySet(String idString, EntityFactory<E> factory)
        throws NumberFormatException, EntityDoesNotExistException
    {
        if(idString.trim().length() > 0)
        {
            return idsToEntitySet(idString.split(","), factory);
        }
        else
        {
            return new HashSet<E>(0);
        }
    }
}
