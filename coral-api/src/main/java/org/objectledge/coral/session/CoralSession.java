package org.objectledge.coral.session;

import java.security.Principal;

import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.relation.CoralRelationManager;
import org.objectledge.coral.relation.CoralRelationQuery;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;

/**
 * An access point to the Coral document store.
 *
 * @version $Id: CoralSession.java,v 1.2 2004-03-16 14:16:17 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface CoralSession
{
    /**
     * Returns an instance of {@link SchemaService}.
     *
     * @return an instance of {@link SchemaService}.
     */
    public CoralSchema getSchema();

    /**
     * Returns an instance of {@link SecurityService}.
     *
     * @return an instance of {@link SecurityService}.
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
     * Executes an RML Script.
     *
     * @param script the script contents.
     * @return execution results report.
     */
    public String executeScript(String script);

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
     */
    public void makeCurrent();
    
    /**
     * Closes the session.
     */
    public void close();
}
