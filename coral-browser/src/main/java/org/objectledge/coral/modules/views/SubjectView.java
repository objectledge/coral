package org.objectledge.coral.modules.views;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.security.Subject;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;

/**
 * The subject view screen.
 */
public class SubjectView extends BaseBrowserView
{
    public SubjectView(Logger logger, CoralSessionFactory sessionFactory, TableStateManager tableStateManager)
    {
        super(logger, sessionFactory, tableStateManager);
    }

    public void process(Context context) throws ProcessingException
    {
        try
        {
            long subjectId = parameters.getLong("sub_id",-1);
            if (subjectId != -1)
            {
                Subject subject = coralSession.getSecurity().getSubject(subjectId);
                templatingContext.put("subject", subject);
            }
        }
        catch(Exception e)
        {
            throw new ProcessingException("Subject not found", e);
        }
        finally
        {
            coralSession.close();
        }
    }
}
