# PolyCinema - API Documentation

## Base URL
```
Development: http://localhost:8080/api
Production: https://api.yoursite.com/api
```

## Authentication
Most endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Response Format
All responses follow this structure:
```json
{
  "success": true,
  "data": { ... },
  "message": "Success message"
}
```

Error responses:
```json
{
  "success": false,
  "error": "Error message",
  "status": 400
}
```

---

## Authentication Endpoints

### 1. Register User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "hoTen": "Nguyen Van A",
  "email": "user@example.com",
  "matKhau": "123456",
  "soDienThoai": "0912345678"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "hoTen": "Nguyen Van A",
    "email": "user@example.com",
    "vaiTro": "ROLE_USER"
  }
}
```

**Status Codes:**
- 200: Success
- 400: Validation error or email already exists
