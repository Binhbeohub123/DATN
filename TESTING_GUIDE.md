# PolyCinema — Testing Guide

## Prerequisites

Start both servers before testing:

```bash
# Terminal 1 — Backend (port 8080)
cd backend
./mvnw.cmd spring-boot:run

# Terminal 2 — Frontend (port 5173)
cd frontend
npm run dev
```

App: http://localhost:5173  
API: http://localhost:8080/api

---

## Test Credentials

| Role  | Email               | Password |
|-------|---------------------|----------|
| Admin | admin@cinema.com    | 123456   |
| User  | user@cinema.com     | 123456   |

---

## TASK 1 — Home Page (`/`)

### Expected behavior
- Banner slider auto-rotates every 5s (3 banners visible)
- Movie grid shows 6+ cards in "Đang chiếu" tab
- Movie cards: poster image (or dark gradient + title fallback if no image)
- Clicking any movie card navigates to `/phim/:id`
- "Sắp chiếu" tab loads different movies
- Navbar shows Login/Register when logged out

### Steps
1. Open http://localhost:5173
2. Verify banner loads (background image visible)
3. Verify movies grid populates
4. Hover over a movie card — play overlay appears
5. Click a movie card — should navigate to detail page

### Common issues
- If movies show "No Image" text → **FIXED**: now shows dark gradient with movie title
- If clicking card does nothing → check browser console for router errors

---

## TASK 2 — Booking Flow

### Step 1: Movie Detail (`/phim/:id`)
1. Click any movie card from home
2. Verify: poster, title, genre, duration, cast shown
3. Verify: 7-day date picker loads
4. Verify: showtime cards appear (should show times like 09:00, 14:00, 19:00)
5. Click a showtime → "Đặt vé ngay" button appears
6. Click "Đặt vé ngay"

### Step 2: Seat Selection (`/seat-selection/:showtimeId`)
- Requires login — redirect to `/auth` if not logged in
- Once logged in: 5 rows (A–E), 10 seats each
- Rows D and E are VIP (purple)
- Click seats to select (gold highlight)
- Max 8 seats per booking
- Click "Tiếp tục" to proceed

### Step 3: Combo Page (`/combo`)
- Lists products: Combo 🍿, Food 🍟, Drinks 🥤
- Use +/- to adjust quantity
- Click "Tiếp tục" or "Bỏ qua"

### Step 4: Checkout (`/checkout`)
- Shows order summary (movie, seats, combos)
- Apply promo code: `POLY10` or `FLAT50K`
- Select payment: VNPay, Momo, or ATM
- ATM option goes straight to payment result page
- VNPay/Momo redirect to payment gateway

---

## TASK 3 — Auth Flow

### Login
1. Go to http://localhost:5173/auth
2. Enter `user@cinema.com` / `123456`
3. Click Đăng nhập
4. Expected: redirect to home, navbar shows user avatar + name

### Navbar user menu (logged in)
- Avatar shows user initials (e.g. "NA")
- Dropdown: Hồ sơ, Vé của tôi, Lịch sử GD, Đăng xuất
- Admin users also see "⚙️ Admin Panel"

### Profile Page (`/profile`)
1. Click avatar → "Hồ sơ"
2. Edit name, phone, birthday
3. Click save

### Logout
- Click avatar → Đăng xuất
- Navbar reverts to Login/Register buttons

---

## TASK 4 — Admin Dashboard (`/admin`)

### Access
1. Login as `admin@cinema.com` / `123456`
2. Click avatar → "⚙️ Admin Panel" or navigate to `/admin`

### Dashboard Page
- KPI cards: Vé hôm nay, Doanh thu, Users, Phim đang chiếu
- Revenue chart (7 days)
- Recent bookings table

### Movies Management
1. Click "Quản Lý Phim" in sidebar
2. Table lists all 9 movies
3. Click ✏️ to edit a movie
4. Click "+ Thêm phim" to create new
5. Click 🗑️ to soft-delete

### Other Admin Sections
- 📅 Lịch Chiếu — manage showtimes
- 🎟️ Đặt Vé — view all bookings
- 👥 Khách Hàng — user management (lock/unlock)
- 🏢 Rạp Chiếu — cinema/room management
- 🎁 Khuyến Mãi — promo codes
- 📈 Báo Cáo — revenue reports

---

## API Quick Reference

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `/api/phim/dang-chieu` | GET | ❌ | Now showing movies |
| `/api/phim/sap-chieu` | GET | ❌ | Coming soon movies |
| `/api/phim/{id}` | GET | ❌ | Movie detail |
| `/api/phim/{id}/lich-chieu` | GET | ❌ | Movie schedules |
| `/api/lich-chieu/{id}/ghe-trong` | GET | ❌ | Available seats |
| `/api/banner` | GET | ❌ | Banner list |
| `/api/auth/login` | POST | ❌ | Login → JWT token |
| `/api/auth/register` | POST | ❌ | Register |
| `/api/auth/profile` | GET | ✅ | User profile |
| `/api/dat-ve` | POST | ✅ | Create booking |
| `/api/admin/stats` | GET | 🔐 ADMIN | Dashboard KPIs |
| `/api/admin/phim` | GET | 🔐 ADMIN | All movies |

---

## Fixes Applied

| Issue | Fix |
|---|---|
| Backend fails to start (OAuth2 error) | Added `@ConditionalOnProperty` to `OAuth2SuccessHandler`; excluded OAuth2 autoconfiguration |
| Admin login returns 401 | `DataInitializer` upserts admin user on every startup with correct BCrypt hash |
| Dates serialized as arrays `[2026,6,3]` | Added `JavaTimeModule` + disabled `WRITE_DATES_AS_TIMESTAMPS` in `WebConfig` |
| No future schedules | `DataInitializer` seeds 7-day schedule grid for top movies + rooms + seats |
| Movie cards not clickable | Added `role="button"` + keyboard handler; improved play icon overlay |
| Home page "No Image" text | Replaced with dark gradient + film emoji + movie title |
