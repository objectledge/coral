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
package org.objectledge.coral.tools;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.jcontainer.dna.Logger;
import org.objectledge.filesystem.FileSystem;

/**
 * A component for loading a batch of sourcefiles specified by a list file.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski </a>
 * @version $Id: BatchLoader.java,v 1.4 2004-05-07 11:16:16 fil Exp $
 */
public abstract class BatchLoader
{
    private final FileSystem fileSystem;
    
    private final String fileEncoding;

    private final Logger logger;
    
    private final Set<String> referencedFiles;

    /**
     * Creates new BatchLoader instance.
     * 
     * @param fileSystem the file system to load file contents from.
     * @param fileEncoding the encoding of the source files.
     */
    public BatchLoader(FileSystem fileSystem, Logger logger, String fileEncoding)
    {
        this(fileSystem, logger, fileEncoding, new HashSet<String>());
    }
    
    /**
     * Creates new BatchLoader instance.
     * 
     * @param fileSystem the file system to load file contents from.
     * @param fileEncoding the encoding of the source files.
     * @param referencedFiles pathnames of all loaded listing and source files will
     *        be added to this list.
     */
    public BatchLoader(FileSystem fileSystem, Logger logger, String fileEncoding,
        Set<String> referencedFiles)
    {
        this.fileSystem = fileSystem;
        this.logger = logger;
        this.fileEncoding = fileEncoding;
        this.referencedFiles = referencedFiles;
    }

    /**
     * Loads all source files specified in a batch file.
     * 
     * @param path the path of the batch file.
     * @throws Exception if the batch file is missing or invalid, or if any of the source files
     * are missing or invalid.
     */
    public void loadBatch(String path) 
        throws Exception
    {
        if(!fileSystem.exists(path))
        {
            throw new IOException("missing listing file " + path);
        }
        logger.info("processing "+path);
        referencedFiles.add(path);
        LineNumberReader lnr = new LineNumberReader(fileSystem.getReader(path, fileEncoding));
        while(lnr.ready())
        {
            String line = lnr.readLine().trim();
            if(line.length() == 0 || line.charAt(0) == '#')
            {
                continue;
            }
            if(line.startsWith("@include "))
            {
                String included = line.substring(9);
                if(!fileSystem.exists(included))
                {
                    throw new IOException("missing include file " + included + " in " + path
                        + " at line " + lnr.getLineNumber());
                }
                loadBatch(included);
                continue;
            }
            if(!fileSystem.exists(line))
            {
                throw new IOException("missing source file " + line + " in " + path + " at line "
                    + lnr.getLineNumber());
            }
            try
            {
                logger.debug("loading "+line);
                referencedFiles.add(line);
                load(fileSystem.getReader(line, fileEncoding));
            }
            catch(Exception e)
            {
                logger.error("failed to load source file " + line, e);
                throw new Exception("failed to load source file " + line, e);
            }
        }
    }

    /**
     * Loads a single source file.
     * 
     * @param reader the reader to read file contents from.
     * @throws Exception if the file is invalid.
     */
    protected abstract void load(Reader reader)
        throws Exception;
}