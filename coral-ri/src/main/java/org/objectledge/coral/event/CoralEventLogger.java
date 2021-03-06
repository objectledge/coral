package org.objectledge.coral.event;

import org.jcontainer.dna.Logger;

/**
 * Logs incoming Coral events.
 *
 * @version $Id: CoralEventLogger.java,v 1.5 2005-02-21 15:48:08 zwierzem Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralEventLogger
    extends CoralEventListener
{
    // Instance variables ////////////////////////////////////////////////////////////////////////
    
    private Logger log;

    // Initialization ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Constructs an {@link CoralEventLogger} and registers it with the {@link
     * org.objectledge.event.EventWhiteboard}.
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
        StringBuilder buff = new StringBuilder();
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
