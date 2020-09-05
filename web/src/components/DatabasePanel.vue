<template>
  <v-list>
    <v-list-group :value="false" v-for="(db,k) in dblist" :key="k">
      <template v-slot:activator>
        <v-list-item-icon>
          <v-icon>fa-database</v-icon>
        </v-list-item-icon>
        <v-list-item-content>
          <v-list-item-title>{{db.name}}</v-list-item-title>
        </v-list-item-content>
        <v-list-item-action>
          <v-menu bottom left>
            <template v-slot:activator="{ on, attrs }">
              <v-btn icon v-bind="attrs" v-on="on">
                <v-icon>mdi-dots-vertical</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item @click="generateDoc(db)">
                <v-list-item-icon>
                  <v-icon>fa-file-code-o</v-icon>
                </v-list-item-icon>
                <v-list-item-title>生成数据库文档</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </v-list-item-action>
      </template>
      <v-list-item>
        <v-text-field v-model="db.keyword" prepend-icon="mdi-database-search" @input="search(db)" placeholder="请输入表名">
        </v-text-field>
      </v-list-item>
      <v-list-group no-action sub-group :value="false" v-for="(table, i) in db.tables" :key="i">
        <template v-slot:activator>
          <v-list-item>
            <v-list-item-icon>
              <v-icon>fa-table</v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title> {{table.name}}</v-list-item-title>
              <v-list-item-subtitle>{{table.comment }}</v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-menu bottom left>
                <template v-slot:activator="{ on, attrs }">
                  <v-btn icon v-bind="attrs" v-on="on">
                    <v-icon>mdi-dots-vertical</v-icon>
                  </v-btn>
                </template>
                <v-list>
                  <v-list-item @click="generateCreateTableSQLScript(db.name,table.name)">
                    <v-list-item-icon>
                      <v-icon>fa-file-code-o</v-icon>
                    </v-list-item-icon>
                    <v-list-item-title>生成Create脚本</v-list-item-title>
                  </v-list-item>
                  <v-list-item @click="generateDropTableSQLScript(db.name,table.name)">
                    <v-list-item-icon>
                      <v-icon>fa-file-code-o</v-icon>
                    </v-list-item-icon>
                    <v-list-item-title>生成Drop脚本</v-list-item-title>
                  </v-list-item>
                  <v-list-item @click="generateInsert(db.name,table)">
                    <v-list-item-icon>
                      <v-icon>fa-file-code-o</v-icon>
                    </v-list-item-icon>
                    <v-list-item-title>生成Insert脚本</v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-menu>
            </v-list-item-action>
          </v-list-item>
        </template>
        <v-list-item v-for="(col, j) in table.columns" :key="j" link>
          <v-list-item-icon v-if="col.primaryKey">
            <v-icon>fa-key</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title v-text="col.name"></v-list-item-title>
            <v-list-item-subtitle>{{col.comment}}</v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-content>
            <v-list-item-subtitle>{{col.type}}<span v-if="col.columnDefault!=null"> =
                {{col.numericType?col.columnDefault:"'"+col.columnDefault+"'"}}</span>
            </v-list-item-subtitle>
            <v-list-item-subtitle v-if="col.identity">(auto_increment)
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </v-list-group>
    </v-list-group>
    <InsertTemplate ref='insert'></InsertTemplate>
    <MarkdownTemplate ref='md'></MarkdownTemplate>
  </v-list>
</template>
<script>
  import config from '@/plugins/config'
  import InsertTemplate from '@/components/InsertTemplate'
  import MarkdownTemplate from '@/components/MarkdownTemplate'
  import ConnectConfig from "@/plugins/connectConfig"
  export default {
    data() {
      return {
        dblist: [],
        name: this.$route.params.name
      }
    },
    watch: {
      $route() {
        this.name = this.$route.params.name
        this.loadData();
      }
    },
    components: {
      InsertTemplate,
      MarkdownTemplate,
    },
    methods: {
      loadData() {
        let that = this;
        let connect = ConnectConfig.getConnects()[this.name]
        let data = new FormData();
        data.append("driver", connect.driver)
        data.append("url", connect.url)
        data.append("username", connect.username)
        data.append("password", connect.password)
        this.$axios.post(config.apiUrl + '/api/getAllInfo', data).then(res => {
          if (res.data.success) {
            that.dblist = res.data.data
            let tables = {};
            that.dblist.forEach((schema) => {
              schema.tables.forEach((table) => {
                let arr = []
                table.columns.forEach((col) => {
                  arr.push(col.name);
                })
                tables[schema.name + '.' + table.name] = arr;
              })
            })
            that.$emit('loaded', tables);

          } else {
            that.$throw(res.data.message);
          }
        }).catch(err => {
          that.$throw(err)
        })
      },
      search(db) {
        let list = document.querySelectorAll(".v-list-group.v-list-group--no-action.v-list-group--sub-group");
        //无法控制v-list-group双向绑定隐藏
        if (db.keyword != null && db.keyword.length > 0) {
          db.tables.forEach((table, i) => {
            if (table.name.toLowerCase().indexOf(db.keyword) >= 0) {
              list[i].style.display = '';
            } else {
              list[i].style.display = 'none';
            }
          })
        } else {
          db.tables.forEach((table, i) => {
            list[i].style.display = '';
          })
        }
      },
      generateDoc(db) {
        this.$refs.md.generate(db);
      },
      generateInsert(schemaName, table) {
        this.$refs.insert.generate(this.name, schemaName, table);
      },
      generateDropTableSQLScript(schemaName, tableName) {
        let that = this;
        let connect = ConnectConfig.getConnects()[this.name]
        let data = new FormData();
        data.append("driver", connect.driver)
        data.append("url", connect.url)
        data.append("username", connect.username)
        data.append("password", connect.password)
        data.append("schemaName", schemaName)
        data.append("tableName", tableName)
        this.$axios.post(config.apiUrl + '/api/generateDropTableSQLScript', data).then(res => {
          if (res.data.success) {
            let sql = that.editor.getValue();
            sql += '\n' + res.data.data;
            this.editor.setValue(sql);
          } else {
            that.$throw(res.data.message);
          }
        }).catch(err => {
          that.$throw(err)
        })
      },
      generateCreateTableSQLScript(schemaName, tableName) {
        let that = this;
        let connect = ConnectConfig.getConnects()[this.name]
        let data = new FormData();
        data.append("driver", connect.driver)
        data.append("url", connect.url)
        data.append("username", connect.username)
        data.append("password", connect.password)
        data.append("schemaName", schemaName)
        data.append("tableName", tableName)
        this.$axios.post(config.apiUrl + '/api/generateCreateTableSQLScript', data).then(res => {
          if (res.data.success) {
            let sql = that.editor.getValue();
            sql += '\n' + res.data.data;
            this.editor.setValue(sql);
          } else {
            that.$throw(res.data.message);
          }
        }).catch(err => {
          that.$throw(err)
        })
      },
    },
    created() {
      this.loadData()
    },
  }
</script>