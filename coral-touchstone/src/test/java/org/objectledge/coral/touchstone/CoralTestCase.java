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
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.container.LedgeContainer;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.tools.init.InitComponent;
import org.objectledge.database.IdGenerator;
import org.objectledge.database.ThreadDataSource;
import org.objectledge.filesystem.FileSystem;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralTestCase.java,v 1.9 2005-02-16 20:01:01 rafal Exp $
 */
public abstract class CoralTestCase extends TestCase
{
    protected static Column col(String name, DataType type)
    {
        return new Column(name, type, Column.NULLABLE);
    }

    protected static Column nnCol(String name, DataType type)
    {
        return new Column(name, type, Column.NO_NULLS);
    }

    protected CoralSessionFactory coralSessionFactory;
    
    protected LedgeContainer container;
    
    protected IDatabaseConnection databaseConnection;

    protected DefaultTable expected;

    protected ITable actual;
    
    protected Logger log;

    protected DataSource dataSource;
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        FileSystem fs = FileSystem.getStandardFileSystem("src/test/resources");
        container = new LedgeContainer(fs, "/config", getClass().getClassLoader()); 
        coralSessionFactory = (CoralSessionFactory)container.getContainer().
            getComponentInstance(CoralSessionFactory.class);
        dataSource = (DataSource)container.getContainer().
            getComponentInstanceOfType(ThreadDataSource.class);
        log = Logger.getLogger(getClass());

        InitComponent init = new InitComponent(dataSource, fs, true, new Log4JLogger(log));
        init.run();

        IdGenerator idGenerator = (IdGenerator)container.getContainer().
            getComponentInstanceOfType(IdGenerator.class);
        idGenerator.getNextId("global_transaction_hack");
        databaseConnection = new DatabaseDataSourceConnection(dataSource);
    }
    
    public void tearDown()
        throws Exception
    {
        container.killContainer();
    }
    
    // DbUnit assertions
    
    protected void expTable(String table, Column... columns)
    {
        expected = new DefaultTable(table, columns);
    }

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

    protected void expRow(Object... values)
        throws DataSetException
    {
        expected.addRow(values);
    }

    protected void assertExpTable()
        throws Exception
    {
        String table = expected.getTableMetaData().getTableName();
        actual = databaseConnection.createQueryTable(table, "SELECT * FROM " + table);
        try
        {
            assertEquals(expected, actual);
        }
        finally
        {
            databaseConnection.close();
        }
    }
}
