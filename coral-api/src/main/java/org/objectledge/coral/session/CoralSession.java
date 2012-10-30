package org.objectledge.coral.session;

import java.security.Principal;

import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.CoralRelationQuery;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.script.CoralScript;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;

/**
 * An access point to the Coral document store.
 *
 * @version $Id: CoralSession.java,v 1.6 2005-02-08 20:34:30 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface CoralSession
    extends AutoCloseable
{
    /**
     * Returns an instance of {@link CoralSchema}.
     *
     * @return an instance of {@link CoralSchema}.
     */
    public CoralSchema getSchema();

    /**
     * Returns an instance of {@link CoralSecurity}.
     *
     * @return an instance of {@link CoralSecurity}.
     */
    public CoralSecurity getSecurity();
    
    /**
     * Returns an instance of {@link CoralStore}.
     *
     * @return an instance of {@link CoralStore}.
     */
    public CoralStore getStore();

    /**
     * Returns an instance of {@link CoralEventWhiteboard}.
     *
     * @return an instance of {@link CoralEventWhiteboard}.
     */
    public CoralEventWhiteboard getEvent();

    /**
     * Returns an instance of {@link CoralQuery}.
     *
     * @return an instance of {@link CoralQuery}.
     */
    public CoralQuery getQuery();

    /**
     * Returns a CoralRelationManager.
     *
     * @return a CoralRelationManager.
     */
    public CoralRelationManager getRelationManager();
    
    /**
     * Returns a CoralRelationQuery.
     *
     * @return a CoralRelationQuery.
     */
    public CoralRelationQuery getRelationQuery();

    /**
     * Returns a CoralScript front end.
     *
     * @return a CoralScript front end.
     */
    public CoralScript getScript();

    /**
     * Returns the user that owns this session.
     *
     * @return the user that owns this session.
     */
    public Principal getUserPrincipal();

    /**
     * Returns the user that owns this session.
     *
     * @return the user that owns this session.
     */
    public Subject getUserSubject();
    
    /**
     * Selects this session as the current session of the thread.
     * 
     * <p>You don't need to call this method unless you use multiple sessions interchangably in a 
     * single thread.</p>
     * 
     * @return the seesion that was previously active (possibly null).
     */
    public CoralSession makeCurrent();
    
    /**
     * Returns a Throwable created in the scope of open() method invocation.
     * 
     * <p>It is expected that the Throwables are created only when high logging verbosity is
     * enabled in the application. In all other cases this method will return null.</p>
     * 
     * @return a Throwable created in the scope of open() method invocation.
     */
    public Throwable getOpeningStackTrace();
    
    /**
     * Closes the session.
     */
    public void close();
}
