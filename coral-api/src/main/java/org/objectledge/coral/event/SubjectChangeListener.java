package org.objectledge.coral.event;

import org.objectledge.coral.security.Subject;

/**
 * Is notified of changes to a <code>Subject</code> data.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: SubjectChangeListener.java,v 1.3 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface SubjectChangeListener
{
    /**
     * Called when <code>Subject</code>'s data change.
     *
     * @param subject the subject that changed.
     */
    public void subjectChanged(Subject subject);
}
