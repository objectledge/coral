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
 * 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: InitBean.java,v 1.3 2004-04-29 12:22:11 fil Exp $
 */
public class InitBean
{
    private DataSource dataSource;
    
    /**
     * Sets the dataSource
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
        FileSystem fs = FileSystem.getClasspathFileSystem();
        if(!DatabaseUtils.hasTable(dataSource, "ledge_id_table"))
        {
            DatabaseUtils.runScript(dataSource, 
                fs.getReader("sql/database/IdGenerator.sql", "UTF-8"));   
        }
        if(!DatabaseUtils.hasTable(dataSource, "ledge_parameters"))
        {
            DatabaseUtils.runScript(dataSource, 
                fs.getReader("sql/parameters/db/DBParameters.sql", "UTF-8"));   
        }
        if(!DatabaseUtils.hasTable(dataSource, "coral_resource_class"))
        {        
            DatabaseUtils.runScript(dataSource, 
                fs.getReader("sql/coral/CoralRITables.sql", "UTF-8"));   
            DatabaseUtils.runScript(dataSource, 
                fs.getReader("sql/coral/CoralDatatypesTables.sql", "UTF-8"));               
        }
        else
        {
            DatabaseUtils.runScript(dataSource, 
                fs.getReader("sql/coral/CoralDatatypesCleanup.sql", "UTF-8"));               
            DatabaseUtils.runScript(dataSource, 
                fs.getReader("sql/coral/CoralRICleanup.sql", "UTF-8"));   
        }
        DatabaseUtils.runScript(dataSource, 
            fs.getReader("sql/coral/CoralRIInitial.sql", "UTF-8"));   
        DatabaseUtils.runScript(dataSource, 
            fs.getReader("sql/coral/CoralDatatypesInitial.sql", "UTF-8"));        
    }
}
