<template>
  <div id="app">
    <v-app id="inspire">
      <v-navigation-drawer v-model="drawer" app clipped>
        <Drawer ref="menu" @editConnect="editConnect" @removeConnect="removeConnect"></Drawer>
      </v-navigation-drawer>
      <v-app-bar app clipped-left>
        <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
        <v-toolbar-title>{{title}}</v-toolbar-title>
      </v-app-bar>

      <v-main>
        <router-view></router-view>
      </v-main>

      <v-btn bottom color="success" dark fab fixed left @click="addConnect">
        <v-icon>mdi-plus</v-icon>
      </v-btn>
      <ConnectDialog ref="dialog"></ConnectDialog>
      <v-footer app>
        <span v-html="copyright" style="text-align: right;width: 100%;"></span>
      </v-footer>
    </v-app>
  </div>
</template>

<script>
  import config from '@/plugins/config'
  import ConnectDialog from "@/components/ConnectDialog"
  import Drawer from "@/components/Drawer"
  export default {
    name: 'App',
    data() {
      return {
        drawer: null,
        title: config.title,
        copyright: config.copyright,
      }
    },
    methods: {
      addConnect() {
        this.$refs.dialog.show();
      },
      editConnect(name) {
        this.$refs.dialog.show(name);
      },
      removeConnect(name) {
        this.$refs.dialog.remove(name);
      },
    },
    created() {
      this.$vuetify.theme.dark = true;
    },
    components: {
      ConnectDialog,
      Drawer
    }
  }
</script>