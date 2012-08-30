package org.objectledge.coral.datatypes;

import java.sql.SQLException;

import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.database.persistence.PersistentFactory;

public class CoralAttributeMapping
    implements Persistent
{
    private static final String TABLE = "coral_attribute_mapping";

    private static final String[] KEY_COLUMNS = { "attribute_class_id" };

    private long classId;

    private boolean isFk;

    private String sqlType;

    private String fkTable;

    private String fkKeyColumn;

    private boolean isCustom;

    public long getClassId()
    {
        return classId;
    }

    public void setClassId(long classId)
    {
        this.classId = classId;
    }

    public boolean isFk()
    {
        return isFk;
    }

    public void setFk(boolean isFk)
    {
        this.isFk = isFk;
    }

    public String getSqlType()
    {
        return sqlType;
    }

    public void setSqlType(String sqlType)
    {
        this.sqlType = sqlType;
    }

    public String getFkTable()
    {
        return fkTable;
    }

    public void setFkTable(String fkTable)
    {
        this.fkTable = fkTable;
    }

    public String getFkKeyColumn()
    {
        return fkKeyColumn;
    }

    public void setFkKeyColumn(String fkKeyColumn)
    {
        this.fkKeyColumn = fkKeyColumn;
    }

    public boolean isCustom()
    {
        return isCustom;
    }

    public void setCustom(boolean isCustom)
    {
        this.isCustom = isCustom;
    }

    @Override
    public String getTable()
    {
        return TABLE;
    }

    @Override
    public String[] getKeyColumns()
    {
        return KEY_COLUMNS;
    }

    @Override
    public void setData(InputRecord record)
        throws SQLException
    {
        classId = record.getLong("attribute_class_id");
        isFk = record.getBoolean("is_fk");
        sqlType = record.getString("sql_type");
        if(record.isNull("is_custom"))
        {
            isCustom = false;
        }
        else
        {
            isCustom = record.getBoolean("is_custom");
        }
        if(isFk || isCustom)
        {
            fkTable = record.getString("fk_table");
            fkKeyColumn = record.getString("fk_key_column");
        }
    }

    @Override
    public void getData(OutputRecord record)
        throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getSaved()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSaved(long id)
    {
        //
    }

    public static PersistentFactory<CoralAttributeMapping> FACTORY = new PersistentFactory<CoralAttributeMapping>()
        {
            @Override
            public CoralAttributeMapping newInstance()
                throws Exception
            {
                return new CoralAttributeMapping();
            }
        };
}
