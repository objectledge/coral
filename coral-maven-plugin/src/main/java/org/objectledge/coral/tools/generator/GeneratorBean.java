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

import org.objectledge.filesystem.FileSystem;


/**
 * An interface between GeneratorComponent and Maven.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GeneratorBean.java,v 1.11 2005-02-10 17:48:48 rafal Exp $
 */
public class GeneratorBean
{
    private String baseDir;
    private String fileEncoding;
    private String sourceFiles;
    private String targetDir;
    private String importGroups;
    private String packageIncludes;
    private String packageExcludes;
    private String headerFile;
    private String sqlTargetDir;
    private String sqlTargetPrefix;
    private String sqlAttributeInfoFile;
    private String sqlListPath;
    
    /**
     * Performs wrapper generation.
     * 
     * @throws Exception if the generation failed for some reason.
     */
    public void run()
        throws Exception
    {
        FileSystem fileSystem = GeneratorComponent.initFileSystem(baseDir);
        GeneratorComponent generator = new GeneratorComponent(fileSystem, fileEncoding,
            sourceFiles, targetDir, importGroups, packageIncludes, packageExcludes, headerFile,
            sqlAttributeInfoFile, sqlTargetDir, sqlTargetPrefix, sqlListPath, System.out);
        generator.execute();
    }

    /**
     * Sets the baseDir.
     *
     * @param baseDir The baseDir to set.
     */
    public void setBaseDir(String baseDir)
    {
        this.baseDir = baseDir;
    }
    
    /**
     * Sets the fileEncoding.
     *
     * @param fileEncoding The fileEncoding to set.
     */
    public void setFileEncoding(String fileEncoding)
    {
        this.fileEncoding = fileEncoding;
    }
    
    /**
     * Sets the importGroups.
     *
     * @param importGroups The packagePrefices to set.
     */
    public void setImportGroups(String importGroups)
    {
        this.importGroups = importGroups;
    }    

    /**
     * Sets the packageExcludes.
     *
     * @param packageExcludes The packageExcludes to set.
     */
    public void setPackageExcludes(String packageExcludes)
    {
        this.packageExcludes = packageExcludes;
    }

    /**
     * Sets the packageIncludes.
     *
     * @param packageIncludes The packageIncludes to set.
     */
    public void setPackageIncludes(String packageIncludes)
    {
        this.packageIncludes = packageIncludes;
    }
    
    /**
     * Sets the header file path.
     *
     * @param headerFile The headerFile to set.
     */
    public void setHeaderFile(String headerFile)
    {
        this.headerFile = headerFile;
    }

    /**
     * Sets the sourceFiles.
     *
     * @param sourceFiles The sourceFiles to set.
     */
    public void setSourceFiles(String sourceFiles)
    {
        this.sourceFiles = sourceFiles;
    }
    
    /**
     * Sets the targetDir.
     *
     * @param targetDir The targetDir to set.
     */
    public void setTargetDir(String targetDir)
    {
        this.targetDir = targetDir;
    }

    
	/**
     * Sets sqlInfoFile attribute.
     * 
	 * @param sqlAttributeInfoFile The sqlAttributeInfoFile to set.
	 */
	public void setSqlAttributeInfoFile(String sqlAttributeInfoFile) 
	{
		this.sqlAttributeInfoFile = sqlAttributeInfoFile;
	}
	
	/**
     * Sets sqlListPath attribute.
     * 
	 * @param sqlListPath The sqlListPath to set.
	 */
	public void setSqlListPath(String sqlListPath) 
	{
		this.sqlListPath = sqlListPath;
	}
	
	/**
     * Sets sqlTargetDir attribute.
     * 
	 * @param sqlTargetDir The sqlTargetDir to set.
	 */
	public void setSqlTargetDir(String sqlTargetDir) 
	{
		this.sqlTargetDir = sqlTargetDir;
	}
	
	/**
     * Sets sqlTargetPrefix attribute.
     * 
	 * @param sqlTargetPrefix The sqlTargetPrefix to set.
	 */
	public void setSqlTargetPrefix(String sqlTargetPrefix) 
	{
		this.sqlTargetPrefix = sqlTargetPrefix;
	}
}
