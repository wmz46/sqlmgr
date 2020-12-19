package com.iceolive.sqlmgr.service.impl;

import com.alibaba.druid.util.JdbcConstants;
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
public class DmDataBaseServiceImpl implements DataBaseService {
    private static final String DRIVER_CLASS_NAME = JdbcConstants.DM_DRIVER;
    private String url;
    private String username;
    private String password;
    private JdbcTemplate jdbcTemplate;
    private static final String[] NUMERIC_TYPE = new String[]{"TINYINT", "SMALLINT", "MEDIUMINT", "INT", "INTEGER",
            "BIGINT", "FLOAT", "DOUBLE", "DECIMAL"};
    private static final String[] STRING_TYPE = new String[]{"CHAR","VARCHAR","VARCHAR2"};

    public DmDataBaseServiceImpl(String url, String username, String password) {
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
        List<Schema> schemaList = new ArrayList<>();
        Schema schema = new Schema();
        schema.setName(this.username);
        schemaList.add(schema);
        return schemaList;
    }

    @Override
    public List<Table> getTableList(String schemaName) {
        String sql = "SELECT TABLE_NAME name,COMMENTS \"comment\" \n" +
                "from USER_TAB_COMMENTS \n" +
                "WHERE table_type = 'TABLE'\n" +
                "ORDER BY TABLE_NAME";
        List<Table> tableList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Table.class));

        return tableList;
    }

    @Override
    public List<Column> getColumnList(String schemaName, String tableName) {
        List<Object> params = new ArrayList<>();
        params.add(tableName);
        params.add(tableName);
        String sql = "SELECT t1.NAME name,\n" +
                "t3.COMMENTS \"comment\",\n" +
                "t1.TYPE$ type,\n" +
                "(CASE WHEN t1.NULLABLE$='N' THEN 0 ELSE 1 END) nullable,\n" +
                "(SELECT COUNT(1) FROM USER_CONS_COLUMNS t4 LEFT JOIN USER_CONSTRAINTS t5 ON t4.CONSTRAINT_NAME=t5.CONSTRAINT_NAME WHERE t5.CONSTRAINT_TYPE = 'P' AND t4.TABLE_NAME=? AND t4.COLUMN_NAME=t1.NAME) primaryKey,\n" +
                "(CASE WHEN t1.info2 =1 THEN 1 ELSE 0 END) \"identity\",\n" +
                "t1.LENGTH$ length,\n" +
                "t1.DEFVAL columnDefault,\n" +
                "t1.SCALE numericScale\n" +
                "FROM SYS.SYSCOLUMNS t1\n" +
                "INNER JOIN SYS.SYSOBJECTS t2 ON t1.ID = t2.ID\n" +
                "LEFT JOIN USER_COL_COMMENTS t3 ON t2.NAME = t3.TABLE_NAME AND t1.NAME = t3.COLUMN_NAME\n" +
                "WHERE  t2.NAME=? AND t2.TYPE$='SCHOBJ' AND t2.SUBTYPE$='UTAB'\n" +
                "ORDER BY t1.COLID;";
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
            String sql = MessageFormat.format("create table  \"{0}\"\n", table.getName());
            sql += "(\n";
            for (int i = 0; i < columnList.size(); i++) {
                Column column = columnList.get(i);
                sql += "    ";
                sql += StringUtils.rightPad(MessageFormat.format("\"{0}\"", column.getName()), maxNameLength + 2, " ");
                String type= column.getType();

                if(Arrays.stream(STRING_TYPE).anyMatch(m ->column.getType().toUpperCase().startsWith(m))){
                    type += "("+column.getLength()+")";
                }else if(type.toUpperCase().equals("DECIMAL")){
                    type += "("+column.getLength()+","+column.getNumericScale()+")";
                }
                sql += StringUtils.rightPad(" " + type, maxTypeLength + 1, " ");

                if (column.getIdentity()) {
                    sql += StringUtils.rightPad(" identity", maxDefaultLength + 11+ maxNullLength, " ");
                } else {
                    String defaultFormat;
                    defaultFormat = " default {0}";
                    sql += StringUtils.rightPad(column.getColumnDefault() != null ? MessageFormat.format(defaultFormat, column.getColumnDefault()) : "", maxDefaultLength + 11, " ");
                    sql += StringUtils.rightPad(column.getNullable() ? " null" : " not null", maxNullLength, " ");
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

            sql+=";";
            //添加表和字段注释
            if (StringUtils.isNotEmpty(table.getComment())) {
                sql += MessageFormat.format("\ncomment on table  \"{0}\" is ''{1}''; ",table.getName(), table.getComment());
            }
            for (int i = 0; i < columnList.size(); i++) {
                Column column = columnList.get(i);
                if (StringUtils.isNotEmpty(column.getComment())) {
                    sql += MessageFormat.format("\ncomment on column \"{0}\".\"{1}\" is ''{2}''; ",table.getName(),column.getName(), column.getComment());
                }
            }
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
            String sql = MessageFormat.format("drop table \"{0}\";\n", table.getName());
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
