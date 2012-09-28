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

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.DefaultConfiguration;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.tools.BatchLoader;
import org.objectledge.coral.tools.generator.model.ResourceClass;
import org.objectledge.coral.tools.generator.model.Schema;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.filesystem.FileSystemProvider;
import org.objectledge.filesystem.LocalFileSystemProvider;
import org.objectledge.filesystem.UnsupportedCharactersInFilePathException;
import org.objectledge.templating.Template;
import org.objectledge.templating.Templating;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.templating.velocity.VelocityTemplating;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.ThreadBuildContext;

/**
 * Performs wrapper generation.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GeneratorComponent.java,v 1.30 2005-02-21 15:44:54 zwierzem Exp $
 */
public class GeneratorComponent
{
    private static final String BUILD_STATE_ID = "org.objectledge.coral.tools.generator.GeneratorComponent";

    /** The Ledge file system. */
    private FileSystem fileSystem;

    /** The recognized hint names. */
    private static Set<String> hintNames = new HashSet<String>();

    static
    {
        hintNames.add("extends");
        hintNames.add("import");
        hintNames.add("order");
        hintNames.add("field");
    }

    /** The ignored hint names. */
    private static Set<String> ignoredHintNames = new HashSet<String>();

    static
    {
        // javadoc 1.4
        ignoredHintNames.add("author");
        ignoredHintNames.add("docRoot");
        ignoredHintNames.add("deprecated");
        ignoredHintNames.add("exception");
        ignoredHintNames.add("inheritDoc");
        ignoredHintNames.add("link");
        ignoredHintNames.add("linkplain");
        ignoredHintNames.add("param");
        ignoredHintNames.add("return");
        ignoredHintNames.add("see");
        ignoredHintNames.add("serial");
        ignoredHintNames.add("serialData");
        ignoredHintNames.add("serialField");
        ignoredHintNames.add("since");
        ignoredHintNames.add("throws");
        ignoredHintNames.add("value");
        ignoredHintNames.add("version");
        // todo
        ignoredHintNames.add("todo");
        ignoredHintNames.add("fixme");
        ignoredHintNames.add("xxx");
    }

    /** The character encoding to use for reading and writing files. */
    private String fileEncoding = "UTF-8";

    /** The path of source file list. */
    private String sourceFiles;

    /** The target directory. */
    private String targetDir;

    /** The target directory for SQL files. */
    private String sqlTargetDir;

    /** The path prefix of the SQL files in the classpath. */
    private String sqlTargetPrefix;

    /** Path to SQL info on AttributeClasses file. */
    private String sqlAttributeInfoFile;

    /** Path of the SQL list file to be generated. */
    private String sqlListPath;

    /** The RML loader. */
    private RMLModelLoader rmlLoader;

    /** The templating component. */
    private Templating templating;

    /** Interface template. */
    private Template interfaceTemplate;

    /** Implementation template. */
    private Template implementationTemplate;

    /** SQL script template. */
    private Template sqlTemplate;

    /** Package prefices used for grouping. */
    private List<String> importGroups = new ArrayList<String>();

    /** The file header contents. */
    private String header;

    /** PrintWriter for informational messages. */
    private Logger log;

    /** packages to generate wrappers in. */
    private List<String> packageIncludes;

    /** packages to not generate wrappers in. */
    private List<String> packageExcludes;

    /** custom fields defined by resource class. */
    private Map<ResourceClass, List<Map<String, String>>> fieldMap = new HashMap<ResourceClass, List<Map<String, String>>>();

    /** Java class to be used as the root of resource inheritance hierarchy. */
    private final String standardResourceImpl;

    /**
     * Creates new GeneratorComponent instance.
     * 
     * @param fileSystem the file system to operate on.
     * @param fileEncoding the character encoding to use for reading and writing files.
     * @param sourceFiles the path of source file list.
     * @param targetDir the target directory.
     * @param importGroups the comma separated list of package prefixes, for grouping.
     * @param packageIncludes packages to include in generation.
     * @param packageExcludes packages to exclude from generation
     * @param headerFile the path to the header file.
     * @param sqlAttributeInfoFile path of the SQL type information for attribute classes.
     * @param sqlTargetDir directory to write the SQL files to.
     * @param sqlTargetPrefix path prefix of the SQL files in the Ledge FS (for list file).
     * @param sqlListPath path of file listing all the generated SQL files.
     * @param standardResourceImpl the class to be used as the root of inheritance hierarchy.
     * @param logger TODO
     * @param out the PrintStream to write informational messages to.
     * @throws Exception if the component could not be initialized.
     */
    public GeneratorComponent(FileSystem fileSystem, Logger log, String fileEncoding,
        String sourceFiles, String targetDir, String importGroups, String packageIncludes,
        String packageExcludes, String headerFile, String sqlAttributeInfoFile,
        String sqlTargetDir, String sqlTargetPrefix, String sqlListPath, String standardResourceImpl)
        throws Exception
    {
        this(fileEncoding, sourceFiles, targetDir, importGroups, packageIncludes, packageExcludes,
                        headerFile, sqlAttributeInfoFile, sqlTargetDir, sqlTargetPrefix,
                        sqlListPath, standardResourceImpl, fileSystem, GeneratorComponent
                            .initTemplating(fileSystem, log), new RMLModelLoader(new Schema()), log);

    }

    /**
     * Creates new GeneratorComponent instance.
     * 
     * @param fileEncoding the character encoding to use for reading and writing files.
     * @param sourceFiles the path of source file list.
     * @param targetDir the target directory.
     * @param importGroups the comma separated list of package prefixes, for grouping.
     * @param packageIncludes packages to include in generation.
     * @param packageExcludes packages to exclude from generation
     * @param headerFile the path to the header file.
     * @param sqlAttributeInfoFile path of the SQL type information for attribute classes.
     * @param sqlTargetDir directory to write the SQL files to.
     * @param sqlTargetPrefix path prefix of the SQL files in the Ledge FS (for list file).
     * @param sqlListPath path of file listing all the generated SQL files.
     * @param standardResourceImpl the class to be used as the root of inheritance hierarchy.
     * @param fileSystem the file system to operate on.
     * @param templating the templating component.
     * @param rmlLoader the loader.
     * @param schema the schema.
     * @param out the PrintStream to write informational messages to.
     * @throws Exception if the component could not be initialized.
     */
    GeneratorComponent(String fileEncoding, String sourceFiles, String targetDir,
        String importGroups, String packageIncludes, String packageExcludes, String headerFile,
        String sqlAttributeInfoFile, String sqlTargetDir, String sqlTargetPrefix,
        String sqlListPath, String standardResourceImpl, FileSystem fileSystem,
        Templating templating, final RMLModelLoader rmlLoader, Logger log)
        throws Exception
    {
        this.fileSystem = fileSystem;
        this.templating = templating;
        this.rmlLoader = rmlLoader;
        this.fileEncoding = fileEncoding;
        this.sourceFiles = sourceFiles;
        this.targetDir = targetDir;
        this.importGroups = split(importGroups);
        this.packageIncludes = split(packageIncludes);
        this.packageExcludes = split(packageExcludes);
        this.sqlAttributeInfoFile = sqlAttributeInfoFile;
        this.sqlTargetDir = sqlTargetDir;
        this.sqlTargetPrefix = sqlTargetPrefix;
        this.sqlListPath = sqlListPath;
        this.log = log;
        this.standardResourceImpl = standardResourceImpl;

        if(fileSystem.exists(headerFile))
        {
            header = fileSystem.read(headerFile, fileEncoding);
        }
        else
        {
            header = "";
        }

        initTemplating();
    }

    public static FileSystem initFileSystem(String baseDir)
    {
        FileSystemProvider lfs = new org.objectledge.filesystem.LocalFileSystemProvider("local",
            baseDir);
        FileSystemProvider cfs = new org.objectledge.filesystem.ClasspathFileSystemProvider(
            "classpath", FileSystem.class.getClassLoader());
        // standard FS allows only 64k in memory files.
        FileSystem fileSystem = new FileSystem(new FileSystemProvider[] { lfs, cfs }, 4096, 250000);
        return fileSystem;
    }

    /**
     * Initializes the Velocity templating component.
     * 
     * @param logger the logger to use.
     * @return the intialized templating component.
     * @throws Exception if the templating component could not be initialized.
     */
    public static Templating initTemplating(FileSystem fileSystem, Logger logger)
        throws Exception
    {
        DefaultConfiguration config = new DefaultConfiguration("config", "<generated>", "");
        DefaultConfiguration configPaths = new DefaultConfiguration("paths", "<generated>",
            "config");
        DefaultConfiguration configPathsPath = new DefaultConfiguration("path", "<generated>",
            "config/paths");
        DefaultConfiguration configEncoding = new DefaultConfiguration("encoding", "<generated>",
            "config");
        configEncoding.setValue("UTF-8");
        configPathsPath.setValue("/");
        configPaths.addChild(configPathsPath);
        config.addChild(configPaths);
        config.addChild(configEncoding);
        return new VelocityTemplating(config, logger, fileSystem);
    }

    /**
     * Performs wrapper generation.
     * 
     * @throws Exception if the generation fails for some reason.
     */
    public void execute()
        throws Exception
    {
        if(rebuildIsNeeded(sourceFiles))
        {
            Set<String> referencedFiles = new HashSet<String>();
            loadSources(sourceFiles, referencedFiles);
            loadSQLInfo(sqlAttributeInfoFile);
            List<ResourceClass> resourceClasses = rmlLoader.getSchema().getResourceClasses();
            for(Iterator<ResourceClass> i = resourceClasses.iterator(); i.hasNext();)
            {
                ResourceClass rc = i.next();
                processClass(rc, referencedFiles);
            }
            String sqlList = generateSQLList();
            if(sqlList.length() > 0)
            {
                if(write(sqlListPath, sqlList, referencedFiles))
                {
                    log.info("writing generated SQL list to " + sqlListPath);
                }
                else
                {
                    log.debug("skipping generated SQL list (not modified)");
                }
            }
            saveBuildState(sourceFiles, referencedFiles);
        }
        else
        {
            log.info("no changes detected - skipping regeneration");
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
        interfaceTemplate = templating
            .getTemplate("org/objectledge/coral/tools/generator/Interface");
        implementationTemplate = templating
            .getTemplate("org/objectledge/coral/tools/generator/Implementation");
        sqlTemplate = templating.getTemplate("org/objectledge/coral/tools/generator/SQL");
    }

    /**
     * Loads custom code along with embedded hints from the specified file.
     * 
     * @param path the path of the file to process.
     * @param hints hint map.
     * @param referencedFiles TODO
     * @return the custom code found in the file.
     * @throws IOException if the file couldn't be read.
     */
    String read(String path, Map<String, List<String>> hints, Set<String> referencedFiles)
        throws IOException
    {
        if(!fileSystem.exists(path))
        {
            return "";
        }
        referencedFiles.add(path);

        LineNumberReader lnr = new LineNumberReader(fileSystem.getReader(path, fileEncoding));
        StringBuilder buff = new StringBuilder();
        int last = 0;
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
                last = buff.length();
            }
            if(inCustom)
            {
                if(in.indexOf('@') >= 0)
                {
                    int a = in.indexOf('@');
                    int b = in.indexOf(' ', a);
                    if(b < 0)
                    {
                        b = in.indexOf('}', b); // javadoc
                    }
                    if(b > 0)
                    {
                        String token = in.substring(a + 1, b);
                        if(hintNames.contains(token))
                        {
                            List<String> l = hints.get(token);
                            if(l == null)
                            {
                                l = new ArrayList<String>();
                                hints.put(token, l);
                            }
                            StringTokenizer st = new StringTokenizer(in.substring(b + 1), ",");
                            while(st.hasMoreElements())
                            {
                                l.add(st.nextToken().trim());
                            }
                        }
                        else if(!ignoredHintNames.contains(token))
                        {
                            throw new IOException("unknown hint @" + token + " in " + path
                                + " at line " + lnr.getLineNumber());
                        }
                    }
                    else
                    {
                        throw new IOException("malformed hint " + in.substring(a) + " in " + path
                            + " at line " + lnr.getLineNumber() + " - value expected");
                    }
                }
                buff.append(in).append('\n');
            }
        }
        buff.setLength(last);
        return buff.toString();
    }

    /**
     * Write the file contents to the disk.
     * <p>
     * If the file already contains specified contents, it is not modified.
     * </p>
     * 
     * @param path the path of the file to process.
     * @param contents the new contents of the file.
     * @param referencedFiles TODO
     * @return <code>true</code> if the file was actually modified.
     * @throws IOException
     * @throws UnsupportedCharactersInFilePathException
     */
    boolean write(String path, String contents, Set<String> referencedFiles)
        throws IOException, UnsupportedCharactersInFilePathException
    {
        if(!fileSystem.exists(path))
        {
            fileSystem.mkdirs(FileSystem.directoryPath(path));
        }
        else
        {
            if(contents.equals(fileSystem.read(path, fileEncoding)))
            {
                return false;
            }
        }
        referencedFiles.add(path);
        fileSystem.write(path, contents, fileEncoding);
        BuildContext buildContext = ThreadBuildContext.getContext();
        File fileLocation = ((LocalFileSystemProvider)fileSystem.getProvider("local"))
            .getFile(path);
        buildContext.refresh(fileLocation);
        return true;
    }

    void loadSources(String path, Set<String> referencedFiles)
        throws Exception
    {
        BatchLoader batchLoader = new BatchLoader(fileSystem, log, fileEncoding, referencedFiles)
            {
                @Override
                protected void load(Reader in)
                    throws Exception
                {
                    rmlLoader.load(in);
                }
            };
        batchLoader.loadBatch(path);
    }

    void loadSQLInfo(String path)
        throws Exception
    {
        if(!fileSystem.exists(path))
        {
            throw new Exception("missing attribute SQL info file " + path);
        }
        Reader reader = fileSystem.getReader(path, fileEncoding);
        try
        {
            AttributeSQLInfoLoader sqlInfoLoader = new AttributeSQLInfoLoader(rmlLoader.getSchema());
            sqlInfoLoader.load(reader);
        }
        catch(IOException e)
        {
            throw new Exception("failed to load atrribute SQL info file " + path, e);
        }
    }

    String classInterfacePath(ResourceClass rc)
    {
        return targetDir + "/" + rc.getFQInterfaceClassName().replace('.', '/') + ".java";
    }

    String classImplPath(ResourceClass rc)
    {
        return targetDir + "/" + rc.getFQImplClassName().replace('.', '/') + ".java";
    }

    String sqlPath(ResourceClass rc)
    {
        return sqlTargetDir + "/" + rc.getName().replace('.', '/') + ".sql";
    }

    String sqlAltPath(ResourceClass rc)
    {
        return sqlTargetPrefix + "/" + rc.getName().replace('.', '/') + ".sql";
    }

    List<String> split(String string)
    {
        if(string == null || string.length() == 0)
        {
            return Collections.emptyList();
        }
        StringTokenizer st = new StringTokenizer(string, ",");
        List<String> list = new ArrayList<String>(st.countTokens());
        while(st.hasMoreTokens())
        {
            list.add(st.nextToken());
        }
        return list;
    }

    boolean matches(String name, List<String> preficesList)
    {
        for(Iterator<String> i = preficesList.iterator(); i.hasNext();)
        {
            String prefix = i.next();
            if(prefix.charAt(prefix.length() - 1) == '*')
            {
                if(name.startsWith(prefix.substring(0, prefix.length() - 1)))
                {
                    return true;
                }
            }
            else
            {
                if(name.equals(prefix))
                {
                    return true;
                }
            }
        }
        return false;
    }

    void processClass(ResourceClass rc, Set<String> referencedFiles)
        throws Exception
    {
        if(rc.hasFlags(ResourceClassFlags.BUILTIN))
        {
            log.debug("skipping " + rc.getName() + " (BUILTIN)");
            return;
        }
        if(!matches(rc.getPackageName(), packageIncludes))
        {
            log.debug("skipping " + rc.getName() + " (package " + rc.getPackageName()
                + " not included)");
            return;
        }
        if(matches(rc.getPackageName(), packageExcludes))
        {
            log.debug("skipping " + rc.getName() + " (package " + rc.getPackageName()
                + " excluded)");
            return;
        }

        if(generateWrapper(rc, classInterfacePath(rc), interfaceTemplate, referencedFiles))
        {
            log.info("writing " + rc.getName() + " interface to " + classInterfacePath(rc));
        }
        else
        {
            log.debug("skipping " + rc.getName() + " interface (not modified)");
        }

        if(generateWrapper(rc, classImplPath(rc), implementationTemplate, referencedFiles))
        {
            log.info("writing " + rc.getName() + " implementation to " + classImplPath(rc));
        }
        else
        {
            log.debug("skipping " + rc.getName() + " implementation (not modified)");
        }

        if(rc.getDbTable() != null && rc.getDbTable().length() > 0)
        {
            if(generateSQL(rc, sqlPath(rc), sqlTemplate, referencedFiles))
            {
                log.info("writing " + rc.getName() + " SQL script to " + sqlPath(rc));
            }
            else
            {
                log.debug("skipping " + rc.getName() + " SQL script (not modified)");
            }
        }
    }

    String resolvePrimaryParentClass(ResourceClass rc)
    {
        return standardResourceImpl;
    }

    /**
     * @param rc
     * @param path
     * @param template
     * @param referencedFiles TODO
     * @throws Exception
     */
    boolean generateWrapper(ResourceClass rc, String path, Template template,
        Set<String> referencedFiles)
        throws Exception
    {
        TemplatingContext context = templating.createContext();
        Map<String, List<String>> hints = new HashMap<String, List<String>>();
        String custom = read(path, hints, referencedFiles);
        ImportTool imports = new ImportTool(rc.getPackageName(), importGroups);

        List<String> importHint = hints.get("import");
        if(importHint != null)
        {
            for(Iterator<String> i = importHint.iterator(); i.hasNext();)
            {
                imports.add(i.next());
            }
        }

        List<String> orderHint = hints.get("order");
        if(orderHint != null)
        {
            rc.setAttributeOrder(orderHint);
        }
        else
        {
            rc.setAttributeOrder(null);
        }

        processExtendsHint(rc, hints);

        List<Map<String, String>> fields = processFieldHint(rc, hints);

        List<Map<String, String>> superFields = new ArrayList<Map<String, String>>();
        if(rc.getImplParentClass() != null)
        {
            addSuperFields(rc.getImplParentClass(), superFields, referencedFiles);
            context.put("implParentClass", rc.getImplParentClass().getImplClassName());
        }
        else
        {
            String ppc = resolvePrimaryParentClass(rc);
            int dot = ppc.lastIndexOf('.');
            String unqPpc = ppc.substring(dot + 1);
            context.put("implParentClass", unqPpc);
            context.put("fqImplParentClass", ppc);
            context.put("primaryParentClass", Boolean.TRUE);
        }

        context.put("imports", imports);
        context.put("class", rc);
        context.put("header", header);
        context.put("custom", custom);
        context.put("fields", fields);
        context.put("superFields", superFields);
        context.put("string", new StringTool(100));

        String result = template.merge(context);
        return write(path, result, referencedFiles);
    }

    boolean generateSQL(ResourceClass rc, String path, Template template,
        Set<String> referencedFiles)
        throws Exception
    {
        TemplatingContext context = templating.createContext();
        context.put("class", rc);
        String result = template.merge(context);
        return write(path, result, referencedFiles);
    }

    String generateSQLList()
    {
        StringBuilder buff = new StringBuilder();
        List<ResourceClass> resourceClasses = rmlLoader.getSchema().getResourceClasses();
        for(Iterator<ResourceClass> i = resourceClasses.iterator(); i.hasNext();)
        {
            ResourceClass rc = i.next();
            if(rc.getDbTable() != null && rc.getDbTable().length() > 0)
            {
                buff.append(sqlAltPath(rc)).append("\n");
            }
        }
        return buff.toString();
    }

    /**
     * Processes field hint into name/type pair list.
     * 
     * @param rc the resource class.
     * @param hints global hint map.
     * @return list of maps with "name" and "type" keys.
     * @throws Exception if the hint list is malformed
     */
    private List<Map<String, String>> processFieldHint(ResourceClass rc,
        Map<String, List<String>> hints)
        throws Exception
    {
        List<String> fieldHint = hints.get("field");
        ArrayList<Map<String, String>> fields = new ArrayList<Map<String, String>>();
        if(fieldHint != null)
        {
            fields.ensureCapacity(fieldHint.size());
            for(int i = 0; i < fieldHint.size(); i++)
            {
                Map<String, String> entry = new HashMap<String, String>();
                StringTokenizer st = new StringTokenizer(fieldHint.get(i));
                if(st.countTokens() != 2)
                {
                    throw new Exception("malformed @field hints - "
                        + "expected @field <type> <name> pairs");
                }
                entry.put("type", st.nextToken().trim());
                entry.put("name", st.nextToken().trim());
                fields.add(entry);
            }
        }
        fieldMap.put(rc, fields);
        return fields;
    }

    /**
     * @param rc
     * @param hints
     * @throws Exception
     * @throws EntityDoesNotExistException
     */
    private void processExtendsHint(ResourceClass rc, Map<String, List<String>> hints)
        throws Exception, EntityDoesNotExistException
    {
        List<String> extendsHint = hints.get("extends");
        if(extendsHint != null)
        {
            if(extendsHint.size() != 1)
            {
                throw new Exception("malformed @extends hint - exactly one value expected");
            }
            ResourceClass implParentClass = rmlLoader.getSchema().getResourceClass(
                extendsHint.get(0));
            rc.setImplParentClass(implParentClass);
        }
        else
        {
            rc.setImplParentClass(null);
        }
    }

    private void addSuperFields(ResourceClass rc, List<Map<String, String>> fields,
        Set<String> referencedFiles)
        throws Exception
    {
        if(!fieldMap.containsKey(rc))
        {
            Map<String, List<String>> hints = new HashMap<String, List<String>>();
            read(classImplPath(rc), hints, referencedFiles);
            processExtendsHint(rc, hints);
            processFieldHint(rc, hints);
        }
        if(rc.getImplParentClass() != null)
        {
            addSuperFields(rc.getImplParentClass(), fields, referencedFiles);
        }
        List<Map<String, String>> superFields = fieldMap.get(rc);
        if(superFields != null)
        {
            fields.addAll(superFields);
        }
    }

    boolean rebuildIsNeeded(String sourceFiles)
    {
        BuildContext buildContext = ThreadBuildContext.getContext();
        if(buildContext.isIncremental())
        {
            String savedStateId = BUILD_STATE_ID + sourceFiles;
            Set<String> referencedFiles = (Set<String>)buildContext.getValue(savedStateId);
            for(String referncedFile : referencedFiles)
            {
                if(buildContext.hasDelta(new File(referncedFile)))
                {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    void saveBuildState(String sourceFiles, Set<String> referencedFiles)
    {
        if(log.isDebugEnabled())
        {
            StringBuilder buff = new StringBuilder();
            List<String> list = new ArrayList<String>(referencedFiles);
            Collections.sort(list);
            for(String path : referencedFiles)
            {
                buff.append(path).append("\n");
            }
            log.debug("saving build state, referenced files:");
        }
        BuildContext buildContext = ThreadBuildContext.getContext();
        String savedStateId = BUILD_STATE_ID + sourceFiles;
        buildContext.setValue(savedStateId, referencedFiles);
    }
}
