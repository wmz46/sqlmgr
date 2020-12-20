package com.iceolive.sqlmgr.service;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.iceolive.sqlmgr.model.Column;
import org.apache.commons.lang3.StringUtils;
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

    public String mysql2dm(String sql) {
        StringBuilder sb = new StringBuilder();
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        for (SQLStatement sqlStatement : statementList) {
            if (sqlStatement instanceof MySqlCreateTableStatement) {
                MySqlCreateTableStatement mySqlCreateTableStatement = (MySqlCreateTableStatement) sqlStatement;
                StringBuilder sb2 = new StringBuilder();
                //建表语句
                sb.append("create table ");
                String tableName = mySqlCreateTableStatement.getTableSource().toString().replace("`", "\"");
                sb.append(tableName);
                sb.append("(\n");
                for (SQLTableElement sqlTableElement : mySqlCreateTableStatement.getTableElementList()) {
                    if (sqlTableElement instanceof SQLColumnDefinition) {
                        SQLColumnDefinition sqlColumnDefinition = (SQLColumnDefinition) sqlTableElement;
                        String columnName = sqlColumnDefinition.getColumnName().replace("`", "\"");
                        //添加字段注释
                        if (sqlColumnDefinition.getComment() != null) {
                            sb2.append(MessageFormat.format("comment on column {0}.{1} is {2};\n", tableName, columnName, sqlColumnDefinition.getComment().toString()));
                        }
                        sb.append("    " + columnName);
                        String dataType = sqlColumnDefinition.getDataType().getName().toLowerCase();
                        switch (dataType){
                            case "tinyint":
                                sb.append(" tinyint");
                                break;
                            case "int":
                                sb.append(" int");
                                break;
                            case "float":
                            case "double":
                                List args =  sqlColumnDefinition.getDataType().getArguments();
                                if( args.size()==2){
                                    sb.append(" numeric("+ args.get(0)+","+args.get(1)+")");
                                }else{
                                    sb.append(" double");
                                }
                                break;
                            case "point" :
                            case "multipolygon":
                                //空间类型
                                sb.append(" st_"+dataType);
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
                    sb.append(MessageFormat.format("comment on table  {0} is {1};\n", tableName, mySqlCreateTableStatement.getComment().toString()));
                }
                sb.append(sb2);
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
