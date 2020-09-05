import Vue from 'vue'
import VueRouter from 'vue-router'
import MainPanel from '@/components/MainPanel'
Vue.use(VueRouter)
export default new VueRouter({
  routes: [{
    path: "/",

  }, {
    path: "/db/:name",
    name:"db",
    component: MainPanel
  }]
})