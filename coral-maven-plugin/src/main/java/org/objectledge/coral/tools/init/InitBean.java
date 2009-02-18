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
package org.objectledge.coral.tools.init;

import javax.sql.DataSource;

import org.objectledge.database.DatabaseUtils;
import org.objectledge.filesystem.FileSystem;

/**
 * A Bean that intializes a JDBC database for use as Coral data backend.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: InitBean.java,v 1.7 2005-02-16 20:33:13 rafal Exp $
 */
public class InitBean
{
    private DataSource dataSource;
    
    private final FileSystem fs = FileSystem.getClasspathFileSystem(); 
    
    /**
     * Sets the dataSource.
     * 
     * @param dataSource the dataSource.
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
    
    /**
     * Initializes the database for coral use.
     * 
     * @throws Exception if the initialization failed.
     */
    public void run()
        throws Exception
    {
        if(!hasTable("ledge_id_table"))
        {
            runScript("sql/database/IdGeneratorTables.sql");
        }
        else
        {
            runScript("sql/database/IdGeneratorCleanup.sql");
        }
        
        if(!hasTable("ledge_parameters"))
        {
            runScript("sql/parameters/db/DBParametersTables.sql");
        }
        else
        {
            runScript("sql/parameters/db/DBParametersCleanup.sql");
        }
        
        if(!hasTable("ledge_scheduler"))
        {
            runScript("sql/scheduler/db/DBSchedulerTables.sql");
        }
        else
        {
            runScript("sql/scheduler/db/DBSchedulerCleanup.sql");
        }
        
        if(!hasTable("ledge_naming_attribute"))
        {
            runScript("sql/naming/db/DBNamingTables.sql");
        }
        else
        {
            runScript("sql/naming/db/DBNamingCleanup.sql");
        }
        
        if(!hasTable("coral_resource_class"))
        {
            runScript("sql/coral/CoralRITables.sql");
            runScript("sql/coral/CoralDatatypesTables.sql");
        }
        else
        {
            runScript("sql/coral/CoralDatatypesCleanup.sql");
            runScript("sql/coral/CoralRICleanup.sql");
        }
        runScript("sql/coral/CoralRIInitial.sql");
        runScript("sql/coral/CoralDatatypesInitial.sql");
    }
    
    private boolean hasTable(String table)
        throws Exception
    {
        return DatabaseUtils.hasTable(dataSource, table);
    }
    
    private void runScript(String path)
        throws Exception
    {
        DatabaseUtils.runScript(dataSource, fs.getReader(path, "UTF-8"));        
    }
}
