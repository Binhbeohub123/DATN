<template>
  <div class="reports-page">
    <transition name="toast"><div v-if="toast.show" :class="['toast',`toast--${toast.type}`]">{{ toast.msg }}</div></transition>

    <!-- Controls -->
    <div class="card controls-card">
      <div class="controls-row">
        <div class="field">
          <label>Từ ngày</label>
          <input v-model="from" type="date" class="ctrl-input" />
        </div>
        <div class="field">
          <label>Đến ngày</label>
          <input v-model="to" type="date" class="ctrl-input" />
        </div>
        <div class="toggle-group">
          <button :class="['tg-btn', groupBy==='day'?'tg-btn--active':'']" @click="groupBy='day';load()">Ngày</button>
          <button :class="['tg-btn', groupBy==='week'?'tg-btn--active':'']" @click="groupBy='week';load()">Tuần</button>
          <button :class="['tg-btn', groupBy==='month'?'tg-btn--active':'']" @click="groupBy='month';load()">Tháng</button>
        </div>
        <button class="btn-primary" @click="load" :disabled="loading">🔍 Xem</button>
        <button class="btn-export" @click="exportExcel" :disabled="exporting">
          <span v-if="exporting">⏳ Đang xuất...</span>
          <span v-else>📥 Xuất Excel</span>
        </button>
      </div>
    </div>

    <!-- Summary KPIs -->
    <div class="kpi-row">
      <div class="kpi-mini" v-for="k in summaryKpis" :key="k.label">
        <div class="kpi-mini-val">{{ k.value }}</div>
        <div class="kpi-mini-lbl">{{ k.label }}</div>
      </div>
    </div>

    <!-- Bar chart (SVG) -->
    <div class="card chart-card">
      <h3>Doanh Thu Theo {{ groupLabel }}</h3>
      <div v-if="loading" class="state-center"><div class="spinner"></div></div>
      <div v-else-if="data.length===0" class="empty-text">Chưa có dữ liệu trong khoảng thời gian này</div>
      <div v-else class="bar-chart-wrap">
        <svg :viewBox="`0 0 ${SVG_W} ${SVG_H}`" class="bar-chart" preserveAspectRatio="xMidYMid meet">
          <!-- y-axis lines -->
          <line v-for="y in yGrid" :key="y" :x1="PAD" :y1="y" :x2="SVG_W-10" :y2="y" stroke="#374151" stroke-width="1"/>
          <!-- bars -->
          <g v-for="(item, i) in chartItems" :key="i">
            <rect
              :x="item.x" :y="item.y" :width="item.w" :height="item.h"
              rx="4" fill="url(#barGrad)"
              class="bar-rect"
            />
            <text :x="item.x+item.w/2" :y="SVG_H-6" text-anchor="middle" font-size="9" fill="#9ca3af">{{ item.label }}</text>
            <text v-if="item.h>20" :x="item.x+item.w/2" :y="item.y-4" text-anchor="middle" font-size="9" fill="#FFD700" font-weight="700">{{ fmtShort(item.val) }}</text>
          </g>
          <defs>
            <linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#FFD700"/>
              <stop offset="100%" stop-color="#B8860B"/>
            </linearGradient>
          </defs>
        </svg>
      </div>
    </div>

    <!-- Data table -->
    <div class="card table-card" v-if="data.length>0">
      <h3>Chi Tiết</h3>
      <div class="table-scroll">
        <table class="data-table">
          <thead><tr><th>Kỳ</th><th>Doanh thu</th></tr></thead>
          <tbody>
            <tr v-for="d in data" :key="d.ngay">
              <td>{{ d.ngay }}</td>
              <td class="td-rev">{{ fmtPrice(d.doanhThu) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import api from '@/services/api'

const SVG_W = 700, SVG_H = 220, PAD = 50, BAR_PAD = 4

const toast = reactive({ show:false, msg:'', type:'success' })
let toastTimer = null
function showToast(msg, type='error') {
  clearTimeout(toastTimer); toast.msg=msg; toast.type=type; toast.show=true
  toastTimer = setTimeout(() => toast.show=false, 3500)
}

// default range: last 30 days
const today = new Date().toISOString().slice(0,10)
const ago30 = new Date(Date.now()-30*86400000).toISOString().slice(0,10)
const from    = ref(ago30)
const to      = ref(today)
const groupBy = ref('day')
const data    = ref([])
const loading = ref(false)
const exporting = ref(false)

const groupLabel = computed(() => groupBy.value==='day'?'Ngày':groupBy.value==='week'?'Tuần':'Tháng')

const summaryKpis = computed(() => {
  const total = data.value.reduce((s,d) => s+Number(d.doanhThu||0), 0)
  const max   = Math.max(...data.value.map(d=>Number(d.doanhThu||0)), 0)
  const days  = data.value.length
  return [
    { label:'Tổng doanh thu', value: fmtPrice(total) },
    { label:'Cao nhất / kỳ', value: fmtPrice(max) },
    { label:'Số kỳ có dữ liệu', value: days },
    { label:'TB / kỳ', value: days ? fmtPrice(total/days) : '—' },
  ]
})

const yGrid = computed(() => {
  return [1,2,3].map(i => PAD + ((SVG_H - PAD*1.5) * (1-i/3)))
})

const chartItems = computed(() => {
  const items = groupedData.value
  if (!items.length) return []
  const maxVal = Math.max(...items.map(d=>Number(d.doanhThu||0)),1)
  const barW = Math.max(4, (SVG_W - PAD - 10) / items.length - BAR_PAD)
  return items.map((d,i) => {
    const val = Number(d.doanhThu||0)
    const h = Math.max(2, ((val/maxVal) * (SVG_H - PAD*1.5)))
    const x = PAD + i * ((SVG_W - PAD - 10)/items.length) + BAR_PAD/2
    const y = SVG_H - PAD/2 - h
    return { x, y, w: barW, h, val, label: labelFor(d.ngay) }
  })
})

const groupedData = computed(() => {
  if (groupBy.value === 'day') return data.value
  const map = {}
  data.value.forEach(d => {
    const key = groupBy.value === 'week'
      ? weekKey(d.ngay)
      : d.ngay?.slice(0,7)
    if (!map[key]) map[key] = { ngay:key, doanhThu:0 }
    map[key].doanhThu += Number(d.doanhThu||0)
  })
  return Object.values(map).sort((a,b) => a.ngay.localeCompare(b.ngay))
})

function weekKey(dateStr) {
  const d = new Date(dateStr)
  const mon = new Date(d); mon.setDate(d.getDate() - d.getDay() + 1)
  return mon.toISOString().slice(0,10)
}
function labelFor(s) {
  if (!s) return ''
  if (groupBy.value==='month') return s.slice(5)        // MM
  const parts = s.split('-')
  return parts.length>=3 ? `${parts[2]}/${parts[1]}` : s
}
function fmtPrice(v) {
  return new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(v||0)
}
function fmtShort(v) {
  if (v >= 1e6) return (v/1e6).toFixed(1)+'M'
  if (v >= 1e3) return (v/1e3).toFixed(0)+'K'
  return String(v)
}

async function load() {
  loading.value=true
  try {
    const r = await api.get('/admin/doanh-thu', { params:{ from:from.value, to:to.value } })
    data.value = Array.isArray(r.data) ? r.data : []
  } catch(e) { showToast('Không tải được dữ liệu báo cáo') }
  finally { loading.value=false }
}

async function exportExcel() {
  exporting.value=true
  try {
    const r = await api.get('/admin/report/export', {
      params:{ from:from.value, to:to.value },
      responseType:'blob'
    })
    const url = URL.createObjectURL(r.data)
    const a = document.createElement('a')
    a.href=url; a.download=`bao-cao-${from.value}_${to.value}.xlsx`
    a.click(); URL.revokeObjectURL(url)
    showToast('Xuất Excel thành công', 'success')
  } catch(e) {
    if (e.response?.status===404) showToast('Tính năng xuất Excel chưa hỗ trợ trên server này')
    else showToast('Lỗi xuất Excel')
  } finally { exporting.value=false }
}

onMounted(load)
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active { transition: opacity 0.3s; }
.toast-enter-from,
.toast-leave-to { opacity: 0; }
</style>
