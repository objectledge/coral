package org.objectledge.coral.touchstone.dbunit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;

public class BooleanDataType
    extends AbstractDataType
{
    public BooleanDataType()
    {
        super("BOOLEAN", Types.BOOLEAN, Boolean.class, false);
    }

    // DataType class

    public Object typeCast(Object value)
        throws TypeCastException
    {
        if(value == null)
        {
            return null;
        }

        if(value instanceof Boolean)
        {
            return value;
        }

        if(value instanceof Number)
        {
            Number number = (Number)value;
            if(number.intValue() == 0)
                return Boolean.FALSE;
            else
                return Boolean.TRUE;
        }

        if(value instanceof String)
        {
            String string = (String)value;

            if(string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false"))
            {
                return Boolean.valueOf(string);
            }
            else
            {
                return typeCast(DataType.INTEGER.typeCast(string));
            }
        }

        throw new TypeCastException(value.toString());
    }

    public int compare(Object o1, Object o2)
        throws TypeCastException
    {
        Boolean value1 = (Boolean)typeCast(o1);
        Boolean value2 = (Boolean)typeCast(o2);

        if(value1 == null && value2 == null)
        {
            return 0;
        }

        if(value1 == null && value2 != null)
        {
            return -1;
        }

        if(value1 != null && value2 == null)
        {
            return 1;
        }

        if(value1.equals(value2))
        {
            return 0;
        }

        if(value1.equals(Boolean.FALSE))
        {
            return -1;
        }

        return 1;
    }

    public Object getSqlValue(int column, ResultSet resultSet)
        throws SQLException, TypeCastException
    {
        boolean value = resultSet.getBoolean(column);
        if(resultSet.wasNull())
        {
            return null;
        }
        return value ? Boolean.TRUE : Boolean.FALSE;
    }

    public void setSqlValue(Object value, int column, PreparedStatement statement)
        throws SQLException, TypeCastException
    {
        statement.setBoolean(column, ((Boolean)typeCast(value)).booleanValue());
    }

}
