# sqlmgr
一个基于springboot的数据库管理工具
## 一、主要功能
- 1.表结构查询
- 2.支持导出数据库文档(md格式) 模板文件:MarkdownTemplate.vue
- 3.支持导出单表的insert文件(sql格式) 模板文件:InsertTemplate.vue
- 4.支持生成单表建表脚本
- 5.支持mysql,h2,sqlserver,达梦数据库
- 6.上述功能不支持视图、索引和存储过程
- 7.支持mysql脚本转达梦脚本
 
## 二、使用的技术
### 前端
- vue
- vue-cli
- vuetify
- codemirror
### 后端
- springboot

## 三、使用说明
### 目录结构
- sqlmgr-server 后端项目 
- sqlmgr-web 前端项目
### 1.生成前端网页
- 1.1 切换到sqlmgr-web目录，该目录为前端项目
- 1.2 执行`npm i `安装依赖
- 1.3 执行`npm run build` 打包，sqlmgr-web/vue.config.js里面已经配置了输出目录为java项目的静态资源文件目录。
### 2.调试或打包后端项目

## 四、操作流程
- 1.按使用说明操作后，运行后端项目，会自动打开网页（推荐使用chrome浏览器）。
![](./doc/1.png)  
- 2.点击网页左下角的加号按钮添加连接，由于后端没有独立数据库，连接配置这些都是存储在网站的localstorage。如在其他电脑上使用，请重新添加连接。
![](./doc/2.png)
- 3.添加后在左侧边栏会增加一个菜单，点击菜单连接数据库，可在右侧进行查询等操作。  
![](./doc/3.png)

