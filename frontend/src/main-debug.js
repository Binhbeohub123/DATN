// Minimal test version of main.js
console.log('=== MAIN.JS DEBUG VERSION ===')

// Step 1: Test Vue import
console.log('Step 1: Importing Vue...')
import { createApp } from 'vue'
console.log('✓ Vue imported')

// Step 2: Test Pinia import
console.log('Step 2: Importing Pinia...')
import { createPinia } from 'pinia'
console.log('✓ Pinia imported')

// Step 3: Test App import
console.log('Step 3: Importing App.vue...')
import App from './App.vue'
console.log('✓ App.vue imported')

// Step 4: Test Router import
console.log('Step 4: Importing router...')
import router from './router'
console.log('✓ Router imported')

// Step 5: Create app
console.log('Step 5: Creating app...')
const app = createApp(App)
console.log('✓ App created')

// Step 6: Install Pinia
console.log('Step 6: Installing Pinia...')
app.use(createPinia())
console.log('✓ Pinia installed')

// Step 7: Install Router
console.log('Step 7: Installing Router...')
app.use(router)
console.log('✓ Router installed')

// Step 8: Add error handlers
console.log('Step 8: Adding error handlers...')
app.config.errorHandler = (err, instance, info) => {
  console.error('❌ Vue Error:', err)
  console.error('Info:', info)
  console.error('Stack:', err.stack)
}

app.config.warnHandler = (msg, instance, trace) => {
  console.warn('⚠️ Vue Warning:', msg)
}
console.log('✓ Error handlers added')

// Step 9: Mount app
console.log('Step 9: Mounting app...')
try {
  app.mount('#app')
  console.log('✓✓✓ APP MOUNTED SUCCESSFULLY ✓✓✓')
} catch (error) {
  console.error('❌❌❌ MOUNT FAILED ❌❌❌')
  console.error('Error:', error)
  console.error('Stack:', error.stack)

  // Show error on page
  document.querySelector('#app').innerHTML = `
    <div style="padding: 40px; background: #fee; color: #c00; font-family: monospace; max-width: 800px; margin: 40px auto; border-radius: 8px; border: 2px solid #c00;">
      <h2 style="margin-top: 0;">❌ App Mount Failed</h2>
      <h3>Error:</h3>
      <pre style="background: white; padding: 10px; border-radius: 4px; overflow: auto;">${error.message}</pre>
      <h3>Stack:</h3>
      <pre style="background: white; padding: 10px; border-radius: 4px; overflow: auto; font-size: 11px;">${error.stack}</pre>
      <button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; background: #c00; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 14px;">Reload Page</button>
    </div>
  `
}
