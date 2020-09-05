<template>
  <splitPane @resize="resize" split="horizontal">
    <template slot="paneL">
      <v-toolbar flat>
        <v-btn icon @click="query">
          <v-icon>fa-play</v-icon>
        </v-btn>
      </v-toolbar>
      <v-textarea label="SQL" id="sql_input" v-model="sql" hint="请输入sql"></v-textarea>
    </template>
    <template slot="paneR">
      <v-card>
        <v-tabs v-model="tab" dark>
          <v-tab v-for="(result,i) in results" :key="i">
            {{ result.sql }}
          </v-tab>
        </v-tabs>
        <v-tabs-items v-model="tab">
          <v-tab-item v-for="(result,i) in results" :key="i">
            <v-data-table :height="tableHeight" fixed-header show-select v-if="result.cmd=='query'"
              :headers="getHeaders(result.result)" :items="result.result">
            </v-data-table>
            <v-card flat v-if="result.cmd!='query'">
              <v-card-text>{{result.result}}行受影响</v-card-text>
            </v-card>
          </v-tab-item>
        </v-tabs-items>
      </v-card>
    </template>
  </splitPane>
</template>
<script>
  import splitPane from 'vue-splitpane'
  import config from '@/plugins/config'
  import ConnectConfig from '@/plugins/connectConfig'
  import CodeMirror from '@/plugins/codeMirror'
  export default {
    components: {
      splitPane
    },
    data() {
      return {
        name: this.$route.params.name,
        sql: '',
        tab: null,
        results: [],
        editor: null,
        tableHeight: 100
      }
    },
    watch: {
      $route() {
        this.name = this.$route.params.name
      }
    },
    mounted() {
      let that = this;
      let editor = CodeMirror.fromTextArea(document.getElementById('sql_input'), {
        mode: 'text/x-mysql',
        lineNumbers: true,
        indentWithTabs: true,
        smartIndent: true,
        matchBrackets: true,
        theme: "ayu-dark",
        autofocus: true,
        extraKeys: {
          'Alt': 'autocomplete',
          'Ctrl-Enter': function () {
            that.query()
          }
        },
      });
      editor.setSize('100%');
      that.editor = editor;
      let paneRHeight = document.querySelector(".splitter-paneR.horizontal").offsetHeight;
      this.tableHeight = (paneRHeight - 110);
    },
    methods: {

      getHeaders(list) {
        let headers = [];
        if (list.length > 0) {
          for (let key in list[0]) {
            headers.push({
              text: key,
              value: key
            })
          }
        }
        return headers;
      },
      query() {
        let that = this;
        let connect = ConnectConfig.getConnects()[this.name]
        let data = new FormData();
        that.sql = that.editor.getValue();
        let sql = that.sql;
        if (that.editor.getSelection()) {
          sql = that.editor.getSelection();
        }
        data.append("driver", connect.driver)
        data.append("url", connect.url)
        data.append("username", connect.username)
        data.append("password", connect.password)
        data.append("sql", sql)
        that.results = [];
        this.$axios.post(config.apiUrl + '/api/query', data).then(res => {
          if (res.data.success) {
            that.results = res.data.data;
          } else {
            that.$throw(res.data.message);
          }
        }).catch(err => {
          that.$throw(err)
        })
      },
      resize() {
        let paneLHeight = document.querySelector(".splitter-paneL.horizontal").offsetHeight;
        let paneRHeight = document.querySelector(".splitter-paneR.horizontal").offsetHeight;
        document.querySelectorAll(".v-data-table__wrapper").forEach((item) => {
          item.style.height = (paneRHeight - 100) + "px";
        })
        this.editor.setSize('100%', (paneLHeight - 95) + 'px');

      },
    }
  }
</script>