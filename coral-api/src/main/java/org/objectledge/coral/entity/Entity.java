package org.objectledge.coral.entity;

/**
 * A base interface of all Coral entities -- including {@link 
 * org.objectledge.coral.security.Subject}, {@link
 * org.objectledge.coral.security.Role}, {@link org.objectledge.coral.security.Permission} and 
 * {@link org.objectledge.coral.store.Resource}.
 *
 * <p> This interface exposes common characteristics of all these objects - a
 * numerical identifier that is guaranteed to be unique throughout the system,
 * and a textual name a name that is useful to humans. The names are not
 * required to be unique, but in certain situations, uniqueness of names is also
 * desired to avoid confusion. </p>
 *
 * @version $Id: Entity.java,v 1.3 2005-02-08 20:34:25 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface Entity
{
    /**
     * Returns the numerical identifier of the entity.
     * 
     * @return the numerical identifier of the entity.
     */
    public long getId();
    
    /**
     * Returns the numerical identifier of the entity as a Java object.
     * 
     * @return the numerical identifier of the entity as a Java object.
     */
    public Long getIdObject();

    /**
     * Returns the numerical identifier of the entity as a string.
     * 
     * @return the numerical identifier of the entity as a string.
     */
    public String getIdString();

    /**
     * Returns the name of the entity.
     *
     * @return the name of the entity.
     */
    public String getName();
}
