package org.objectledge.coral.event;

import org.jcontainer.dna.Logger;

/**
 * Logs incoming Coral events.
 *
 * @version $Id: CoralEventLogger.java,v 1.2 2004-02-27 12:41:03 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralEventLogger
    extends CoralEventListener
{
    // Instance variables ////////////////////////////////////////////////////////////////////////
    
    private Logger log;

    // Initialization ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Constructs an {@link ARLEventLogger} and registers it with the {@link
     * EventService}.
     *
     * @param coralEventHub the event hub.
     * @param log the logger.
     */
    CoralEventLogger(CoralEventHub coralEventHub, Logger log)
    {
        this.log = log;
        register(coralEventHub.getGlobal());
    }
    
    // CoralEventListener implementation /////////////////////////////////////////////////////////

    /**
     * The event processing method to be implemented by concrete multiplexer
     * classess. 
     *
     * @param type the type of the event (same as the name of the interface
     *        with 'Listener' suffix removed).
     * @param entity1 the identifier of the first entity involved.
     * @param entity2 the identifier of the second entity involved or
     *        <code>1</code>.
     * @param entity3 the identifier of the third entity involved or
     *        <code>-1</code>
     * @param added the 'added' argument of the event methods
     */
    protected void event(String type, long entity1, long entity2,
                                  long entity3, boolean added)
    {
        StringBuffer buff = new StringBuffer();
        buff.append("Coral event: ");
        buff.append(type);
        buff.append("(#");
        buff.append(entity1);
        if(entity2 != -1L)
        {
            buff.append(", #");
            buff.append(entity2);
            if(entity3 != -1)
            {
                buff.append(", #");
                buff.append(entity3);
            }
        }
        buff.append(added ? ", true)" : ", false)");
        log.info(buff.toString());
    }    
}
