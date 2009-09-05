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
package org.objectledge.coral.tools.sql;

import java.io.Reader;

import javax.sql.DataSource;

import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.coral.tools.BatchLoader;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.filesystem.FileSystem;

/**
 * A Bean that runns SQL scripts on a JDBC database.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SqlRunnerBean.java,v 1.2 2004-12-27 03:06:25 rafal Exp $
 */
public class SqlRunnerBean
{
    private String baseDir;
    
    private String sourcesList;
    
    private String fileEncoding;
    
    private DataSource dataSource;

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
     * Sets the dataSource.
     *
     * @param dataSource The dataSource to set.
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
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
     * Sets the sourcesList.
     *
     * @param sourcesList The sourcesList to set.
     */
    public void setSourcesList(String sourcesList)
    {
        this.sourcesList = sourcesList;
    }

    /**
     * Runns the RML scripts specified in the source list file. 
     * 
     * @throws Exception if the script processing fails.
     */
    public void run()
        throws Exception
    {
        FileSystem fileSystem = FileSystem.getStandardFileSystem(baseDir);
        Logger logger = new Log4JLogger(org.apache.log4j.Logger.getLogger(SqlRunnerBean.class));
        BatchLoader loader = new BatchLoader(fileSystem, logger, fileEncoding)
        {
            public void load(Reader in)
                throws Exception
            {
                DatabaseUtils.runScript(dataSource, in);
            }
        };
        loader.loadBatch(sourcesList);
    }    
}
