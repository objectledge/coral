package org.objectledge.coral.tools.extract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.templating.MergingException;
import org.objectledge.templating.Templating;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.templating.velocity.VelocityTemplating;

/**
 * Extracts files from a given directory in Ledge filesystem to disk, performing optional template
 * rendering.
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class FileExtractionComponent
{
    private final FileSystem fileSystem;

    public FileExtractionComponent(FileSystem fileSystem)
    {
        this.fileSystem = fileSystem;
    }

    public void run(String source, File target, String fileEncoding,
        Map<String, Object> templateVars, List<String> templateMacroLibraries)
        throws ConfigurationException, IOException, MergingException
    {
        org.apache.log4j.Logger templatingLog = org.apache.log4j.Logger
            .getLogger("org.apache.velocity");
        templatingLog.setLevel(Level.ERROR);
        Templating templating = new VelocityTemplating(new VelocityTemplating.Config()
            .withEncoding(fileEncoding).withLibraries(templateMacroLibraries), new Log4JLogger(
            templatingLog), fileSystem);
        TemplatingContext context = templating.createContext(templateVars);

        if(fileSystem.exists(source) && fileSystem.isDirectory(source))
        {
            extractDir(source, target, templating, context);
        }
        else
        {
            throw new IOException(source + " does not exits or is not a directory");
        }
    }

    private void extractDir(String source, File target, Templating templating,
        TemplatingContext context)
        throws IOException, MergingException
    {
        for(String name : fileSystem.list(source))
        {
            final String source2 = source + "/" + name;
            final File target2 = new File(target, name);
            if(fileSystem.isDirectory(source2))
            {
                target2.mkdir();
                extractDir(source2, target2, templating, context);
            }
            else
            {
                extractFile(source2, target2, templating, context);
            }
        }
    }

    private void extractFile(String source, File target, Templating templating,
        TemplatingContext context)
        throws IOException, MergingException
    {

        if(source.endsWith(templating.getTemplateExtension()))
        {
            String targetFileName = target.getName().substring(0,
                target.getName().length() - templating.getTemplateExtension().length());
            target = new File(target.getParentFile(), targetFileName);
            try(OutputStream os = new BufferedOutputStream(new FileOutputStream(target)))
            {
                try(Writer w = new OutputStreamWriter(os, templating.getTemplateEncoding()))
                {
                    templating.merge(context,
                        fileSystem.getReader(source, templating.getTemplateEncoding()), w, source);
                }
            }
        }
        else
        {
            try(OutputStream os = new BufferedOutputStream(new FileOutputStream(target)))
            {
                fileSystem.read(source, os);
            }
        }
    }

}
