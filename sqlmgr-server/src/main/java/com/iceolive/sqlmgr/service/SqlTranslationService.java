package com.iceolive.sqlmgr.service;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLColumnPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLNotNullConstraint;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库翻译
 *
 * @author:wangmianzhe
 **/
@Service
public class SqlTranslationService {
    /**
     * mysql翻译成达梦
     * @param sql
     * @return
     */
    public String mysql2dm(String sql) {
        if(sql == null){
            return sql;
        }
        StringBuilder sb = new StringBuilder();
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        for (SQLStatement sqlStatement : statementList) {
            if (sqlStatement instanceof MySqlCreateTableStatement) {
                MySqlCreateTableStatement mySqlCreateTableStatement = (MySqlCreateTableStatement) sqlStatement;
                String tableName = mySqlCreateTableStatement.getTableSource().toString().replace("`", "");
                //用于存储字段注释
                StringBuilder sb2 = new StringBuilder();
                //是否建表前检测
                StringBuilder sb3 = new StringBuilder();
                if (mySqlCreateTableStatement.isIfNotExiists()) {
                    sb3.append("DECLARE\n");
                    sb3.append("  V_COUNT INT:=0;\n");
                    sb3.append("  V_SQL TEXT;\n");
                    sb3.append("BEGIN\n");
                    sb3.append("  select count(1) into V_COUNT from dba_tables where TABLE_NAME='" + tableName + "' and OWNER=(select user from dual);\n");
                    sb3.append("  IF(V_COUNT=0) THEN\n");
                }
                //建表语句
                sb.append("create table ");
                sb.append(tableName);
                sb.append("(\n");
                for (SQLTableElement sqlTableElement : mySqlCreateTableStatement.getTableElementList()) {
                    if (sqlTableElement instanceof SQLColumnDefinition) {
                        SQLColumnDefinition sqlColumnDefinition = (SQLColumnDefinition) sqlTableElement;
                        String columnName = sqlColumnDefinition.getColumnName().replace("`", "");
                        //添加字段注释
                        if (sqlColumnDefinition.getComment() != null) {
                            sb2.append(MessageFormat.format("comment on column \"{0}\".\"{1}\" is {2};\n", tableName, columnName, sqlColumnDefinition.getComment().toString()));
                        }
                        sb.append(MessageFormat.format("    \"{0}\"", columnName));
                        String dataType = sqlColumnDefinition.getDataType().getName().toLowerCase();
                        switch (dataType) {
                            case "tinyint":
                                sb.append(" tinyint");
                                break;
                            case "int":
                                sb.append(" int");
                                break;
                            case "float":
                            case "double":
                                List args = sqlColumnDefinition.getDataType().getArguments();
                                if (args.size() == 2) {
                                    sb.append(" numeric(" + args.get(0) + "," + args.get(1) + ")");
                                } else {
                                    sb.append(" double");
                                }
                                break;
                            case "point":
                            case "multipolygon":
                                //空间类型
                                sb.append(" st_" + dataType);
                                break;
                            default:
                                sb.append(" " + sqlColumnDefinition.getDataType().toString().toLowerCase());
                                break;
                        }
                        if (sqlColumnDefinition.isAutoIncrement()) {
                            sb.append(" identity");
                        } else {
                            if (sqlColumnDefinition.getConstraints().stream().filter(m -> m instanceof SQLNotNullConstraint).count() > 0) {
                                sb.append(" not null");
                            } else {
                                sb.append(" null");
                            }
                            if (sqlColumnDefinition.getDefaultExpr() != null) {
                                sb.append(" default " + sqlColumnDefinition.getDefaultExpr().toString());
                            }
                        }
                        MySqlPrimaryKey primaryKey = (MySqlPrimaryKey) mySqlCreateTableStatement.getTableElementList().stream().filter(m -> m instanceof MySqlPrimaryKey).findFirst().orElse(null);
                        if (primaryKey != null && primaryKey.containsColumn(sqlColumnDefinition.getColumnName())) {
                            //由于primaryKey存储的列名不确定前后有没有加`，这里用字段定义里面的列名。
                            sb.append("\n        primary key");
                        }
                        if (sqlColumnDefinition.getConstraints().stream().filter(m -> m instanceof SQLColumnPrimaryKey).count() > 0) {
                            sb.append("\n        primary key");
                        }
                        sb.append(",\n");
                    }
                }
                //去掉最后一个逗号和换行
                sb.deleteCharAt(sb.length() - 2);
                sb.append(");\n");
                //添加表注释
                if (mySqlCreateTableStatement.getComment() != null) {
                    sb.append(MessageFormat.format("comment on table  \"{0}\" is {1};\n", tableName, mySqlCreateTableStatement.getComment().toString()));
                }
                sb.append(sb2);
                if (mySqlCreateTableStatement.isIfNotExiists()) {
                    //执行多条
                    for (String str : sb.toString().split(";")) {
                        if (str.trim().length() > 0) {
                            sb3.append("    V_SQL='");
                            sb3.append(str.replace("'", "''") + ";");
                            sb3.append("';\n");
                            sb3.append("    EXECUTE IMMEDIATE V_SQL;\n");
                        }
                    }
                    sb3.append("    END IF;\n");
                    sb3.append("END;\n");
                    sb = sb3;
                }
            } else {
                List<SQLStatement> sqlStatementList = new ArrayList<>();
                sqlStatementList.add(sqlStatement);
                sb.append(SQLUtils.toSQLString(sqlStatementList, JdbcConstants.DM).replace("`", "\""));

            }
        }
        String result = sb.toString().trim();
        if (result.charAt(result.length() - 1) != ';') {
            result += ";";
        }
        return result + "\n";
    }
}
