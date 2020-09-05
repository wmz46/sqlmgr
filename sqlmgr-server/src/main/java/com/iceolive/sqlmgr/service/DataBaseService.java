package com.iceolive.sqlmgr.service;

import com.iceolive.sqlmgr.model.Column;
import com.iceolive.sqlmgr.model.Schema;
import com.iceolive.sqlmgr.model.Table;
import com.iceolive.sqlmgr.model.vo.SchemaVO;

import java.util.List;
import java.util.Map;

/**
 * @author wangmianzhe
 */
public interface DataBaseService {
    /**
     * 获取版本信息
     *
     * @return
     */
    String getVersion();

    /**
     * 获取所有库，只获取信息
     *
     * @return
     */
    List<Schema> getSchemaList();

    /**
     * 获取单个库的所有表
     *
     * @param schemaName
     * @return
     */
    List<Table> getTableList(String schemaName);

    /**
     * 获取单个表的所有列
     *
     * @param schemaName
     * @param tableName
     * @return
     */
    List<Column> getColumnList(String schemaName, String tableName);

    /**
     * 获取所有信息
     *
     * @return
     */
    List<SchemaVO> getAllInfo();

    /**
     * 生成建表语句
     *
     * @param schemaName
     * @param tableName
     * @return
     */
    String generateCreateTableSQLScript(String schemaName, String tableName);

    /**
     * 生成删表语句
     *
     * @param schemaName
     * @param tableName
     * @return
     */
    String generateDropTableSQLScript(String schemaName, String tableName);

    /**
     * 查询sql
     *
     * @param sql
     * @return
     */
    List<Map<String, Object>> query(String sql);

    /**
     * 执行sql
     *
     * @param sql
     * @return
     */
    int execute(String sql);

    /**
     * 获取主键
     * @return
     */
    List<String> getPrimaryKey(String schemaName, String tableName);

}

