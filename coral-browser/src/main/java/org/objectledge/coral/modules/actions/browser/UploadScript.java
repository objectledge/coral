package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.upload.FileUpload;
import org.objectledge.upload.UploadContainer;
import org.objectledge.upload.UploadLimitExceededException;
import org.objectledge.web.mvc.MVCContext;

/**
 * Upload script.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: UploadScript.java,v 1.4 2005-03-14 13:19:26 zwierzem Exp $
 */
public class UploadScript extends BaseBrowserAction
{
	/** file upload */
	private FileUpload fileUpload;
	
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param fileUpload the file upload manager.
     */
    public UploadScript(Logger logger, FileUpload fileUpload)
    {
        super(logger);
    	this.fileUpload = fileUpload;        
    }

    /**
     * Runns the valve.
     *   
     * @param context the context.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException
    {
        UploadContainer script;
        try
        {
            script = fileUpload.getContainer("script");
        }
        catch(UploadLimitExceededException e)
        {
            // TODO Inform the user abour a problem in file upload
            throw new ProcessingException(e);
        }
        if(script != null)
        {
        	templatingContext.put("uploaded", new String(script.getBytes()));
        }
    }
}
