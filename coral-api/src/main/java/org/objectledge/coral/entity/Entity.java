package org.objectledge.coral.entity;

/**
 * A base interface of all Coral entities -- including {@link Subject}, {@link
 * Role}, {@link Permission} and {@link Resource}.
 *
 * <p> This interface exposes common characteristics of all these objects - a
 * numerical identifier that is guaranteed to be unique throughout the system,
 * and a textual name a name that is useful to humans. The names are not
 * required to be unique, but in certain situations, uniqueness of names is also
 * desired to avoid confusion. </p>
 *
 * @version $Id: Entity.java,v 1.1 2004-02-18 14:21:27 fil Exp $
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
     * Returns the name of the entity.
     *
     * @return the name of the entity.
     */
    public String getName();
}
