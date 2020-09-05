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
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangmianzhe
 */
public class H2DataBaseServiceImpl  implements DataBaseService {
    private static final String DRIVER_CLASS_NAME = "org.h2.Driver";
    private String url;
    private String username;
    private String password;
    private JdbcTemplate jdbcTemplate;
    private static final String[] NUMERIC_TYPE = new String[]{"TINYINT", "SMALLINT", "MEDIUMINT", "INT", "INTEGER",
            "BIGINT", "FLOAT", "DOUBLE", "DECIMAL"};

    public H2DataBaseServiceImpl(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.jdbcTemplate = JdbcUtil.createJdbcTemplate(DRIVER_CLASS_NAME, url, username, password);
    }
    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public List<Schema> getSchemaList() {
        String sql = "SELECT DISTINCT TABLE_SCHEMA name\n" +
                "FROM INFORMATION_SCHEMA.TABLES;";
        List<Schema> schemaList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Schema.class));
        List<String> sysSchema = new ArrayList<>();
        sysSchema.add("INFORMATION_SCHEMA");
        schemaList = schemaList.stream().filter(m -> !sysSchema.contains(m.getName())).collect(Collectors.toList());
        return schemaList;
    }

    @Override
    public List<Table> getTableList(String schemaName) {
        List<Object> params = new ArrayList<>();
        params.add(schemaName);
        String sql = "SELECT TABLE_NAME name,REMARKS comment\n" +
                "FROM INFORMATION_SCHEMA.TABLES\n" +
                "WHERE TABLE_SCHEMA=?\n"+
                "ORDER BY TABLE_NAME;";
        List<Table> tableList = jdbcTemplate.query(sql, params.toArray(), new BeanPropertyRowMapper<>(Table.class));

        return tableList;
    }

    @Override
    public List<Column> getColumnList(String schemaName, String tableName) {
        List<Object> params = new ArrayList<>();
        params.add(schemaName);
        params.add(tableName);
        params.add(schemaName);
        params.add(tableName);
        String sql = "SELECT COLUMN_NAME name,REMARKS comment,\n" +
                "SUBSTRING(COLUMN_TYPE,0,LOCATE(' ',COLUMN_TYPE)) type,\n" +
                "(CASE WHEN IS_NULLABLE='NO' THEN 0 ELSE 1 END) nullable,\n" +
                "(SELECT COUNT(1) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND COLUMN_NAME=INFORMATION_SCHEMA.COLUMNS.COLUMN_NAME) primaryKey,\n" +
                "(CASE WHEN COLUMN_TYPE like '%NEXT VALUE FOR%' THEN 1 ELSE 0 END) identity,\n" +
                "(CASE WHEN TYPE_NAME='float' OR TYPE_NAME='double' OR TYPE_NAME='decimal' THEN NUMERIC_PRECISION ELSE CHARACTER_MAXIMUM_LENGTH END) length,\n" +
                "(CASE WHEN LOCATE('NEXT VALUE FOR',COLUMN_DEFAULT)=1 THEN null ELSE COLUMN_DEFAULT END) columnDefault,\n" +
                "NUMERIC_SCALE numericScale\n" +
                "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                "WHERE TABLE_SCHEMA=? AND TABLE_NAME=?\n"+
                "order by ORDINAL_POSITION;";
        List<Column> columnList = jdbcTemplate.query(sql, params.toArray(), new BeanPropertyRowMapper<>(Column.class));
        for(Column column : columnList){
            column.setNumericType(Arrays.stream(NUMERIC_TYPE).anyMatch(m -> column.getType().toUpperCase().startsWith(m)));
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
            String sql = MessageFormat.format("create table if not exists `{0}`\n", table.getName());
            sql += "(\n";
            for (int i = 0; i < columnList.size(); i++) {
                Column column = columnList.get(i);
                sql += "    ";
                sql += StringUtils.rightPad(MessageFormat.format("`{0}`", column.getName()), maxNameLength + 2, " ");
                sql += StringUtils.rightPad(" " + column.getType(), maxTypeLength + 1, " ");

                if (column.getIdentity()) {
                    sql += StringUtils.rightPad(" auto_increment", maxDefaultLength + 11+ maxNullLength, " ");
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
            sql+=";";
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
            String sql = MessageFormat.format("drop table if exists `{0}`;\n", table.getName());
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
