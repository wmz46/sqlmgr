package com.iceolive.sqlmgr.model;

import lombok.Data;
 
/**
 * @author wangmianzhe
 */
@Data
public class Column {
    /**
     * 字段名
     */
    private String name;
    /**
     * 字段说明
     */
    private String comment;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 可否为空
     */
    private Boolean nullable;
    /**
     * 是否主键
     */
    private Boolean primaryKey;
    /**
     * 是否自增
     */
    private Boolean identity;
    /**
     * 长度
     */
    private Integer length;
    /**
     * 小数位
     */
    private  Integer numericScale;
    /**
     * 默认值
     */
    private String columnDefault;
    /**
     * 是否数值类型
     */
    private Boolean numericType;

}
