package org.objectledge.coral.modules.views;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.table.SubjectNameComparator;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.table.generic.ListTableModel;
import org.objectledge.web.mvc.MVCContext;

/**
 * The subject view screen.
 */
public class ChooseSubject extends BaseBrowserView
{
    public ChooseSubject(Context context, Logger logger, CoralSessionFactory sessionFactory,
                          TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }

    public void process(Context context) throws ProcessingException
    {
        try
        {
            MVCContext mvcContext = MVCContext.getMVCContext(context);
			TableColumn[] columns = new TableColumn[1];
			columns[0] = new TableColumn("name", new SubjectNameComparator(mvcContext.getLocale()));
            TableState state = tableStateManager.getState(context, "coral:screen:choose_subject");
            if (state.isNew())
            {
                state.setTreeView(false);
                state.setPageSize(0);
				state.setSortColumnName("name");
            }
            Subject[] subjects = coralSession.getSecurity().getSubject();
            TableModel model = new ListTableModel(subjects, columns);
            TableTool helper = new TableTool(state, model);
            templatingContext.put("table", helper);
        }
        catch(Exception e)
        {
            throw new ProcessingException("Cannot create TableTool", e);
        }
        finally
        {
            coralSession.close();
        }
    }

}
