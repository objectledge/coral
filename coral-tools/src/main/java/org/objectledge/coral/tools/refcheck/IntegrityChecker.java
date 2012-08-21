package org.objectledge.coral.tools.refcheck;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Deque;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jcontainer.dna.Logger;
import org.objectledge.database.DatabaseType;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.pico.inject.FileSystemResourceInjector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Checks integrity constraints that are not enforced on the database level.
 */
public class IntegrityChecker
{
    private final Connection conn;

    private final DatabaseType dbType;

    private final Logger log;

    private SetMultimap<Long, Long> hierarchy;

    private SortedMap<Long, String> classNames;

    // injected queries

    private String genericRequiredAttr;

    private String genericRequiredData;

    private String classHierarchy;

    private String nullAttrValues;

    private String attrCheckList;

    private String attrCheckMatch;

    private String attrCheckMatchMulti;

    private String attrCheckMatch2;

    private String attrCheckMatchMulti2;

    private String orphanedGenericResource;

    public IntegrityChecker(Connection conn, FileSystem fileSystem, Logger log)
        throws SQLException
    {
        this.conn = conn;
        this.log = log;
        this.dbType = DatabaseType.detect(conn);
        FileSystemResourceInjector.inject(fileSystem, this,
            String.format(".%s.sql", dbType.getSuffix()), ".sql");
        hierarchy = getResourceClassesHierarchy();
        classNames = getResourceClassNames();
    }

    public void run()
        throws SQLException
    {
        checkGenericRequiredAttributes();
        checkGenericNullAttributeValues();
        checkGenericAttributes();
        checkSharedGenericAttributes();
        checkOrphanedGenericResourceRecors();
    }

    private void checkGenericRequiredAttributes()
        throws SQLException
    {
        log.info("checking required attributes in Generic resources");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(genericRequiredAttr);
                try
                {
                    while(rset1.next())
                    {
                        long classId = rset1.getLong(1);
                        long attrId = rset1.getLong(2);
                        String attrName = rset1.getString(3);
                        String attrTable = rset1.getString(4);
                        String fkColumn = rset1.getBoolean(6) ? rset1.getString(5) : "data_key";
                        for(Long descClassId : hierarchy.get(classId))
                        {
                            String className = classNames.get(descClassId);
                            String query = format(genericRequiredData, attrId, attrTable, fkColumn,
                                descClassId, attrId, fkColumn);
                            log.info("  checking attribute " + attrName + " in class " + className);
                            ResultSet rset2 = stmt2.executeQuery(query);
                            try
                            {
                                while(rset2.next())
                                {
                                    log.error("missing required attribute resource_id="
                                        + rset2.getLong(1) + " attribute_defnition_id="
                                        + rset2.getLong(2) + " data_key=" + rset2.getLong(3));
                                }
                            }
                            finally
                            {
                                rset2.close();
                            }
                        }
                    }
                }
                finally
                {
                    rset1.close();
                }
            }
            finally
            {
                stmt2.close();
            }
        }
        finally
        {
            stmt1.close();
        }
    }

    private void checkGenericNullAttributeValues()
        throws SQLException
    {
        log.info("checking null attribute values");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(nullAttrValues);
                try
                {
                    while(rset1.next())
                    {
                        String table = rset1.getString(1);
                        String column = rset1.getString(2);

                        String query = format("SELECT data_key FROM %s WHERE %s IS NULL", table,
                            column);
                        log.info("  checking table " + table);
                        ResultSet rset2 = stmt2.executeQuery(query);
                        try
                        {
                            while(rset2.next())
                            {
                                log.error(format("null value in %s data_key = %d", table,
                                    rset2.getLong(1)));
                            }
                        }
                        finally
                        {
                            rset2.close();
                        }
                    }

                }
                finally
                {
                    rset1.close();
                }
            }
            finally
            {
                stmt2.close();
            }
        }
        finally
        {
            stmt1.close();
        }
    }

    private SetMultimap<Long, Long> getResourceClassesHierarchy()
        throws SQLException
    {
        SetMultimap<Long, Long> direct = HashMultimap.create();
        Statement stmt = conn.createStatement();
        try
        {
            ResultSet rset = stmt.executeQuery(classHierarchy);
            try
            {
                while(rset.next())
                {
                    direct.put(rset.getLong(1), rset.getLong(2));
                }
            }
            finally
            {
                rset.close();
            }
        }
        finally
        {
            stmt.close();
        }
        SetMultimap<Long, Long> indirect = HashMultimap.create();
        Deque<Long> stack = new LinkedList<Long>();
        for(Long parent : direct.keySet())
        {
            stack.addLast(parent);
            while(!stack.isEmpty())
            {
                Long current = stack.removeLast();
                for(Long descendant : direct.get(current))
                {
                    indirect.put(current, descendant);
                    if(!descendant.equals(current))
                    {
                        stack.addLast(descendant);
                    }
                }
            }
        }
        return indirect;
    }

    private SortedMap<Long, String> getResourceClassNames()
        throws SQLException
    {
        SortedMap<Long, String> map = new TreeMap<Long, String>();
        Statement stmt = conn.createStatement();
        try
        {
            ResultSet rset = stmt
                .executeQuery("SELECT resource_class_id, name FROM coral_resource_class");
            try
            {
                while(rset.next())
                {
                    map.put(rset.getLong(1), rset.getString(2));
                }
            }
            finally
            {
                rset.close();
            }
        }
        finally
        {
            stmt.close();
        }
        return map;
    }

    private void checkGenericAttributes()
        throws SQLException
    {
        log.info("checking null attribute values");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(attrCheckList);
                // System.out.println(attrCheckList);
                try
                {
                    while(rset1.next())
                    {
                        long attrClass = rset1.getLong(1);
                        String table = rset1.getString(2);
                        String column = rset1.getString(3);

                        String query;
                        final boolean resourceList = table.contains("list");
                        query = format(resourceList ? attrCheckMatchMulti : attrCheckMatch,
                            attrClass, column, table);
                        log.info("  checking table " + table);
                        // System.out.println(query);
                        ResultSet rset2 = stmt2.executeQuery(query);
                        try
                        {
                            while(rset2.next())
                            {
                                if(!resourceList || (rset2.getLong(1) == 0 && rset2.wasNull()))
                                {
                                    log.error(format("coral_generic_resource.data_key=%d %s.%s=%d",
                                        rset2.getLong(1), table, column, rset2.getLong(2)));
                                }
                            }
                        }
                        finally
                        {
                            rset2.close();
                        }
                    }

                }
                finally
                {
                    rset1.close();
                }
            }
            finally
            {
                stmt2.close();
            }
        }
        finally
        {
            stmt1.close();
        }
    }

    private void checkSharedGenericAttributes()
        throws SQLException
    {
        log.info("checking null attribute values");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(attrCheckList);
                // System.out.println(attrCheckList);
                try
                {
                    while(rset1.next())
                    {
                        long attrClass = rset1.getLong(1);
                        String table = rset1.getString(2);
                        String column = rset1.getString(3);

                        String query;
                        final boolean resourceList = table.contains("list")
                            || table.contains("parameters");
                        query = format(resourceList ? attrCheckMatchMulti2 : attrCheckMatch2,
                            attrClass, column, table);
                        log.info("  checking table " + table);
                        // System.out.println(query);
                        ResultSet rset2 = stmt2.executeQuery(query);
                        try
                        {
                            while(rset2.next())
                            {

                                log.error(format(
                                    "%s.%s=%d is joined with %d coral_generic_resource_records",
                                    table, column, rset2.getLong(1), rset2.getInt(2)));
                            }
                        }
                        finally
                        {
                            rset2.close();
                        }
                    }

                }
                finally
                {
                    rset1.close();
                }
            }
            finally
            {
                stmt2.close();
            }
        }
        finally
        {
            stmt1.close();
        }
    }

    private void checkOrphanedGenericResourceRecors()
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        try
        {
            ResultSet rset = stmt.executeQuery(orphanedGenericResource);
            try
            {
                while(rset.next())
                {
                    log.error(format(
                        "coral_generic_resource.resource_id=%d does not have corresponding coral_resource",
                        rset.getLong(1)));
                }
            }
            finally
            {
                rset.close();
            }
        }
        finally
        {
            stmt.close();
        }
    }
}
