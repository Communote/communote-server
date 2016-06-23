package com.communote.server.persistence.tasks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;

import com.communote.server.model.task.TaskStatus;

/**
 * 
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public final class TaskStatusEnum extends TaskStatus implements java.io.Serializable, Comparable,
        org.hibernate.usertype.EnhancedUserType {

    /**
     * serial id
     */
    private static final long serialVersionUID = 1L;

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    /**
     * Default constructor. Hibernate needs the default constructor to retrieve an instance of the
     * enum from a JDBC resultset. The instance will be converted to the correct enum instance in
     * {@link #nullSafeGet(java.sql.ResultSet, String[], Object)}.
     */
    public TaskStatusEnum() {
        super();
    }

    /**
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable cached, Object owner)
     */
    public Object assemble(java.io.Serializable cached, Object owner) {
        return cached;
    }

    /**
     * @see org.hibernate.usertype.UserType#deepCopy(Object)
     */
    public Object deepCopy(Object value) throws HibernateException {
        // Enums are immutable - nothing to be done to deeply clone it
        return value;
    }

    /**
     * @see org.hibernate.usertype.UserType#disassemble(Object value)
     */
    public java.io.Serializable disassemble(Object value) {
        return (java.io.Serializable) value;
    }

    /**
     * @see org.hibernate.usertype.UserType#equals(Object, Object)
     */
    public boolean equals(Object x, Object y) throws HibernateException {
        return (x == y) || (x != null && y != null && y.equals(x));
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#fromXMLString(String string)
     */
    public Object fromXMLString(String string) {
        return com.communote.server.model.task.TaskStatus.fromString(String.valueOf(string));
    }

    /**
     * @see org.hibernate.usertype.UserType#hashCode(Object value)
     */
    public int hashCode(Object value) {
        return value.hashCode();
    }

    /**
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public boolean isMutable() {
        // Enums are immutable
        return false;
    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, String[], Object)
     */
    public Object nullSafeGet(ResultSet resultSet, String[] values, Object owner)
            throws HibernateException, SQLException {
        final String value = (String) resultSet.getObject(values[0]);
        return resultSet.wasNull() ? null : fromString(value);
    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, Object, int)
     */
    public void nullSafeSet(PreparedStatement statement, Object value, int index)
            throws HibernateException, SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setObject(index, String.valueOf(String.valueOf(value)));
        }
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#objectToSQLString(Object object)
     */
    public String objectToSQLString(Object object) {
        return String.valueOf(((com.communote.server.model.task.TaskStatus) object).getValue());
    }

    /**
     * @see org.hibernate.usertype.UserType#replace(Object original, Object target, Object owner)
     */
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    /**
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    public Class returnedClass() {
        return TaskStatus.class;
    }

    /**
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#toXMLString(Object object)
     */
    public String toXMLString(Object object) {
        return String.valueOf(((com.communote.server.model.task.TaskStatus) object).getValue());
    }
}