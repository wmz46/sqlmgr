package com.iceolive.sqlmgr.model.vo;

import com.iceolive.sqlmgr.model.Column;
import com.iceolive.sqlmgr.model.Table;
import lombok.Data;

import java.util.List;

/**
 * @author wangmianzhe
 */
@Data
public class TableVO extends Table {
    private List<Column> columns ;
}
