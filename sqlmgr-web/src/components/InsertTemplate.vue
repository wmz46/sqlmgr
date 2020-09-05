<template>
  <div style="display:none">
    <p v-for="(item,i) in datas" :key="i">
      insert into {{table.name}}(
      <template v-for="(column,i) in table.columns">
        {{i>0?", ":""}}{{column.name}}
      </template>) values(
      <template v-for="(column,i) in table.columns">
        {{i>0?", ":""}}{{formatValue(column,item[column.name])}}
      </template>);<br>
    </p>
  </div>
</template>
<script>
  import config from '@/plugins/config'
  import connectConfig from '@/plugins/connectConfig'
  export default {
    data() {
      return {
        table: {},
        datas: [],
      }
    },
    methods: {
      formatValue(column, value) {
        if (value == null) {
          return "null";
        } else if (column.numericType) {
          return value;
        } else if (column.type.toLowerCase().indexOf("datetime") == 0) {
          return "'" + this.$moment(value).format("YYYY-MM-DD HH:mm:ss.SSS") + "'";
        } else if (column.type.toLowerCase().indexOf("date") == 0) {
          return "'" + this.$moment(value).format("YYYY-MM-DD") + "'";
        } else {
          return "'" + value + "'"
        }
      },
      generate(connectName, schemaName, table) {
        let that = this;
        let sql = "SELECT * FROM " + schemaName + "." + table.name;
        let connect = connectConfig.getConnects()[connectName]
        let data = new FormData();
        data.append("driver", connect.driver)
        data.append("url", connect.url)
        data.append("username", connect.username)
        data.append("password", connect.password)
        data.append("sql", sql)
        this.$axios.post(config.apiUrl + '/api/query', data).then(res => {
          if (res.data.success) {
            that.table = table;
            that.datas = res.data.data[0].result;
            that.download();
          } else {
            that.$throw(res.data.message);
          }
        }).catch(err => {
          that.$throw(err)
        })


      },
      download() {
        this.$nextTick(() => {
          let tempDom = document.createElement('div');
          tempDom.innerHTML = this.$el.innerHTML.replace(/<br>/g, "\n");
          console.info(tempDom.innerText)
          let filename = "insert-" + this.table.name + ".sql";
          let blob = new Blob([tempDom.innerText], {
            type: 'text/plain;charset=utf-8;'
          });
          if (navigator.msSaveOrOpenBlob) {
            navigator.msSaveOrOpenBlob(blob, filename);
          } else {
            let url = URL.createObjectURL(blob);
            let downloadLink = document.createElement('a');
            downloadLink.href = url;
            downloadLink.download = filename;
            document.body.appendChild(downloadLink);
            downloadLink.click();
            document.body.removeChild(downloadLink);
            URL.revokeObjectURL(url);
          }
        });
      }
    },
  }
</script>