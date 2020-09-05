package com.iceolive.sqlmgr.service.impl;

import com.iceolive.sqlmgr.model.Column;
import com.iceolive.sqlmgr.model.Schema;
import com.iceolive.sqlmgr.model.Table;
import com.iceolive.sqlmgr.model.vo.SchemaVO;
import com.iceolive.sqlmgr.model.vo.TableVO;
import com.iceolive.sqlmgr.service.DataBaseService;
import com.iceolive.sqlmgr.util.JdbcUtil;
import com.iceolive.sqlmgr.util.MapperUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangmianzhe
 */
public class MsSqlDataBaseServiceImpl implements DataBaseService {
    @Override
    public String getVersion() {
        String sql = "SELECT @@VERSION;";
        String version = jdbcTemplate.queryForObject(sql, String.class);
        return version;
    }

    private static final String[] NUMERIC_TYPE = new String[]{"BIT", "SMALLINT", "MEDIUMINT", "INT", "INTEGER",
            "BIGINT", "FLOAT", "DOUBLE", "DECIMAL"};
    private static final String DRIVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String url;
    private String username;
    private String password;
    private JdbcTemplate jdbcTemplate;

    public MsSqlDataBaseServiceImpl(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.jdbcTemplate = JdbcUtil.createJdbcTemplate(DRIVER_CLASS_NAME, url, username, password);
    }


    @Override
    public List<Schema> getSchemaList() {
        List<Schema> schemaList = new ArrayList<>();
        Schema schema = new Schema();
        schema.setName("dbo");
        schemaList.add(schema);
        return schemaList;
    }

    @Override
    public List<Table> getTableList(String schemaName) {
        String sql = "SELECT t.name, CONVERT(NVARCHAR(100), ISNULL(e.value, '')) comment\n" +
                "FROM SYS.TABLES t\n" +
                "LEFT JOIN sys.extended_properties e\n" +
                "ON (t.object_id = e.major_id AND e.minor_id = 0)\n" +
                "ORDER BY t.name";
        List<Table> tableList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Table.class));
        return tableList;
    }

    @Override
    public List<Column> getColumnList(String schemaName, String tableName) {
        List<Object> params = new ArrayList<>();
        params.add(tableName);
        params.add(tableName);
        String sql = "WITH indexCTE AS\n" +
                "         (\n" +
                "             SELECT ic.column_id,\n" +
                "                    ic.index_column_id,\n" +
                "                    ic.object_id\n" +
                "             FROM sys.indexes idx\n" +
                "                      INNER JOIN sys.index_columns ic ON idx.index_id = ic.index_id AND idx.object_id = ic.object_id\n" +
                "             WHERE idx.object_id = OBJECT_ID(?)\n" +
                "               AND idx.is_primary_key = 1\n" +
                "         )\n" +
                "select col.name,\n" +
                "\n" +
                "       prop.value                              comment,\n" +
                "       systype.name                            type,\n" +
                "       (IIF(col.is_nullable = 0, 0, 1))        nullable,\n" +
                "       (IIF(indexCTE.column_id IS NULL, 0, 1)) primaryKey,\n" +
                "       (IIF(col.is_identity = 0, 0, 1))        [identity],\n" +
                "       (\n" +
                "           case\n" +
                "               when systype.name = 'nvarchar' and col.max_length > 0 then col.max_length / 2\n" +
                "               when systype.name = 'nchar' and col.max_length > 0 then col.max_length / 2\n" +
                "               when systype.name = 'ntext' and col.max_length > 0 then col.max_length / 2\n" +
                "               when systype.name = 'decimal' then col.precision\n" +
                "               else col.max_length\n" +
                "               end\n" +
                "           )                                   length,\n" +
                "       com.text                                    columnDefault,\n" +
                "       cast(col.scale as int)                  numericScale\n" +
                "FROM sys.columns col\n" +
                "         INNER JOIN sys.types systype\n" +
                "                    ON col.system_type_id = systype.system_type_id and col.user_type_id = systype.user_type_id\n" +
                "         left JOIN sys.extended_properties prop on col.object_id = prop.major_id and col.column_id = prop.minor_id\n" +
                "         LEFT JOIN indexCTE ON col.column_id = indexCTE.column_id AND col.object_id = indexCTE.object_id\n" +
                "         LEFT JOIN sys.syscomments com ON col.default_object_id = com.id\n" +
                "WHERE col.object_id = OBJECT_ID(?)\n" +
                "ORDER BY col.column_id";
        List<Column> columnList = jdbcTemplate.query(sql, params.toArray(), new BeanPropertyRowMapper<>(Column.class));
        for (Column column : columnList) {
            String columnDefault = column.getColumnDefault();
            if (columnDefault != null) {
                if (columnDefault.startsWith("((") || columnDefault.startsWith("('")) {
                    column.setColumnDefault(columnDefault.substring(2, columnDefault.length() - 2));
                }
            }
            column.setNumericType(Arrays.stream(NUMERIC_TYPE).anyMatch(m -> column.getType().toUpperCase().startsWith(m)));
            String[] WithoutLengthType = new String[]{"bit", "datetime", "int", "money", "float"};
            if ("decimal".equals(column.getType())) {
                column.setType(column.getType() + "(" + column.getLength() + ", " + column.getNumericScale() + ")");
            } else if (!Arrays.stream(WithoutLengthType).anyMatch(m -> column.getType().toLowerCase().startsWith(m))) {
                column.setType(column.getType() + "(" + column.getLength() + ")");
            }
        }
        return columnList;
    }

    @Override
    public List<SchemaVO> getAllInfo() {
        List<Schema> schemaList = getSchemaList();
        List<SchemaVO> schemaVOList = MapperUtil.map(schemaList, SchemaVO.class);
        for (SchemaVO schemaVO : schemaVOList) {
            List<Table> tableList = getTableList(schemaVO.getName());
            List<TableVO> tableVOList = MapperUtil.map(tableList, TableVO.class);
            for (int i = 0; i < tableVOList.size(); i++) {
                TableVO tableVO = tableVOList.get(i);
                List<Column> columnList = getColumnList(schemaVO.getName(), tableVO.getName());
                tableVO.setColumns(columnList);
            }
            schemaVO.setTables(tableVOList);
        }
        return schemaVOList;
    }

    @Override
    public String generateCreateTableSQLScript(String schemaName, String tableName) {
        Table table = getTableList(schemaName).stream().filter(m -> m.getName().equals(tableName)).findFirst().orElse(null);
        if (table != null) {

            List<Column> columnList = getColumnList(schemaName, tableName);
            int maxNameLength = columnList.stream().max(Comparator.comparing(m -> m.getName().length())).get().getName().length();
            int maxTypeLength = columnList.stream().max(Comparator.comparing(m -> m.getType().length())).get().getType().length();
            String maxDefault = columnList.stream().max(Comparator.comparing(m -> m.getColumnDefault() == null ? 0 : m.getColumnDefault().length())).get().getColumnDefault();
            int maxDefaultLength = maxDefault != null ? maxDefault.length() : 0;
            int maxNullLength = columnList.stream().anyMatch(m -> !m.getNullable()) ? 9 : 5;
            String sql = MessageFormat.format("create table [{0}]\n", table.getName());
            sql += "(\n";
            for (int i = 0; i < columnList.size(); i++) {
                Column column = columnList.get(i);
                sql += "    ";
                sql += StringUtils.rightPad(MessageFormat.format("[{0}]", column.getName()), maxNameLength + 2, " ");
                sql += StringUtils.rightPad(" " + column.getType(), maxTypeLength + 1, " ");

                if (column.getIdentity()) {
                    sql += StringUtils.rightPad(" identity", maxDefaultLength + 9 + maxNullLength, " ");
                } else {
                    String defaultFormat;
                    if (column.getNumericType()) {
                        defaultFormat = " default {0}";
                    } else {
                        defaultFormat = " default ''{0}''";
                    }
                    sql += StringUtils.rightPad(column.getColumnDefault() != null ? MessageFormat.format(defaultFormat, column.getColumnDefault()) : "", maxDefaultLength + 11, " ");
                    sql += StringUtils.rightPad(column.getNullable() ? " null" : " not null", maxNullLength, " ");
                }
                if (StringUtils.isNotEmpty(column.getComment())) {
                    sql += MessageFormat.format(" comment ''{0}''", column.getComment());
                }
                if (column.getPrimaryKey()) {
                    sql += "\n        primary key";
                }
                if (i < columnList.size() - 1) {
                    sql += ",";
                }
                sql += "\n";
            }
            sql += ")";

            if (StringUtils.isNotEmpty(table.getComment())) {
                sql += MessageFormat.format("\n    comment ''{0}''", table.getComment());
            }
            sql += ";";
            //todo 如果有索引怎么生成索引脚本
            return sql;
        } else {
            throw new RuntimeException(MessageFormat.format("表名[{0}]不存在", tableName));
        }
    }

    @Override
    public String generateDropTableSQLScript(String schemaName, String tableName) {
        Table table = getTableList(schemaName).stream().filter(m -> m.getName().equals(tableName)).findFirst().orElse(null);
        if (table != null) {
            String sql = MessageFormat.format("drop table [{0}];\n", table.getName());
            return sql;
        } else {
            throw new RuntimeException(MessageFormat.format("表名[{0}]不存在", tableName));
        }
    }

    @Override
    public List<Map<String, Object>> query(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public int execute(String sql) {
        return jdbcTemplate.update(sql);
    }

    @Override
    public List<String> getPrimaryKey(String schemaName, String tableName) {
        List<Column> columnList = getColumnList(schemaName, tableName);
        return columnList.stream().filter(m -> m.getPrimaryKey()).collect(Collectors.toList()).stream().map(m -> m.getName()).collect(Collectors.toList());

    }
}
