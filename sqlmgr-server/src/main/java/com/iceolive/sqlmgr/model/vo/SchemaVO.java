package com.iceolive.sqlmgr.model.vo;

import com.iceolive.sqlmgr.model.Schema;
import com.iceolive.sqlmgr.model.Table;
import lombok.Data;

import java.util.List;

/**
 * @author wangmianzhe
 */
@Data
public class SchemaVO extends Schema {
    private List<TableVO> tables;
}
