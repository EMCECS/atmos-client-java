package com.emc.atmos.sync.util;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JdbcUtil {
    private static final Logger l4j = Logger.getLogger( JdbcUtil.class );

    private static final String SQL_PARAMETER_PATTERN = ":([a-zA-Z_]+)";

    /**
     * Allows the use of parameterized SQL statements (i.e. "select x from y where id = :id")
     */
    public static PreparedStatement prepareStatement( Connection connection, String sql ) throws SQLException {
        String indexedSql = sql.replaceAll( SQL_PARAMETER_PATTERN, "?" );
        l4j.debug( "Creating new statement for SQL:\nparameterized: " + sql + "\nindexed      : " + indexedSql );
        return connection.prepareStatement( indexedSql );
    }

    /**
     * Allows the use of parameterized SQL statements (i.e. "select x from y where id = :id")
     */
    public static <T> T executeQueryForObject( DataSource dataSource,
                                               String sql,
                                               Map<String, String> params,
                                               Class<T> returnType ) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            statement = prepareStatement( connection, sql );
            setParameters( statement, sql, params );
            resultSet = statement.executeQuery();
            if ( !resultSet.next() ) return null;
            return castResult( resultSet, 1, returnType );
        } catch ( SQLException e ) {
            throw new RuntimeException( e );
        } finally {
            close( resultSet );
            close( statement );
            close( connection );
        }
    }

    public static void executeUpdate( DataSource dataSource, String sql, Map<String, String> params ) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = prepareStatement( connection, sql );
            setParameters( statement, sql, params );
            statement.executeUpdate();
        } catch ( SQLException e ) {
            throw new RuntimeException( e );
        } finally {
            close( statement );
            close( connection );
        }
    }

    public static void setParameters( PreparedStatement statement, String sql, Map<String, String> params )
            throws SQLException {
        l4j.debug( "Preparing statement parameters for SQL:\n" + sql );
        Matcher matcher = Pattern.compile( SQL_PARAMETER_PATTERN ).matcher( sql );
        int paramIndex = 1;
        while ( matcher.find() ) {
            String paramName = matcher.group( 1 );
            l4j.debug( "Setting parameter (index " + paramIndex + "): " + paramName + "=" + params.get( paramName ) );
            statement.setString( paramIndex++, params.get( paramName ) );
        }
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T castResult( ResultSet resultSet, int columnIndex, Class<T> returnType ) throws SQLException {
        if ( String.class == returnType ) {
            return (T) resultSet.getString( columnIndex );
        } else if ( Integer.class == returnType ) {
            return (T) new Integer( resultSet.getInt( columnIndex ) );
        } else {
            throw new UnsupportedOperationException(
                    "return type " + returnType + " is not supported (maybe you should add it)" );
        }
    }

    public static void close( ResultSet resultSet ) {
        if ( resultSet != null ) {
            try {
                resultSet.close();
            } catch ( SQLException e ) {
                // ignore
            }
        }
    }

    public static void close( Statement statement ) {
        if ( statement != null ) {
            try {
                statement.close();
            } catch ( SQLException e ) {
                // ignore
            }
        }
    }

    public static void close( Connection connection ) {
        if ( connection != null ) {
            try {
                connection.close();
            } catch ( SQLException e ) {
                // ignore
            }
        }
    }

    private JdbcUtil() {
    }
}
