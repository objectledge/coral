package org.objectledge.coral.modules.views;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.table.CoralTableModel;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.web.mvc.MVCContext;

/**
 * The resource view screen.
 */
public class ChooseResource
    extends BaseBrowserView
{
    public ChooseResource(Context context, Logger logger, CoralSessionFactory sessionFactory,
                           TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }
    
    public void process(Context context) throws ProcessingException
    {
        try
        {
            MVCContext mvcContext = MVCContext.getMVCContext(context);
            String domain = parameters.get("domain","");
            if(domain.length()>0)
            {
                ResourceClass domainClass = coralSession.getSchema().getResourceClass(domain);
                templatingContext.put("domainClass", domainClass);
            }
            TableState state = tableStateManager.getState(context, "coral:screen:choose_resource");
            if(state.isNew())
            {
                state.setTreeView(true);
                //state.setMultiSelect(false);
                String rootId = Long.toString(1);
                state.setRootId(rootId);
                state.setCurrentPage(0);
                state.setShowRoot(true);
                state.setExpanded(rootId);
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
