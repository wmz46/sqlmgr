<template>
  <splitPane :min-percent='20' :default-percent='20' split="vertical">
    <template slot="paneL">
      <div class="fill-height" style="overflow: auto;">
        <DatabasePanel @loaded="dbInfoLoaded" @output="outputSql"></DatabasePanel>
      </div>
    </template>
    <template slot="paneR">
      <ConsolePanel ref="consolePanel"></ConsolePanel>
    </template>
  </splitPane>
</template>
<script>
  import splitPane from 'vue-splitpane'
  import DatabasePanel from '@/components/DatabasePanel'
  import ConsolePanel from '@/components/ConsolePanel'
  export default {
    data() {
      return {}
    },
    methods: {
      dbInfoLoaded(tables) {
        //两个子控件通讯
        this.$refs.consolePanel.editor.setOption('hintOptions', {
          tables: tables
        });
      },
      outputSql(sql) {
        let editor = this.$refs.consolePanel.editor;
        editor.setValue(editor.getValue() + '\n' + sql);
      }
    },
    created() {},
    components: {
      splitPane,
      DatabasePanel,
      ConsolePanel
    }
  }
</script>