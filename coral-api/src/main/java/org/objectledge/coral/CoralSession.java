package org.objectledge.coral;

import java.security.Principal;

import org.objectledge.coral.event.CoralEventWiteboard;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;

/**
 * An access point to the Coral document store.
 *
 * @version $Id: CoralSession.java,v 1.1 2004-02-18 14:21:27 fil Exp $
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
    public CoralEventWiteboard getEvent();

    /**
     * Returns an instance of {@link CoralQuery}.
     *
     * @return an instance of {@link CoralQuery}.
     */
    public CoralQuery getQuery();

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
     * Closes the session.
     */
    public void close();
}
