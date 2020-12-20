package com.iceolive.sqlmgr;

import com.iceolive.sqlmgr.service.SqlTranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SqlmgrApplicationTests {

    @Autowired
    SqlTranslationService sqlTranslationService;

    @Test
    void test() {
        String sql = "create table per_region\n" +
                "(\n" +
                "    id               int unsigned auto_increment comment '编号'\n" +
                "        primary key,\n" +
                "    name             varchar(30)                  not null comment '辖区名称{vo}',\n" +
                "    no               varchar(50)                  null comment '辖区no',\n" +
                "    parent_id        int unsigned default 0       null comment '上级编号{vo}',\n" +
                "    fullname         varchar(100)                 null comment '全称{vo}',\n" +
                "    area             double(12, 2) unsigned       null comment '面积{vo}',\n" +
                "    background_color varchar(25)  default ''      null comment '热区背景色{vo}',\n" +
                "    border_color     varchar(25)  default ''      null comment '热区边框颜色{vo}',\n" +
                "    region_type      int unsigned                 not null comment '类型[dict(RegionType)]{vo}',\n" +
                "    border_size      tinyint unsigned             null comment '边框大小{vo}',\n" +
                "    map_level        tinyint unsigned             null comment '地图级别{vo}',\n" +
                "    description      text                         null comment '描述{vo}',\n" +
                "    economic_info    text                         null comment '经济发展情况{vo}',\n" +
                "    society_info     text                         null comment '社会发展情况{vo}',\n" +
                "    about            text                         null comment '辖区概况{vo}',\n" +
                "    img              varchar(255)                 null comment '辖区图片{vo}',\n" +
                "    lonlat           point                        null comment '经纬度{vo,li}',\n" +
                "    geom             multipolygon                 null comment '地图边界{vo,li}',\n" +
                "    seq              int unsigned default 0       null comment '排序{vo}',\n" +
                "    creator          int          default 0       null comment '创建者',\n" +
                "    modifier         int          default 0       null comment '修改者',\n" +
                "    create_time      datetime                     null comment '创建时间',\n" +
                "    update_time      datetime                     null comment '修改时间',\n" +
                "    code             varchar(100) charset utf8mb4 null comment '行政编码{vo}'\n" +
                ")\n" +
                "    comment '辖区表';";
        String result = sqlTranslationService.mysql2dm(sql);
        System.out.println(result);
    }

}
