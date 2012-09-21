package org.objectledge.coral.tools.transform;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.jcontainer.dna.Logger;
import org.objectledge.database.DatabaseType;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.pico.inject.FileSystemResourceInjector;

public class GenericToTabular
    implements TransformationComponent
{
    private Connection sourceConn;

    private Connection targetConn;

    private FileSystem fileSystem;

    // auto loaded scripts
    private String tables;

    private String dataColumns;

    private String attributes;

    private String fkConstraints;

    private String notNullConstraints;

    private String ukConstraints;

    private Logger log;

    @Override
    public void run(Connection sourceConn, Connection targetConn, FileSystem fileSystem, Logger log)
        throws SQLException
    {
        this.fileSystem = fileSystem;
        this.log = log;
        this.sourceConn = sourceConn;
        this.targetConn = targetConn;
        DatabaseType db = DatabaseType.detect(sourceConn);
        FileSystemResourceInjector.inject(fileSystem, this,
            String.format(".%s.sql", db.getSuffix()), ".sql");

        dropMetadataConstraints();
        transferMetadata();
        updateHandlers();
        updateParentClasses();
        updateDbTables();
        updateDbColumns();
        updateRootResource();
        transferCustomAttributes();
        setupMetadataConstraints();

        createDataTables();
        fillDataTables();

        setupPkConstraints();
        setupFkConstraints();
        setupNotNullConstraints();
        setupUkConstraints();

        transferAuxiliaryTables();

    }

    // these two methods need to go to PersitentResourceHandler

    public static String transformTableName(String name)
    {
        return name.replace('.', '_');
    }

    // a full list of reserved SQL keywords needs to be used

    public String transformColumnName(String name)
    {
        return transformColumnName(name, targetConn);
    }

    public static String transformColumnName(String name, Connection conn)
    {
        try
        {
            if(DatabaseUtils.reservedWords(conn).contains(name.toUpperCase()))
            {
                return name + "_";
            }
            else
            {
                return name;
            }
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void runScript(Connection conn, String path)
        throws SQLException
    {
        try
        {
            Reader reader = fileSystem.getReader(path, "UTF-8");
            DatabaseUtils.runScript(conn, reader);
        }
        catch(IOException e)
        {
            throw new RuntimeException("failed to load script " + path, e);
        }
    }

    private int runStatement(Connection conn, String sql)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        try
        {
            stmt.execute(sql);
            return stmt.getUpdateCount();
        }
        finally
        {
            stmt.close();
        }
    }

    private int transferTable(String name, int batchSize)
        throws SQLException
    {
        return DatabaseUtils.transferTable(sourceConn, targetConn, null, null, name, null, true,
            batchSize, false);
    }

    private interface StatementGenerator
    {
        void generate(ResultSet rs, StringBuilder buff)
            throws SQLException;
    }

    private void runGeneratedStatements(String info, String inputQuery, StatementGenerator gen)
        throws SQLException
    {
        log.info(info);
        List<String> out = new ArrayList<String>();
        Statement stmt = sourceConn.createStatement();
        try
        {
            ResultSet rs = stmt.executeQuery(inputQuery);
            try
            {
                StringBuilder buff = new StringBuilder();
                while(rs.next())
                {
                    gen.generate(rs, buff);
                    if(buff.length() > 0)
                    {
                        out.add(buff.toString());
                    }
                    buff.setLength(0);
                }
            }
            finally
            {
                rs.close();
            }
        }
        finally
        {
            stmt.close();
        }
        stmt = targetConn.createStatement();
        try
        {
            for(String o : out)
            {
                stmt.execute(o);
            }
        }
        finally
        {
            stmt.close();
        }
    }

    private void dropMetadataConstraints()
        throws SQLException
    {
        runScript(targetConn, "sql/coral/CoralRIDropConstraints.sql");
        runScript(targetConn, "sql/coral/CoralDatatypesDropConstraints.sql");
    }

    private void setupMetadataConstraints()
        throws SQLException
    {
        runScript(targetConn, "sql/coral/CoralRIConstraints.sql");
        runScript(targetConn, "sql/coral/CoralDatatypesConstraints.sql");
    }

    private void transferMetadata()
        throws SQLException
    {
        int cnt;
        log.info("transferring metadata");
        cnt = transferTable("coral_attribute_class", 10);
        log.info("transferred " + cnt + " attribute classes");
        cnt = transferTable("coral_resource_class", 100);
        log.info("transferred " + cnt + " resource classes");
        cnt = transferTable("coral_resource_class_inheritance", 100);
        log.info("transferred " + cnt + " resource class inheritance relationships");
        cnt = transferTable("coral_attribute_definition", 100);
        log.info("transferred " + cnt + " attribute definitions");

        cnt = transferTable("coral_permission", 100);
        log.info("transferred " + cnt + " permission definitions");
        cnt = transferTable("coral_permission_association", 100);
        log.info("transferred " + cnt + " permission to resource class relationships");

        cnt = transferTable("coral_role", 100);
        log.info("transferred " + cnt + " roles");
        cnt = transferTable("coral_role_implication", 100);
        log.info("transferred " + cnt + " role relationships");

        cnt = transferTable("coral_subject", 100);
        log.info("transferred " + cnt + " subjects");
        cnt = transferTable("coral_role_assignment", 100);
        log.info("transferred " + cnt + " subject role assignments");
        cnt = transferTable("coral_permission_assignment", 100);
        log.info("transferred " + cnt + " permission grants");

        cnt = transferTable("coral_resource", 1000);
        log.info("transferred " + cnt + " resource metadata records");

        cnt = transferTable("coral_relation", 100);
        log.info("transferred " + cnt + " resource relation definitions");
        cnt = transferTable("coral_relation_data", 1000);
        log.info("transferred " + cnt + " resource relation elements");
    }

    private void updateHandlers()
        throws SQLException
    {
        log.info("updating class handers");
        int cnt = runStatement(
            targetConn,
            "UPDATE coral_resource_class\n"
                + "SET handler_class_name = 'org.objectledge.coral.datatypes.PersistentResourceHandler'\n"
                + "WHERE resource_class_id > 2");
        log.info("switched " + cnt + " classes to use PersistentResourceHandler");
    }

    private void updateDbTables()
        throws SQLException
    {
        runGeneratedStatements("updating resource class db_tables", tables,
            new StatementGenerator()
                {
                    public void generate(ResultSet rs, StringBuilder buff)
                        throws SQLException
                    {
                        final String table = rs.getString(1);
                        buff.append("UPDATE coral_resource_class\n");
                        buff.append("SET db_table_name = '").append(transformTableName(table))
                            .append("'\n");
                        buff.append("WHERE name = '").append(table).append("'");
                    }
                });
    }

    private void updateDbColumns()
        throws SQLException
    {
        runGeneratedStatements("updating resource class db_tables", attributes,
            new StatementGenerator()
                {
                    public void generate(ResultSet rs, StringBuilder buff)
                        throws SQLException
                    {
                        final long attributeDefId = rs.getLong(3);
                        final String attributeName = rs.getString(4);
                        final String columnName = transformColumnName(attributeName);
                        if(!attributeName.equals(columnName))
                        {
                            buff.append("UPDATE coral_attribute_definition\nSET db_column = '"
                                + columnName + "'\nWHERE attribute_definition_id = "
                                + attributeDefId);
                        }
                    }
                });
    }

    private void updateParentClasses()
        throws SQLException
    {
        int count = runStatement(targetConn,
            "UPDATE coral_resource_class_inheritance SET parent = 2 where parent = 1");
        log.info("switched " + count + " classes to inherit from coral.PersistentNode");
    }

    private void updateRootResource()
        throws SQLException
    {
        runStatement(targetConn,
            "update coral_resource set resource_class_id = 2 where resource_id = 1");
        log.info("switched root resource class to PersitentNode");
    }

    private void transferCustomAttributes()
        throws SQLException
    {
        Statement stmt = sourceConn.createStatement();
        try
        {
            ResultSet rs = stmt
                .executeQuery("SELECT fk_table FROM coral_attribute_mapping WHERE is_custom");
            try
            {
                log.info("transferring custom attributes");
                while(rs.next())
                {
                    String tableName = rs.getString(1);
                    int cnt = transferTable(tableName, 1000);
                    log.info("transferred " + cnt + " " + tableName + " records");
                }
            }
            finally
            {
                rs.close();
            }
        }
        finally
        {
            stmt.close();
        }
    }

    private void createDataTables()
        throws SQLException
    {
        log.info("creating resource data tables");
        List<String> out = new ArrayList<String>();
        Statement stmt = sourceConn.createStatement();
        try
        {
            ResultSet rs = stmt.executeQuery(dataColumns);
            try
            {
                StringBuilder buff = new StringBuilder();
                String lastTable = null;
                while(rs.next())
                {
                    final String table = rs.getString(1);
                    if(lastTable == null || !lastTable.equals(table))
                    {
                        if(lastTable != null)
                        {
                            buff.append("\n)");
                            out.add(buff.toString());
                            buff.setLength(0);
                        }
                        buff.append("CREATE TABLE ").append(transformTableName(table))
                            .append(" (\n  resource_id BIGINT");
                    }
                    buff.append(",\n  ").append(transformColumnName(rs.getString(2))).append(" ")
                        .append(rs.getString(3));
                    lastTable = table;
                }
                if(lastTable != null)
                {
                    buff.append("\n)\n");
                    out.add(buff.toString());
                }
            }
            finally
            {
                rs.close();
            }
        }
        finally
        {
            stmt.close();
        }
        stmt = targetConn.createStatement();
        try
        {
            for(String o : out)
            {
                stmt.execute(o);
            }
        }
        finally
        {
            stmt.close();
        }
    }

    private void fillDataTables()
        throws SQLException
    {
        log.info("copying resource data");
        Statement stmt = sourceConn.createStatement();
        try
        {
            List<ResourceClassInfo> infos = new ArrayList<ResourceClassInfo>();
            ResultSet rs = stmt.executeQuery(attributes);
            try
            {
                ResourceClassInfo rci = null;
                while(rs.next())
                {
                    if(rci == null || rci.getResourceClassId() != rs.getLong(1))
                    {
                        rci = new ResourceClassInfo(rs.getLong(1), rs.getString(2));
                        infos.add(rci);
                    }
                    rci.addAttribute(new AttributeInfo(rs.getLong(3), rs.getString(4), rs
                        .getString(5), rs.getBoolean(6), rs.getBoolean(7)));
                }
            }
            finally
            {
                rs.close();
            }
            for(ResourceClassInfo rci : infos)
            {
                long time = System.currentTimeMillis();
                int count;
                try
                {
                    count = DatabaseUtils.transfer(sourceConn, targetConn, rci.selectQuery(),
                        rci.insertQuery(), 1000, false);
                }
                catch(SQLException e)
                {
                    SQLException n = e.getNextException();
                    if(n != null)
                    {
                        System.out.println(n.toString());
                    }
                    throw e;
                }
                time = System.currentTimeMillis() - time;
                log.info("  transferred " + count + " " + rci.getClassName() + " resources in "
                    + time + "ms");
            }
        }
        finally
        {
            stmt.close();
        }
    }

    private void setupPkConstraints()
        throws SQLException
    {
        runGeneratedStatements("setting up primary key constraints", tables,
            new StatementGenerator()
                {
                    public void generate(ResultSet rs, StringBuilder buff)
                        throws SQLException
                    {
                        final String table = rs.getString(1);
                        buff.append("ALTER TABLE ").append(transformTableName(table)).append("\n")
                            .append("ADD PRIMARY KEY (resource_id)");
                    }
                });
    }

    private void setupFkConstraints()
        throws SQLException
    {
        runGeneratedStatements("setting up foreign key constraints", fkConstraints,
            new StatementGenerator()
                {
                    public void generate(ResultSet rs, StringBuilder buff)
                        throws SQLException
                    {
                        final String table = rs.getString(1);
                        final String column = rs.getString(2);
                        final String ref_table = rs.getString(3);
                        final String ref_column = rs.getString(4);
                        buff.append("ALTER TABLE ").append(transformTableName(table)).append("\n")
                            .append("ADD FOREIGN KEY (").append(transformColumnName(column))
                            .append(")\n").append("REFERENCES ").append(ref_table).append("(")
                            .append(transformColumnName(ref_column)).append(")");
                    }
                });
    }

    private void setupNotNullConstraints()
        throws SQLException
    {
        runGeneratedStatements("setting up non-null constraints", notNullConstraints,
            new StatementGenerator()
                {
                    public void generate(ResultSet rs, StringBuilder buff)
                        throws SQLException
                    {
                        final String table = rs.getString(1);
                        final String column = rs.getString(2);
                        buff.append("ALTER TABLE ").append(transformTableName(table)).append("\n")
                            .append("ALTER ").append(transformColumnName(column)).append("\n")
                            .append("SET NOT NULL");
                    }
                });
    }

    private void setupUkConstraints()
        throws SQLException
    {
        runGeneratedStatements("setting up unique constraints", ukConstraints,
            new StatementGenerator()
                {
                    public void generate(ResultSet rs, StringBuilder buff)
                        throws SQLException
                    {
                        final String table = rs.getString(1);
                        final String column = rs.getString(2);
                        buff.append("ALTER TABLE ").append(transformTableName(table)).append("\n")
                            .append("ADD UNIQUE(").append(transformColumnName(column)).append(")");
                    }
                });
    }

    private void transferAuxiliaryTables()
        throws SQLException
    {
        int cnt;
        log.info("transferring additional tables");
        runStatement(targetConn, "ALTER TABLE ledge_naming_attribute "
            + "DROP CONSTRAINT ledge_naming_attribute_context_id_fkey");
        cnt = transferTable("ledge_naming_context", 100);
        log.info("transferred " + cnt + " naming contexts");
        cnt = transferTable("ledge_naming_attribute", 1000);
        log.info("transferred " + cnt + " naming attributes");
        runStatement(targetConn, "ALTER TABLE ledge_naming_attribute "
            + "ADD FOREIGN KEY (context_id) REFERENCES ledge_naming_context(context_id)");
        cnt = transferTable("ledge_id_table", 10);
        log.info("transferred " + cnt + " ledge_id_table entries");
    }

    private class ResourceClassInfo
    {
        private final long resourceClassId;

        private final String className;

        private final List<AttributeInfo> attributes = new ArrayList<AttributeInfo>();

        public ResourceClassInfo(long resourceClassId, String className)
        {
            this.resourceClassId = resourceClassId;
            this.className = className;
        }

        public long getResourceClassId()
        {
            return resourceClassId;
        }

        public String getClassName()
        {
            return className;
        }

        public void addAttribute(AttributeInfo attribute)
        {
            attributes.add(attribute);
        }

        @Override
        public String toString()
        {
            return className + " " + attributes + "\n";
        }

        public String selectQuery()
        {
            StringBuilder buff = new StringBuilder();
            buff.append("select\n");
            buff.append("  coalesce(\n");
            for(int i = 1; i <= attributes.size(); i++)
            {
                buff.append("    a").append(i).append(".resource_id");
                if(i < attributes.size())
                {
                    buff.append(",\n");
                }
            }
            buff.append(") resource_id,\n");
            for(int i = 1; i <= attributes.size(); i++)
            {
                AttributeInfo ai = attributes.get(i - 1);
                buff.append("  a").append(i).append(".data as ")
                    .append(transformColumnName(ai.getAttributeName()));
                if(i < attributes.size())
                {
                    buff.append(",");
                }
                buff.append("\n");
            }
            buff.append("from\n");
            for(int i = 1; i <= attributes.size(); i++)
            {
                AttributeInfo ai = attributes.get(i - 1);
                if(i > 1)
                {
                    buff.append("  full outer join\n");
                }
                buff.append("  (");
                buff.append(ai.selectStatement());
                buff.append("  ) a").append(i);
                if(i > 1)
                {
                    buff.append(" on (");
                    if(i > 2)
                    {
                        buff.append("coalesce(");
                        for(int j = 1; j <= i - 1; j++)
                        {
                            buff.append("a").append(j).append(".resource_id");
                            if(j < i - 1)
                            {
                                buff.append(", ");
                            }
                        }
                        buff.append(")");
                    }
                    else
                    {
                        buff.append("a").append(i - 1).append(".resource_id");
                    }
                    buff.append(" = a").append(i).append(".resource_id)\n");
                }
            }
            return buff.toString();
        }

        public String insertQuery()
        {
            StringBuilder buff = new StringBuilder();
            buff.append("insert into ").append(transformTableName(className)).append(" (\n");
            buff.append("  resource_id,\n");
            for(int i = 1; i <= attributes.size(); i++)
            {
                AttributeInfo ai = attributes.get(i - 1);
                buff.append("  ").append(transformColumnName(ai.getAttributeName()));
                if(i < attributes.size())
                {
                    buff.append(",\n");
                }
                else
                {
                    buff.append(")\n");
                }
            }
            buff.append("values (");
            for(int i = 0; i <= attributes.size(); i++)
            {
                buff.append("?");
                if(i < attributes.size())
                {
                    buff.append(", ");
                }
            }
            buff.append(")\n");
            return buff.toString();
        }
    }

    private static class AttributeInfo
    {
        private final long attributeDefinitionId;

        private final String attributeName;

        private final String tableName;

        private final boolean isForeignKey;

        private final boolean isCustom;

        public AttributeInfo(long attributeDefinitionId, String attributeName, String tableName,
            boolean isForeignKey, boolean isCustom)
        {
            this.attributeDefinitionId = attributeDefinitionId;
            this.attributeName = attributeName;
            this.tableName = tableName;
            this.isForeignKey = isForeignKey;
            this.isCustom = isCustom;
        }

        public long getAttributeDefinitionId()
        {
            return attributeDefinitionId;
        }

        public String getAttributeName()
        {
            return attributeName;
        }

        public String getTableName()
        {
            return tableName;
        }

        public boolean isForeignKey()
        {
            return isForeignKey;
        }

        public boolean isCustom()
        {
            return isCustom;
        }

        @Override
        public String toString()
        {
            return "\n  " + attributeName;
        }

        public String selectStatement()
        {
            StringBuilder buff = new StringBuilder();
            buff.append("select\n");
            buff.append("     ")
                .append(isCustom() ? "g.data_key" : (isForeignKey() ? "a.ref" : "a.data"))
                .append(" as data, \n");
            buff.append("     g.resource_id\n");
            buff.append("   from\n");
            buff.append("     coral_generic_resource g");
            if(!isCustom())
            {
                buff.append(",\n");
                buff.append("     ").append(getTableName()).append(" a");
            }
            buff.append("\n");
            buff.append("   where\n");
            buff.append("     g.attribute_definition_id = ").append(getAttributeDefinitionId())
                .append("\n");
            if(!isCustom())
            {
                buff.append("     and a.data_key = g.data_key\n");
            }
            return buff.toString();
        }
    }
}
