import Vue from 'vue'
import App from './App.vue'
import vuetify from '@/plugins/vuetify' // path to vuetify export
import axios from 'axios'
import moment from 'moment'
import router from '@/plugins/router'


Vue.prototype.$axios = axios
Vue.prototype.$moment = moment
Vue.prototype.$throw = (err) => {
  let message = err && err.response && err.response.data && err.response.data.message || err.message || err
  alert(message)
}
Vue.config.productionTip = false
new Vue({
  vuetify,
  router,
  render: h => h(App),
}).$mount('#app')