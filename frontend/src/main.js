import { createSSRApp } from 'vue'
import App from './App.vue'
import uviewPlus from 'uview-plus'
import { createPinia } from 'pinia'
import request from './utils/request'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  
  app.use(uviewPlus)
  app.use(pinia)
  
  app.config.globalProperties.$request = request
  
  return {
    app,
    pinia
  }
}
