// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral.touchstone;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.objectledge.container.LedgeContainer;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.IdGenerator;
import org.objectledge.database.ThreadDataSource;
import org.objectledge.filesystem.FileSystem;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralTestCase.java,v 1.8 2004-03-16 13:38:12 fil Exp $
 */
public abstract class CoralTestCase extends TestCase
{
    protected CoralSessionFactory coralSessionFactory;
    
    protected LedgeContainer container;
    
    protected IDatabaseConnection databaseConnection;
    
    protected Logger log;
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        FileSystem fs = FileSystem.getStandardFileSystem("src/test/resources");
        container = new LedgeContainer(fs, "/config", getClass().getClassLoader()); 
        coralSessionFactory = (CoralSessionFactory)container.getContainer().
            getComponentInstance(CoralSessionFactory.class);
        DataSource ds = (DataSource)container.getContainer().
            getComponentInstanceOfType(ThreadDataSource.class);
        if(!DatabaseUtils.hasTable(ds, "ledge_id_table"))
        {
            DatabaseUtils.runScript(ds, fs.getReader("sql/database/IdGenerator.sql", "UTF-8"));   
        }
        if(!DatabaseUtils.hasTable(ds, "ledge_parameters"))
        {
            DatabaseUtils.runScript(ds, fs.getReader("sql/parameters/db/DBParameters.sql", "UTF-8"));   
        }
        if(!DatabaseUtils.hasTable(ds, "coral_resource_class"))
        {        
            DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralRITables.sql", "UTF-8"));   
            DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralDatatypesTables.sql", "UTF-8"));               
        }
        else
        {
            DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralDatatypesCleanup.sql", "UTF-8"));               
            DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralRICleanup.sql", "UTF-8"));   
        }
        DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralRIInitial.sql", "UTF-8"));   
        DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralDatatypesInitial.sql", "UTF-8")); 
        IdGenerator idGenerator = (IdGenerator)container.getContainer().
            getComponentInstanceOfType(IdGenerator.class);
        idGenerator.getNextId("global_transaction_hack");
        databaseConnection = new DatabaseDataSourceConnection(ds);
        log = Logger.getLogger(getClass());
    }
    
    public void tearDown()
        throws Exception
    {
        container.killContainer();
    }
    
    // DbUnit assertions
    
    public void assertEquals(IDataSet expected, IDataSet actual)
        throws Exception
    {
        Assertion.assertEquals(expected, actual);
    }

    public void assertEquals(ITable expected, ITable actual)
        throws Exception
    {
        Assertion.assertEquals(expected, actual);
    }
}
