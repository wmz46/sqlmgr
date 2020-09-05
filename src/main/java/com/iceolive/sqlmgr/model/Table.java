package com.iceolive.sqlmgr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangmianzhe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    /**
     * 表名
     */
    private String name;
    /**
     * 表说明
     */
    private String comment;
}
