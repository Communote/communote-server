package com.communote.server.external.hibernate.usertypes;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * <p>
 * A hibernate user type which converts a Clob into a String and back again.
 * </p>
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class HibernateStringClobType implements UserType {

    @Override
    public Object assemble(java.io.Serializable cached, Object owner) {
        return this.deepCopy(cached);
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public java.io.Serializable disassemble(Object value) {
        return (java.io.Serializable) value;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        boolean equal = false;
        if (x == y) {
            equal = true;
        } else if (x == null || y == null) {
            equal = false;
        } else if (!(x instanceof String) || !(y instanceof String)) {
            equal = false;
        } else {
            equal = ((String) x).equals(y);
        }
        return equal;
    }

    @Override
    public int hashCode(Object x) {
        return x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
            throws HibernateException, SQLException {
        final StringBuilder buffer = new StringBuilder();
        try {
            // First we get the stream
            Reader inputStream = resultSet.getCharacterStream(names[0]);
            if (inputStream == null) {
                return null;
            }
            char[] buf = new char[1024];
            int read = -1;

            while ((read = inputStream.read(buf)) > 0) {
                buffer.append(new String(buf, 0, read));
            }
            inputStream.close();
        } catch (IOException exception) {
            throw new HibernateException("Unable to read from resultset", exception);
        }
        return buffer.toString();
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object data, int index)
            throws HibernateException, SQLException {
        if (data != null) {
            StringReader r = new StringReader((String) data);
            preparedStatement.setCharacterStream(index, r, ((String) data).length());
        }
        else {
            preparedStatement.setNull(index, sqlTypes()[0]);
        }
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        return this.deepCopy(original);
    }

    @Override
    public Class returnedClass() {
        return String.class;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.CLOB };
    }
}