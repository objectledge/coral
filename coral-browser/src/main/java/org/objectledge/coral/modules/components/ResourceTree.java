package org.objectledge.coral.modules.components;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.table.CoralTableModel;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.web.mvc.MVCContext;

/**
 * The base browse component class.
 */
public class ResourceTree
    extends BaseBrowserComponent
{
    public ResourceTree(Logger logger, CoralSessionFactory sessionFactory,
                         TableStateManager tableStateManager)
    {
        super(logger, sessionFactory, tableStateManager);
    }
    
    public void process(Context context) throws ProcessingException
    {
        try
        {
            MVCContext mvcContext = MVCContext.getMVCContext(context);
            TableState state = tableStateManager.getState(context, "coral:components:resource_tree");
            if(state.isNew())
            {
                state.setTreeView(true);
                String rootId = Long.toString(1);
                state.setRootId(rootId);
                state.setCurrentPage(0);
                state.setShowRoot(true);
                state.setExpanded(rootId);
                // TODO: configure default
                state.setPageSize(0);
                state.setSortColumnName("name");
            }
            TableModel model = new CoralTableModel(coralSession, logger, mvcContext.getLocale());
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
