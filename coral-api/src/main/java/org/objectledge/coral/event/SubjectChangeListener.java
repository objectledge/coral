package org.objectledge.coral.event;

import org.objectledge.coral.security.Subject;

/**
 * Is notified of changes to a <code>Subject</code> data.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: SubjectChangeListener.java,v 1.2 2004-02-18 15:08:21 fil Exp $
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
