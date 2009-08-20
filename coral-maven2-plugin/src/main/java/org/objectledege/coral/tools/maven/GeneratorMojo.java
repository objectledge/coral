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
import org.apache.maven.plugin.logging.Log;
import org.jcontainer.dna.Logger;
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

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            FileSystem fileSystem = GeneratorComponent.initFileSystem(baseDir);
            GeneratorComponent generator = new GeneratorComponent(fileSystem, new MavenDNALogger(
                getLog()), fileEncoding, sourcesList, targetDir, importGroups, packageIncludes,
                packageExcludes, headerFile, sqlAttributeInfoFile, sqlTargetDir, sqlTargetPrefix,
                sqlListPath, System.out);
            generator.execute();
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("Exception while generating Coral wrappers", e);
        }
    }

    private static class MavenDNALogger
        implements Logger
    {
        private final Log log;

        public MavenDNALogger(Log log)
        {
            this.log = log;
        }

        public boolean isErrorEnabled()
        {
            return log.isErrorEnabled();
        }

        public void error(String msg)
        {
            log.error(msg);
        }

        public void error(String msg, Throwable e)
        {
            log.error(msg, e);
        }

        public boolean isWarnEnabled()
        {
            return log.isWarnEnabled();
        }

        public void warn(String msg)
        {
            log.warn(msg);
        }

        public void warn(String msg, Throwable e)
        {
            log.warn(msg, e);
        }

        public boolean isInfoEnabled()
        {
            return log.isInfoEnabled();
        }

        public void info(String msg)
        {
            log.info(msg);
        }

        public void info(String msg, Throwable e)
        {
            log.info(msg, e);
        }

        public boolean isDebugEnabled()
        {
            return log.isDebugEnabled();
        }

        public void debug(String msg)
        {
            log.debug(msg);
        }

        public void debug(String msg, Throwable e)
        {
            log.debug(msg, e);
        }

        public boolean isTraceEnabled()
        {
            return log.isDebugEnabled();
        }

        public void trace(String msg)
        {
            log.debug(msg);
        }

        public void trace(String msg, Throwable e)
        {
            log.debug(msg, e);
        }

        public Logger getChildLogger(String arg0)
        {
            throw new UnsupportedOperationException();
        }
    }
}
