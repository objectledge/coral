package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.upload.FileUpload;
import org.objectledge.upload.UploadContainer;

/**
 * Upload script.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: UploadScript.java,v 1.1 2004-06-16 12:51:56 pablo Exp $
 */
public class UploadScript extends BaseBrowserAction
{
	/** file upload */
	private FileUpload fileUpload;
	
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     * @param fileUpload the file upload manager.
     */
    public UploadScript(Logger logger, CoralSessionFactory coralSessionFactory, FileUpload fileUpload)
    {
        super(logger, coralSessionFactory);
    	this.fileUpload = fileUpload;        
    }

    /**
     * Runns the valve.
     *   
     * @param context the context.
     */
    public void process(Context context) throws ProcessingException
    {
        UploadContainer script = fileUpload.getContainer("script");
        if(script != null)
        {
        	TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
        	templatingContext.put("uploaded", new String(script.getBytes()));
        }
    }
}
