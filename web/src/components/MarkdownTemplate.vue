<template>
  <div style="display:none">
    # 数据库设计文档<br>
    **数据库名：** {{db.name}}<br>
    | 序号 | 表名 | 说明 |<br>
    | :---: | :---: | :---: |<br>
    <p v-for="(table,i) in db.tables" :key="i">
      | {{i+1}} | [{{table.name}}](#{{table.name}}) | {{table.comment}} |<br>
    </p>
    <div v-for="table in db.tables" :key="table.name">
      ## {{table.name}}<br>
      **说明：** {{table.comment}}<br>
      **数据列：**<br>
      | 序号 | 名称 | 数据类型 | 小数位 | 允许空值 | 主键 | 默认值 | 说明 |<br>
      | :---: | :---: | :---:| :---: | :---: | :---: | :---: | :---: |<br>
      <p v-for="(column,i) in table.columns" :key="i">
        | {{i+1}} | {{column.name}} | {{column.type}} | {{column.numericScale}} | {{column.nullable}} |
        {{column.primaryKey}} | {{column.columnDefault}} |{{column.comment}} |<br>
      </p>
    </div>
  </div>
</template>
<script>
  export default {
    data() {
      return {
        db: {}
      }
    },
    methods: {
      generate(db) {
        this.db = db;
        this.download();
      },
      download() {
        this.$nextTick(() => {
          let filename = "数据库文档-" + this.db.name + ".md";
          let tempDom = document.createElement('div');
          tempDom.innerHTML = this.$el.innerHTML.replace(/<br>/g, "\n");
          let blob = new Blob([tempDom.innerText], {
            type: 'text/x-markdown;charset=utf-8;'
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
    }
  }
</script>