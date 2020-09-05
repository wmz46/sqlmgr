package com.iceolive.sqlmgr.controller;

import com.iceolive.sqlmgr.model.BaseResult;
import com.iceolive.sqlmgr.model.vo.SchemaVO;
import com.iceolive.sqlmgr.service.DataBaseService;
import com.iceolive.sqlmgr.service.impl.H2DataBaseServiceImpl;
import com.iceolive.sqlmgr.service.impl.MsSqlDataBaseServiceImpl;
import com.iceolive.sqlmgr.service.impl.MySqlDataBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangmianzhe
 */
@RestController
@RequestMapping("/api")
public class ApiController {
    @RequestMapping(value = "/getAllInfo")
    public BaseResult getAllInfo(String driver, String url, String username, String password) {
        BaseResult result = new BaseResult();
        DataBaseService dataBaseService = getDataBaseService(driver, url, username, password);
        List<SchemaVO> list = dataBaseService.getAllInfo();
        return result.success(list);
    }

    @RequestMapping(value = "/query")
    public BaseResult query(String driver, String url, String username, String password, String sql) {
        BaseResult result = new BaseResult();
        DataBaseService dataBaseService = getDataBaseService(driver, url, username, password);
        List<Map<String, Object>> list = new ArrayList<>();
        for (String s : sql.split(";")) {
            if(StringUtils.isNotBlank(s)) {
                Map<String, Object> map = new HashMap<>();
                map.put("sql", s);
                if (s.trim().toLowerCase().startsWith("select")) {
                    map.put("cmd", "query");
                    map.put("result", dataBaseService.query(s));
                } else {
                    map.put("cmd", "execute");
                    map.put("result", dataBaseService.execute(s));
                }
                list.add(map);
            }
        }
        return result.success(list);
    }

    @RequestMapping(value = "/generateCreateTableSQLScript")
    public BaseResult generateCreateTableSQLScript(String driver, String url, String username, String password, String schemaName, String tableName) {
        BaseResult result = new BaseResult();
        DataBaseService dataBaseService = getDataBaseService(driver, url, username, password);
        String sql = dataBaseService.generateCreateTableSQLScript(schemaName, tableName);
        return result.success(sql);
    }

    @RequestMapping(value = "/generateDropTableSQLScript")
    public BaseResult generateDropTableSQLScript(String driver, String url, String username, String password, String schemaName, String tableName) {
        BaseResult result = new BaseResult();
        DataBaseService dataBaseService = getDataBaseService(driver, url, username, password);
        String sql = dataBaseService.generateDropTableSQLScript(schemaName, tableName);
        return result.success(sql);
    }

    private DataBaseService getDataBaseService(String driver, String url, String username, String password) {
        DataBaseService service = null;
        if ("mysql".equals(driver)) {
            service = new MySqlDataBaseServiceImpl(url, username, password);
        } else if ("h2".equals(driver)) {
            service = new H2DataBaseServiceImpl(url, username, password);
        } else  if ("mssql".equals(driver)) {
            service = new MsSqlDataBaseServiceImpl(url, username, password);
        }else {
            throw new RuntimeException("不支持的数据库类型");
        }
        return service;
    }
}
