//
// Copyright (c) 2003, 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.
// nor the names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
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
package org.objectledge.coral.relation;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationImplTest.java,v 1.1 2004-03-10 16:31:53 zwierzem Exp $
 */
public class RelationImplTest extends MockObjectTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockPersistence;
    private Persistence persistence;

    private CoralStore coralStore;

    private Mock mockInputRecord;
    private InputRecord inputRecord;
    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private ResultSet resultSet;

    private RelationImpl relation;

	private Mock mockResource1 = new Mock(Resource.class);
	private Resource resource1 = (Resource) mockResource1.proxy();
	private Mock mockResource2 = new Mock(Resource.class);
	private Resource resource2 = (Resource) mockResource2.proxy();

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RelationImplTest.class);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        coralStore = new MockCoralStore();

        mockInputRecord = new Mock(InputRecord.class);
        mockInputRecord.stub().method("getLong").will(returnValue(10L));
        mockInputRecord.stub().method("getString").will(returnValue("relation name2"));
        inputRecord = (InputRecord)mockInputRecord.proxy();

        mockStatement = new Mock(Statement.class);
        statement = (Statement)mockStatement.proxy();

        mockConnection = new Mock(Connection.class);
        mockConnection.stub().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();

        mockDatabase = new Mock(Database.class);
        mockDatabase.stub().method("getNextId").will(returnValue(1L));
        mockDatabase.stub().method("getConnection").will(returnValue(connection));
        database = (Database)mockDatabase.proxy();

        resultSet = new RelationContentsResultSet(); 

        mockStatement.stub().method("executeQuery").will(returnValue(resultSet));

        relation = new RelationImpl(persistence, coralStore, database, "relation name");
    }


    public void testRelationImpl()
    {
        assertEquals(relation.getId(), -1L);
        assertEquals(relation.getName(), "relation name");

        try
        {
            relation.setData(inputRecord);
        }
        catch (PersistenceException e)
        {
            fail("Peristence exception occured");
        }

		assertEquals(relation.getId(), 10L);
		assertEquals(relation.getName(), "relation name2");

		assertFalse(relation.isInverted());
		
		Set relatedTo1 = relation.get(1L);
		assertEquals(relatedTo1.size(), 3);
		assertTrue(relatedTo1.contains(new Long(4L)));
		assertTrue(relatedTo1.contains(new Long(5L)));
		assertTrue(relatedTo1.contains(new Long(6L)));

		Set relatedTo100 = relation.get(100L);
		assertEquals(relatedTo100.size(), 0);

		assertTrue(relation.hasRef(1L, 4L));
		assertTrue(relation.hasRef(1L, 5L));
		assertTrue(relation.hasRef(1L, 6L));
		assertTrue(relation.hasRef(2L, 5L));
		assertTrue(relation.hasRef(2L, 6L));
		assertTrue(relation.hasRef(3L, 6L));
		
		assertFalse(relation.hasRef(1L, 1L));
		assertFalse(relation.hasRef(8L, 1L));
		
		assertEquals("avg mapping size", relation.getAvgMappingSize(), 2F, 0.5F);

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(1L));
		Resource[] resRelatedTo1 = relation.get(resource1);
		assertEquals(resRelatedTo1.length, 3);
		if(resRelatedTo1[0].getId() == 4L)
		{
			if(resRelatedTo1[1].getId() == 5L) assertEquals(resRelatedTo1[2].getId(), 6L);
			if(resRelatedTo1[2].getId() == 5L) assertEquals(resRelatedTo1[1].getId(), 6L);
		} 
		if(resRelatedTo1[0].getId() == 5L)
		{
			if(resRelatedTo1[1].getId() == 4L) assertEquals(resRelatedTo1[2].getId(), 6L);
			if(resRelatedTo1[2].getId() == 4L) assertEquals(resRelatedTo1[1].getId(), 6L);
		} 
		if(resRelatedTo1[0].getId() == 6L)
		{
			if(resRelatedTo1[1].getId() == 4L) assertEquals(resRelatedTo1[2].getId(), 5L);
			if(resRelatedTo1[2].getId() == 4L) assertEquals(resRelatedTo1[1].getId(), 5L);
		} 

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(100L));
		Resource[] resRelatedTo100 = relation.get(resource1);
		assertEquals(resRelatedTo100.length, 0);

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(1L));
		mockResource2.expect(atLeastOnce()).method("getId").will(returnValue(4L));
		assertTrue(relation.hasRef(resource1, resource2));

		assertFalse(relation.hasRef(resource1, resource1));
		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(8L));
		assertFalse(relation.hasRef(resource1, resource1));

		// inverted
		Relation invRelation = relation.getInverted();

		assertEquals(relation.getInverted(), invRelation);
		assertEquals(invRelation.getInverted(), relation);

		assertEquals(invRelation.getId(), 10L);
		assertEquals(invRelation.getName(), "relation name2");

		assertTrue(invRelation.isInverted());

		Set relatedTo6 = invRelation.get(6L);
		assertEquals(relatedTo6.size(), 3);
		assertTrue(relatedTo6.contains(new Long(1L)));
		assertTrue(relatedTo6.contains(new Long(2L)));
		assertTrue(relatedTo6.contains(new Long(3L)));

		relatedTo100 = invRelation.get(100L);
		assertEquals(relatedTo100.size(), 0);

		assertTrue(invRelation.hasRef(6L, 1L));
		assertTrue(invRelation.hasRef(6L, 2L));
		assertTrue(invRelation.hasRef(6L, 3L));
		assertTrue(invRelation.hasRef(5L, 1L));
		assertTrue(invRelation.hasRef(5L, 2L));
		assertTrue(invRelation.hasRef(4L, 1L));

		assertFalse(invRelation.hasRef(6L, 6L));
		assertFalse(invRelation.hasRef(1L, 1L));

		assertEquals("avg mapping size", invRelation.getAvgMappingSize(), 2F, 0.5F);

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(6L));
		Resource[] resRelatedTo6 = invRelation.get(resource1);
		assertEquals(resRelatedTo6.length, 3);
		if(resRelatedTo6[0].getId() == 1L)
		{
			if(resRelatedTo6[1].getId() == 2L) assertEquals(resRelatedTo6[2].getId(), 3L);
			if(resRelatedTo6[2].getId() == 2L) assertEquals(resRelatedTo6[1].getId(), 3L);
		} 
		if(resRelatedTo6[0].getId() == 2L)
		{
			if(resRelatedTo6[1].getId() == 1L) assertEquals(resRelatedTo6[2].getId(), 3L);
			if(resRelatedTo6[2].getId() == 1L) assertEquals(resRelatedTo6[1].getId(), 3L);
		} 
		if(resRelatedTo6[0].getId() == 3L)
		{
			if(resRelatedTo6[1].getId() == 2L) assertEquals(resRelatedTo6[2].getId(), 1L);
			if(resRelatedTo6[2].getId() == 2L) assertEquals(resRelatedTo6[1].getId(), 1L);
		} 

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(100L));
		resRelatedTo100 = invRelation.get(resource1);
		assertEquals(resRelatedTo100.length, 0);

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(4L));
		mockResource2.expect(atLeastOnce()).method("getId").will(returnValue(1L));
		assertTrue(invRelation.hasRef(resource1, resource2));

		assertFalse(invRelation.hasRef(resource1, resource1));
		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(8L));
		assertFalse(invRelation.hasRef(resource1, resource1));
    }

    private class RelationContentsResultSet implements ResultSet
    {
		private long[][] reldef =  new long[][] {
			{1L, 4L}, {1L, 5L}, {1L, 6L},
			{2L, 5L}, {2L, 6L},
			{3L, 6L}
		};
    	
    	private int i = -1;
    	
        /**
         * {@inheritDoc}
         */
        public boolean next() throws SQLException
        {
        	i++;
            return i < reldef.length;
        }

        /**
         * {@inheritDoc}
         */
        public long getLong(int columnIndex) throws SQLException
        {
            return reldef[i][columnIndex-1];
        }

        // not called methods ---------------------------------------------------------------------

        /**
         * {@inheritDoc}
         */
        public int getConcurrency() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int getFetchDirection() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int getFetchSize() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int getRow() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int getType() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void afterLast() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void beforeFirst() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void cancelRowUpdates() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void clearWarnings() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void close() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void deleteRow() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void insertRow() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void moveToCurrentRow() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void moveToInsertRow() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void refreshRow() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateRow() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean first() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isAfterLast() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isBeforeFirst() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isFirst() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isLast() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean last() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean previous() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean rowDeleted() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean rowInserted() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean rowUpdated() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean wasNull() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public byte getByte(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public double getDouble(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public float getFloat(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int getInt(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public short getShort(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setFetchDirection(int direction) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setFetchSize(int rows) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateNull(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean absolute(int row) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean getBoolean(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean relative(int rows) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public byte[] getBytes(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateByte(int columnIndex, byte x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateDouble(int columnIndex, double x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateFloat(int columnIndex, float x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateInt(int columnIndex, int x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateLong(int columnIndex, long x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateShort(int columnIndex, short x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBoolean(int columnIndex, boolean x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBytes(int columnIndex, byte[] x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public InputStream getAsciiStream(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public InputStream getBinaryStream(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public InputStream getUnicodeStream(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Reader getCharacterStream(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Object getObject(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateObject(int columnIndex, Object x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateObject(int columnIndex, Object x, int scale) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getCursorName() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getString(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateString(int columnIndex, String x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public byte getByte(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public double getDouble(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public float getFloat(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int findColumn(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int getInt(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public long getLong(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public short getShort(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateNull(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean getBoolean(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public byte[] getBytes(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateByte(String columnName, byte x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateDouble(String columnName, double x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateFloat(String columnName, float x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateInt(String columnName, int x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateLong(String columnName, long x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateShort(String columnName, short x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBoolean(String columnName, boolean x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBytes(String columnName, byte[] x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public BigDecimal getBigDecimal(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public URL getURL(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Array getArray(int i) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateArray(int columnIndex, Array x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Blob getBlob(int i) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBlob(int columnIndex, Blob x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Clob getClob(int i) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateClob(int columnIndex, Clob x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Date getDate(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateDate(int columnIndex, Date x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Ref getRef(int i) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateRef(int columnIndex, Ref x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public ResultSetMetaData getMetaData() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public SQLWarning getWarnings() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Statement getStatement() throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Time getTime(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateTime(int columnIndex, Time x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Timestamp getTimestamp(int columnIndex) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public InputStream getAsciiStream(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public InputStream getBinaryStream(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public InputStream getUnicodeStream(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateAsciiStream(String columnName, InputStream x, int length)
            throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBinaryStream(String columnName, InputStream x, int length)
            throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Reader getCharacterStream(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Object getObject(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateObject(String columnName, Object x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateObject(String columnName, Object x, int scale) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Object getObject(int i, Map map) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getString(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateString(String columnName, String x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public BigDecimal getBigDecimal(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public URL getURL(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Array getArray(String colName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateArray(String columnName, Array x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Blob getBlob(String colName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateBlob(String columnName, Blob x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Clob getClob(String colName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateClob(String columnName, Clob x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Date getDate(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateDate(String columnName, Date x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Date getDate(int columnIndex, Calendar cal) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Ref getRef(String colName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateRef(String columnName, Ref x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Time getTime(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateTime(String columnName, Time x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Time getTime(int columnIndex, Calendar cal) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Timestamp getTimestamp(String columnName) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void updateTimestamp(String columnName, Timestamp x) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Object getObject(String colName, Map map) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Date getDate(String columnName, Calendar cal) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Time getTime(String columnName, Calendar cal) throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException
        {
            throw new UnsupportedOperationException();
        }
    }
    
    private class MockCoralStore implements CoralStore
    {
    	private Map resById = new HashMap();
    	
		/**
		 * {@inheritDoc}
		 */
		public Resource getResource(long id) throws EntityDoesNotExistException
		{
			Long idKey = new Long(id);
			Resource resource = (Resource) resById.get(idKey);
			if(resource == null)
			{
				resource = new MockResource(id);
				resById.put(idKey, resource);
			}
			return resource;
		}

		// uimplemented methods -------------------------------------------------------------------

        /**
         * {@inheritDoc}
         */
        public Resource[] getResource()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResource(Resource parent)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResource(String name)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getUniqueResource(String name) throws IllegalStateException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResource(Resource parent, String name)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getUniqueResource(Resource parent, String name) throws IllegalStateException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResourceByPath(String path)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getUniqueResourceByPath(String path) throws EntityDoesNotExistException, AmbigousEntityNameException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] getResourceByPath(Resource start, String path)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getUniqueResourceByPath(Resource start, String path) throws EntityDoesNotExistException, AmbigousEntityNameException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource createResource(String name, Resource parent, ResourceClass resourceClass, Map attributes) throws UnknownAttributeException, ValueRequiredException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void deleteResource(Resource resource) throws EntityInUseException, IllegalArgumentException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public int deleteTree(Resource res) throws EntityInUseException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setName(Resource resource, String name)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setParent(Resource child, Resource parent) throws CircularDependencyException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void unsetParent(Resource child)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setOwner(Resource resource, Subject owner)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource copyResource(Resource source, Resource destinationParent, String destinationName)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void copyTree(Resource sourceRoot, Resource destinationParent, String destinationName)
        {
			throw new UnsupportedOperationException();
        }
    }
    
    private class MockResource implements Resource
    {
    	private long id;
    	
    	public MockResource(long id)
    	{
    		this.id = id;
    	}
    	
		/**
		 * {@inheritDoc}
		 */
		public long getId()
		{
			return id;
		}

		// uimplemented methods -------------------------------------------------------------------

        /**
         * {@inheritDoc}
         */
        public String getPath()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public ResourceClass getResourceClass()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Subject getCreatedBy()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public java.util.Date getCreationTime()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Subject getModifiedBy()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public java.util.Date getModificationTime()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Subject getOwner()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public PermissionAssignment[] getPermissionAssignments()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public PermissionAssignment[] getPermissionAssignments(Role role)
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getParent()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isDefined(AttributeDefinition attribute) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Object get(AttributeDefinition attribute) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void set(AttributeDefinition attribute, Object value) throws UnknownAttributeException, ModificationNotPermitedException, ValueRequiredException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void unset(AttributeDefinition attribute) throws ValueRequiredException, UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void setModified(AttributeDefinition attribute) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isModified(AttributeDefinition attribute) throws UnknownAttributeException
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void update()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public void revert()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public Resource getDelegate()
        {
			throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        public String getName()
        {
			throw new UnsupportedOperationException();
        }
    }
}
