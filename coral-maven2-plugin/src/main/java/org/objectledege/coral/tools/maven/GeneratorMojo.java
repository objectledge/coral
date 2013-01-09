package org.objectledege.coral.tools.maven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.objectledge.coral.tools.generator.GeneratorComponent;
import org.objectledge.filesystem.FileSystem;

/**
 * Generates Java wrappers for a family of Coral resource classes.
 * 
 * @goal generator
 * @phase generate-sources
 */
public class GeneratorMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${project.basedir.canonicalPath}
     */
    private String baseDir;

    /**
     * @parameter expression="${coral.generator.file.encodnig}" default-value="UTF-8"
     */
    private String fileEncoding;

    /**
     * @parameter expression="${coral.generator.sources.list}
     *            default-value="src/main/resources/rml/sources.list"
     */
    private String sourcesList;

    /**
     * @parameter expression="${coral.generator.target.dir}" default-value="src/main/java"
     */
    private String targetDir;

    /**
     * @parameter expression="${coral.generator.import.groups}
     *            default-value="java.,javax.,org.objectledge."
     */
    private String importGroups;

    /**
     * @parameter expression="${coral.generator.package.includes}"
     *            default-value="${project.groupId},${project.groupId}.*"
     */
    private String packageIncludes;

    /**
     * @parameter expression="${coral.generator.package.excludes}" default-value=""
     */
    private String packageExcludes;

    /**
     * @parameter expression="${coral.generator.header.file}" default-value="LICENSE.txt"
     */
    private String headerFile;

    /**
     * @parameter
     */
    private boolean sqlGenerationEnabled = false;

    /**
     * @parameter expression="${coral.generator.sql.attributeInfo}
     *            default-value="sql/coral/CoralDatatypesAttributes.properties"
     */
    private String sqlAttributeInfoFile;

    /**
     * @parameter expression="${coral.generator.sql.targetDir}
     *            default-value="src/main/resources/sql"
     */
    private String sqlTargetDir;

    /**
     * @parameter expression="${coral.generator.sql.targetPrefix}" default-value="sql"
     */
    private String sqlTargetPrefix;

    /**
     * @parameter expression="${coral.generator.sql.list}"
     *            default-value="src/main/resources/sql/generated.list"
     */
    private String sqlListPath;

    /**
     * @parameter default-value="org.objectledge.coral.datatypes.StandardResource"
     */
    private String standardResourceImpl;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            FileSystem fileSystem = GeneratorComponent.initFileSystem(baseDir);
            MavenDNALogger log = new MavenDNALogger(getLog());
            GeneratorComponent generator = new GeneratorComponent(fileSystem, log, fileEncoding,
                sourcesList, targetDir, importGroups, packageIncludes, packageExcludes, headerFile,
                sqlGenerationEnabled, sqlAttributeInfoFile, sqlTargetDir, sqlTargetPrefix,
                sqlListPath, standardResourceImpl);
            generator.execute();
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("Exception while generating Coral wrappers", e);
        }
    }
}
