// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//  
// * Redistributions of source code must retain the above copyright notice,  
//	 this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//	 this list of conditions and the following disclaimer in the documentation  
//	 and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//	 nor the names of its contributors may be used to endorse or promote products  
//	 derived from this software without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
// OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
// POSSIBILITY OF SUCH DAMAGE. 
// 
package org.objectledge.coral.tools.generator;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.objectledge.coral.tools.generator.model.ResourceClass;
import org.objectledge.coral.tools.generator.model.Schema;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.templating.Template;
import org.objectledge.templating.Templating;
import org.objectledge.templating.TemplatingContext;

/**
 * Performs wrapper generation.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GeneratorComponent.java,v 1.3 2004-03-30 09:07:44 fil Exp $
 */
public class GeneratorComponent
{
    /** The Ledge file system. */
    private FileSystem fileSystem;

    /** The recognized hint names. */
    private static Set hintNames = new HashSet();
    
    static
    {
        hintNames.add("extends");
        hintNames.add("import");
        hintNames.add("order");
    }
    
    /** The character encoding to use for reading and writing files. */
    private String fileEncoding = "UTF-8";

    /** The path of source file list. */
    private String sourceFiles;
    
    /** The target directory. */
    private String targetDir;
    
    /** The schema. */
    private Schema schema;
    
    /** The RML loader. */
    private RMLModelLoader loader;
    
    /** The templating component. */
    private Templating templating;
    
    /** Interface template. */
    private Template interfaceTemplate;
    
    /** Implementation template. */
    private Template genericImplTemplate;
    
    /** Package prefices used for grouping. */
    private List packagePrefices = new ArrayList();
    
    /** License contents. */
    private String license;

    /**
     * Creates new GeneratorComponent instance.
     * 
     * @param fileEncoding the character encoding to use for reading and writing files.
     * @param sourceFiles the path of source file list.
     * @param targetDir the target directory.
     * @param packagePrefices the comma separated list of package prefixes, for grouping.
     * @param licensePath the path to the license.
     * @param fileSystem the file system to operate on.
     * @param templating the templating component.
     * @param schema the schema.
     * @param loader the loader.
     * @throws Exception if the component could not be initialized.
     */
    public GeneratorComponent(String fileEncoding, String sourceFiles, 
        String targetDir, String packagePrefices, String licensePath, FileSystem fileSystem,
        Templating templating, Schema schema, RMLModelLoader loader)
        throws Exception
    {
        this.fileSystem = fileSystem;
        this.templating = templating;
        this.schema = schema;
        this.loader = loader;
        
        this.fileEncoding = fileEncoding;
        this.sourceFiles = sourceFiles;
        this.targetDir = targetDir;
        
        StringTokenizer st = new StringTokenizer(packagePrefices, ",");
        while(st.hasMoreTokens())
        {
            this.packagePrefices.add(st.nextToken());
        }

        if(fileSystem.exists(licensePath))
        {
            license = fileSystem.read(licensePath, fileEncoding);
        }
        else
        {
            license = "";
        }
        
        initTemplating();
    }

    /**
     * Performs wrapper generation.
     * 
     * @throws Exception if the generation fails for some reason.
     */
    public void execute()
        throws Exception
    {
        loadSources(sourceFiles);
        List resourceClasses = schema.getResourceClasses();
        for(Iterator i = resourceClasses.iterator(); i.hasNext();)
        {
            ResourceClass rc = (ResourceClass)i.next();
            processClass(rc);
        }
    }
    
    // implementation ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Initializes the Velocity templating component.
     *
     * @throws Exception if the required templates are not available.
     */
    void initTemplating()
        throws Exception
    {
        interfaceTemplate = templating.
            getTemplate("org/objectledge/coral/tools/generator/Interface");
        genericImplTemplate = templating.
            getTemplate("org/objectledge/coral/tools/generator/GenericImpl");
    }
    
    /**
     * Loads custom code along with embedded hints from the specified file.
     * 
     * @param path the path of the file to process.
     * @param hints hint map.
     * @return the custom code found in the file.
     * @throws IOException if the file couldn't be read.
     */
    String read(String path, Map hints)
        throws IOException
    {
        if(!fileSystem.exists(path))
        {
            return "";
        }
        
        LineNumberReader lnr = new LineNumberReader(fileSystem.getReader(path, fileEncoding));
        StringBuffer out = new StringBuffer();
        int last=0;
        boolean inCustom = false;
        String in;
        while(true)
        {
            in = lnr.readLine();
            if(in == null)
            {
                break;
            }
            if(!inCustom && in.indexOf("@custom") > 0)
            {
                inCustom = true;
                continue;
            }
            if(inCustom && in.trim().equals("}"))
            {
                last = out.length();
            }
            if(inCustom)
            {
                if(in.indexOf('@') >=0 )
                {
                    int a = in.indexOf('@');
                    int b = in.indexOf(' ',a);
                    if(b > 0)
                    {
                        String token = in.substring(a+1, b);
                        if(hintNames.contains(token))
                        {
                            List l = (List)hints.get(token);
                            if(l == null)
                            {
                                l = new ArrayList();
                                hints.put(token, l);
                            }
                            StringTokenizer st = new StringTokenizer(in.substring(b+1), ",");
                            while(st.hasMoreElements())
                            {
                                l.add(st.nextToken().trim());
                            }
                        }
                    }
                }
                out.append(in).append('\n');
            }
        }
        out.setLength(last);
        return out.toString();        
    }
    
    /**
     * Write the file contents to the disk.
     * 
     * <p>If the file already contains specified contents, it is not modified.</p>
     * 
     * @param path the path of the file to process.
     * @param contents the new contents of the file.
     * @throws IOException if the file could not be written.
     */
    void write(String path, String contents)
        throws IOException
    {
        if(!fileSystem.exists(path))
        {
            fileSystem.mkdirs(FileSystem.directoryPath(path));
        }
        else
        {
            if(contents.equals(fileSystem.read(path, fileEncoding)))
            {
                return;
            }
        }
        fileSystem.write(path, contents, fileEncoding);
    }
    
    void loadSources(String path)
        throws Exception
    {
        LineNumberReader lnr = new LineNumberReader(fileSystem.
            getReader(path, fileEncoding));
        while(lnr.ready())
        {
            String line = lnr.readLine().trim();
            if(line.length() == 0 || line.charAt(0) == '#')
            {
                continue;
            }
            if(line.startsWith("@include "))
            {
                loadSources(line.substring(9));
            }
            loader.load(fileSystem.getReader(line, fileEncoding));
        }    
    }

    String classInterfacePath(ResourceClass rc)
    {
        return targetDir+"/"+rc.getFQInterfaceClassName().replace('.', '/')+".java";
    }

    String classImplPath(ResourceClass rc)
    {
        return targetDir+"/"+rc.getFQImplClassName().replace('.', '/')+".java";
    }

    void processClass(ResourceClass rc)
        throws Exception
    {
        generateWrapper(rc, classInterfacePath(rc), interfaceTemplate);
        // when resource metaclass support is added we'll have a bit of logic that selects
        // apropriate implementation here.
        generateWrapper(rc, classImplPath(rc), genericImplTemplate);
    }

    /**
     * @param rc
     * @param path
     * @param template
     * @throws Exception
     */
    void generateWrapper(ResourceClass rc, String path, Template template) 
        throws Exception
    {
        TemplatingContext context = templating.createContext();
        Map hints = new HashMap();
        String custom = read(path, hints);
        ImportTool imports = new ImportTool(rc.getPackageName(), packagePrefices);

        List importHint = (List)hints.get("import");
        if(importHint != null)
        {
            for(Iterator i = importHint.iterator(); i.hasNext();)
            {
                imports.add((String)i.next());
            }
        }

        List orderHint = (List)hints.get("order");
        if(orderHint != null)
        {
            rc.setAttributeOrder(orderHint);
        }
        else
        {
            rc.setAttributeOrder(null);
        }

        List extendsHint = (List)hints.get("extends");
        if(extendsHint != null)
        {
            if(extendsHint.size() != 1)
            {
                throw new Exception("malformed @extends hint - exactly one value expected");
            }
            ResourceClass implParentClass = schema.getResourceClass((String)extendsHint.get(0));
            rc.setImplParentClass(implParentClass);
        }
        else
        {
            rc.setImplParentClass(null);
        }

        context.put("imports", imports);
        context.put("class", rc);
        context.put("license", license);
        context.put("custom", custom);

        String result = template.merge(context);
        write(path, result);
    }
}
