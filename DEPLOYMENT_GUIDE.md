# PolyCinema - Deployment Guide

## Quick Start (Development)

### 1. Database Setup
```bash
# Create database
mysql -u root -p
CREATE DATABASE cinema_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit

# Load sample data
mysql -u root -p cinema_db < sample_data.sql
mysql -u root -p cinema_db < sample_data_ghe.sql
```

### 2. Backend Setup
```bash
cd backend

# Configure application.properties
# Update: spring.datasource.username, spring.datasource.password

# Run backend
.\mvnw.cmd spring-boot:run
```

Backend runs at: `http://localhost:8080`

### 3. Frontend Setup
```bash
cd frontend

# Install dependencies (first time only)
npm install

# Run development server
npm run dev
```

Frontend runs at: `http://localhost:5173`

### 4. Test Login
- **Admin**: `admin@cinema.com` / `123456`
- **User**: `user@cinema.com` / `123456`

---

## Production Deployment

### Backend (Spring Boot)

#### Build JAR
```bash
cd backend
.\mvnw.cmd clean package -DskipTests
```

Output: `backend/target/cinema-backend-0.0.1-SNAPSHOT.jar`

#### Run in Production
```bash
java -jar target/cinema-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### Production Configuration
Create `application-prod.properties`:
```properties
# Database
spring.datasource.url=jdbc:mysql://your-prod-db:3306/cinema_db?useSSL=true
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# VNPay
vnpay.tmn_code=${VNPAY_TMN_CODE}
vnpay.hash_secret=${VNPAY_HASH_SECRET}
vnpay.return_url=https://yoursite.com/payment-return

# CORS
spring.web.cors.allowed-origins=https://yoursite.com

# Logging
logging.level.root=WARN
logging.level.com.cinema=INFO
```

#### Deploy Options

**Option 1: Docker**
Create `Dockerfile`:
```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t cinema-backend .
docker run -p 8080:8080 -e DB_USER=root -e DB_PASSWORD=secret cinema-backend
```

**Option 2: Systemd Service (Linux)**
Create `/etc/systemd/system/cinema-backend.service`:
```ini
[Unit]
Description=PolyCinema Backend
After=mysql.service

[Service]
User=cinema
WorkingDirectory=/opt/cinema
ExecStart=/usr/bin/java -jar /opt/cinema/cinema-backend.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

Enable:
```bash
sudo systemctl enable cinema-backend
sudo systemctl start cinema-backend
```

**Option 3: Nginx Reverse Proxy**
```nginx
server {
    listen 80;
    server_name api.yoursite.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

### Frontend (Vue 3)

#### Build for Production
```bash
cd frontend
npm run build
```

Output: `frontend/dist/` directory

#### Deploy Options

**Option 1: Nginx Static Hosting**
```nginx
server {
    listen 80;
    server_name yoursite.com;
    root /var/www/cinema/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

Copy files:
```bash
sudo cp -r dist/* /var/www/cinema/dist/
```

**Option 2: Vercel**
```bash
npm install -g vercel
vercel --prod
```

Set environment variable in Vercel:
- `VITE_API_BASE_URL` = `https://api.yoursite.com`

**Option 3: Netlify**
Create `netlify.toml`:
```toml
[build]
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

Deploy:
```bash
npm install -g netlify-cli
netlify deploy --prod
```

**Option 4: Docker**
Create `Dockerfile`:
```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## Environment Variables

### Backend
```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=cinema_db
DB_USER=root
DB_PASSWORD=yourpassword

# Security
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000

# VNPay
VNPAY_TMN_CODE=your-tmn-code
VNPAY_HASH_SECRET=your-hash-secret
VNPAY_RETURN_URL=https://yoursite.com/payment-return

# CORS
ALLOWED_ORIGINS=https://yoursite.com
```

### Frontend
```bash
# API URL
VITE_API_BASE_URL=https://api.yoursite.com
```

---

## Database Migration

### Backup
```bash
mysqldump -u root -p cinema_db > backup_$(date +%Y%m%d).sql
```

### Restore
```bash
mysql -u root -p cinema_db < backup_20260602.sql
```

### Production Database Setup
```sql
-- Create production user
CREATE USER 'cinema_prod'@'%' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON cinema_db.* TO 'cinema_prod'@'%';
FLUSH PRIVILEGES;

-- Enable SSL (recommended)
ALTER USER 'cinema_prod'@'%' REQUIRE SSL;
```

---

## SSL/HTTPS Setup

### Let's Encrypt (Certbot)
```bash
# Install certbot
sudo apt install certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d yoursite.com -d api.yoursite.com

# Auto-renewal
sudo certbot renew --dry-run
```

---

## Monitoring & Logs

### Backend Logs
```bash
# View logs
tail -f /var/log/cinema-backend.log

# Log rotation
sudo nano /etc/logrotate.d/cinema-backend
```

Config:
```
/var/log/cinema-backend.log {
    daily
    rotate 7
    compress
    missingok
    notifempty
}
```

### Frontend Access Logs (Nginx)
```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### Database Monitoring
```sql
-- Check connections
SHOW PROCESSLIST;

-- Slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

---

## Performance Optimization

### Backend
```properties
# Connection pooling
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# JVM options
-Xms512m -Xmx2g -XX:+UseG1GC
```

### Frontend
```javascript
// Lazy loading routes
const MovieDetail = () => import('./pages/MovieDetailPage.vue')

// Image optimization
<img loading="lazy" :src="poster" />
```

### Database
```sql
-- Add indexes
CREATE INDEX idx_lich_chieu_phim ON lich_chieu(phim_id);
CREATE INDEX idx_lich_chieu_ngay ON lich_chieu(ngay_chieu);
CREATE INDEX idx_dat_ve_user ON dat_ve(nguoi_dung_id);

-- Query cache (MySQL 8+ removed, use Redis instead)
```

---

## Security Checklist

- [ ] Change default admin password
- [ ] Use strong JWT secret (256-bit)
- [ ] Enable HTTPS/SSL
- [ ] Set secure CORS policy
- [ ] Use environment variables (never commit secrets)
- [ ] Enable SQL prepared statements (already done in Spring)
- [ ] Rate limiting on login endpoint
- [ ] Database user has minimal privileges
- [ ] Regular security updates (`mvnw dependency:updates`)
- [ ] Input validation on all forms
- [ ] XSS protection (Vue auto-escapes)
- [ ] CSRF protection (Spring Security)

---

## Backup Strategy

### Automated Backup Script
```bash
#!/bin/bash
# backup-cinema.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/cinema"

# Database backup
mysqldump -u cinema_prod -p$DB_PASSWORD cinema_db | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# Keep last 7 days
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +7 -delete

# Upload to cloud (optional)
# aws s3 cp $BACKUP_DIR/db_$DATE.sql.gz s3://your-bucket/backups/
```

Setup cron:
```bash
crontab -e
# Add: 0 2 * * * /opt/scripts/backup-cinema.sh
```

---

## Troubleshooting Production

### Backend won't start
```bash
# Check Java version
java -version

# Check port availability
netstat -tuln | grep 8080

# Verify database connection
mysql -h your-db-host -u cinema_prod -p

# Check application logs
journalctl -u cinema-backend -f
```

### Frontend 502 Bad Gateway
```bash
# Check backend is running
curl http://localhost:8080/api/phim/dang-chieu

# Check nginx config
sudo nginx -t

# Restart nginx
sudo systemctl restart nginx
```

### Database connection pool exhausted
```sql
-- Check open connections
SELECT * FROM information_schema.processlist;

-- Kill idle connections
KILL <process_id>;
```

```properties
# Increase pool size
spring.datasource.hikari.maximum-pool-size=20
```

### High memory usage
```bash
# Check Java heap
jmap -heap <PID>

# Generate heap dump
jmap -dump:live,format=b,file=heap.bin <PID>

# Analyze with VisualVM or Eclipse MAT
```

---

## Rollback Procedure

### Backend Rollback
```bash
# Stop current version
sudo systemctl stop cinema-backend

# Restore previous JAR
cp /opt/cinema/backups/cinema-backend-v1.0.jar /opt/cinema/cinema-backend.jar

# Restart
sudo systemctl start cinema-backend
```

### Database Rollback
```bash
# Restore from backup
mysql -u root -p cinema_db < /backups/cinema/db_20260601_020000.sql
```

### Frontend Rollback
```bash
# Restore previous build
cp -r /var/www/cinema/backups/dist-v1.0/* /var/www/cinema/dist/
```

---

## CI/CD Pipeline (GitHub Actions)

Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy PolyCinema

on:
  push:
    branches: [ main ]

jobs:
  deploy-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Build with Maven
        run: |
          cd backend
          ./mvnw clean package -DskipTests
      
      - name: Deploy to server
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_KEY }}
          source: "backend/target/*.jar"
          target: "/opt/cinema/"

  deploy-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Build frontend
        run: |
          cd frontend
          npm install
          npm run build
      
      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v20
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.ORG_ID }}
          vercel-project-id: ${{ secrets.PROJECT_ID }}
```

---

## Health Check Endpoints

### Backend Health Check
```java
@RestController
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
```

### Monitoring Setup (Prometheus + Grafana)
```properties
# application.properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

---

## Load Balancing (Optional)

### Nginx Load Balancer
```nginx
upstream cinema_backend {
    least_conn;
    server backend1:8080;
    server backend2:8080;
    server backend3:8080;
}

server {
    location /api {
        proxy_pass http://cinema_backend;
    }
}
```

---

## Contact & Support

- **Documentation**: `/TESTING_GUIDE.md`
- **API Docs**: `http://localhost:8080/swagger-ui.html` (if Swagger enabled)
- **Issues**: GitHub Issues or internal tracking

---

**Last Updated:** June 2, 2026  
**Version:** 1.0.0
