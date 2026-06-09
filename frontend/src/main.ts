/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Composables
import { createApp } from 'vue'

import { createPinia } from 'pinia'

// Plugins
import { registerPlugins } from '@/plugins'

// Components
import App from './App.vue'

// Styles
import 'unfonts.css'
import router from './routers/index'

const app = createApp(App)

app.use(createPinia()) 

registerPlugins(app)

app.use(router).mount('#app')
