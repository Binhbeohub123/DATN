# Staff Authentication — Phase 1 (RQ74/RQ75)

## Login Approach

**Chosen: Option (b) — Separate `POST /api/staff/login` endpoint**

### Why

1. **Clear role rejection**: The endpoint validates credentials via the existing `AuthService.login()`, then additionally checks the JWT's `VaiTro` claim. Non-staff accounts receive a `403` with message *"Tài khoản không có quyền nhân viên"* — impossible to achieve cleanly by reusing the generic `/api/auth/login` without modifying it.

2. **Frontend separation**: Staff have a dedicated `/staff/login` page and route group (`/staff/*`). A separate API endpoint keeps the staff login flow self-contained.

3. **No duplication**: `StaffController.staffLogin()` delegates all credential validation to `AuthService.login()`. Only the role check is added — zero password/hashing logic was duplicated.

4. **Admin compatibility**: Admins (VaiTro = 'admin') can also use the staff login and access staff-only routes, since `@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")` is used throughout.

## Deliverables

| File | Change |
|---|---|
| `migrations/V2__staff_checkin_columns.sql` | New — adds `NhanVienId`, `TrangThaiCheckIn`, `ThoiGianCheckIn`, `NhanVienCheckInId`, `MaQR_Hash` index to `DatVe` |
| `backend/.../entity/DatVe.java` | Updated — new JPA fields with `@ManyToOne` FK relationships to `NguoiDung` |
| `backend/.../controller/StaffController.java` | New — `POST /api/staff/login`, `POST /api/staff/logout`, `GET /api/staff/profile`, `GET /api/staff/ping` |
| `backend/.../config/SecurityConfig.java` | Updated — `.requestMatchers("/api/staff/login").permitAll()` added before staff role guard |
| `frontend/src/view/StaffLoginPage.vue` | New — staff login UI |
| `frontend/src/view/StaffDashboardPage.vue` | New — staff landing page with welcome message + placeholder menu links (POS, QR check-in, shift report) |
| `frontend/src/router/index.js` | Updated — staff routes + navigation guard redirects unauthenticated/wrong-role users to `/staff/login`; OAuth & `/auth` redirects branch by role for staff |

## Staff Logout

JWT is stateless. Logout is handled by:
- **Backend**: `POST /api/staff/logout` endpoint (currently confirms action; can be extended with token blacklisting).
- **Frontend**: `authStore.logout()` clears `localStorage` token + user, then redirects to `/staff/login`.

## How to Test

1. Create a staff account in `NguoiDung` with `VaiTro = 'staff'`.
2. Navigate to `/staff/login` and log in with the staff email/password.
3. Verify `/staff/dashboard` is accessible.
4. Try logging in with a customer account — expect 403 error message.
5. After logout, verify `/staff/dashboard` redirects back to `/staff/login`.