package build.dream.api.ibatis;

import build.dream.api.constants.Constants;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class DatabaseIdProvider implements org.apache.ibatis.mapping.DatabaseIdProvider {
    private static final String DATABASE_PRODUCT_NAME_MYSQL = "MySQL";
    private static final String DATABASE_PRODUCT_NAME_ORACLE = "Oracle";
    private static final String DATABASE_PRODUCT_NAME_SQL_SERVER = "sqlserver";

    @Override
    public void setProperties(Properties properties) {
    }

    @Override
    public String getDatabaseId(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        String databaseProductName = connection.getMetaData().getDatabaseProductName();
        switch (databaseProductName) {
            case DATABASE_PRODUCT_NAME_MYSQL:
                return Constants.DATABASE_ID_MYSQL;
            case DATABASE_PRODUCT_NAME_ORACLE:
                return Constants.DATABASE_ID_ORACLE;
            case DATABASE_PRODUCT_NAME_SQL_SERVER:
                return Constants.DATABASE_ID_SQL_SERVER;
            default:
                return null;
        }
    }
}
