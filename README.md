# Partner Backend — Health Portal Hub

Production-ready Spring Boot 3.3 backend for the Health Portal Hub partner app.  
Supports the **Admin** panel and all three **Mobile** provider roles (Doctor, Pharmacy, Lab).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6 + JJWT 0.12.6 |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | SQLite 3 (`partner.db`) |
| Build | Maven |
| Utilities | Lombok |

---

## Prerequisites

- Java 17+ (JDK)
- Maven 3.8+

---

## Setup & Run

```bash
# 1. Navigate to the backend folder
cd Partner-Backend

# 2. Build the project (skip tests on first run)
mvn clean package -DskipTests

# 3. Run the application
mvn spring-boot:run
```

Or run the fat JAR directly:

```bash
java -jar target/partner-backend-1.0.0.jar
```

The server starts on **http://localhost:8080**.

SQLite database file `partner.db` is created automatically in the working directory on first run.

---

## Default Admin Account

Seeded automatically on startup:

| Field | Value |
|---|---|
| Email | `admin@healthwallet.pk` |
| Password | `admin123` |

---

## Environment / Configuration

All settings live in `src/main/resources/application.properties`.

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `spring.datasource.url` | `jdbc:sqlite:partner.db` | SQLite file path |
| `app.jwt.secret` | (long string) | JWT HMAC-SHA256 signing secret |
| `app.jwt.expiration-ms` | `86400000` | Token lifetime (24 h) |
| `app.upload.dir` | `uploads` | Directory for uploaded documents |

---

## Folder Structure

```
Partner-Backend/
├── pom.xml
└── src/main/
    ├── resources/
    │   └── application.properties
    └── java/com/partner/backend/
        ├── PartnerBackendApplication.java
        ├── DataInitializer.java           ← seeds default admin
        ├── common/
        │   ├── entity/                    ← 20 JPA entities + enums
        │   ├── repository/                ← Spring Data repos
        │   ├── security/                  ← JWT, SecurityConfig
        │   ├── exception/                 ← GlobalExceptionHandler
        │   └── util/                      ← ApiResponse, ResponseWrapper, FileStorageUtil
        ├── admin/
        │   ├── controller/
        │   ├── service/
        │   └── dto/
        └── mobile/
            ├── auth/
            ├── doctor/
            ├── pharmacy/
            └── lab/
```

---

## API Reference

All responses follow the envelope:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

Paginated responses use:

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "last": false
  }
}
```

### Authentication

All protected routes require:
```
Authorization: Bearer <token>
```

---

### Auth Endpoints (public)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/admin/login` | Admin login → JWT |
| `POST` | `/api/auth/provider/login` | Provider login → JWT |
| `POST` | `/api/auth/provider/otp/send` | Send OTP to phone |
| `POST` | `/api/auth/provider/otp/verify` | Verify OTP |
| `GET` | `/api/healthz` | Health check |

#### Admin Login Example
```bash
curl -X POST http://localhost:8080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@healthwallet.pk","password":"admin123"}'
```

#### Provider Login Example
```bash
curl -X POST http://localhost:8080/api/auth/provider/login \
  -H "Content-Type: application/json" \
  -d '{"email":"doctor@example.com","password":"password123"}'
```

---

### Admin Endpoints (`ADMIN` role required)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/admin/dashboard/stats` | Platform KPIs |
| `GET` | `/api/admin/patients` | All patients (paginated) |
| `GET` | `/api/admin/doctors` | Doctors list (`?status=PENDING`) |
| `POST` | `/api/admin/doctors` | Add doctor |
| `GET` | `/api/admin/doctors/{id}` | Doctor detail |
| `PUT` | `/api/admin/doctors/{id}/approve` | Approve doctor |
| `PUT` | `/api/admin/doctors/{id}/reject` | Reject doctor |
| `GET` | `/api/admin/pharmacies` | Pharmacies list |
| `POST` | `/api/admin/pharmacies` | Add pharmacy |
| `GET` | `/api/admin/pharmacies/{id}` | Pharmacy detail |
| `PUT` | `/api/admin/pharmacies/{id}/approve` | Approve |
| `PUT` | `/api/admin/pharmacies/{id}/reject` | Reject |
| `GET` | `/api/admin/labs` | Labs list |
| `POST` | `/api/admin/labs` | Add lab |
| `GET` | `/api/admin/labs/{id}` | Lab detail |
| `PUT` | `/api/admin/labs/{id}/approve` | Approve |
| `PUT` | `/api/admin/labs/{id}/reject` | Reject |
| `GET` | `/api/admin/earnings` | Platform earnings summary |
| `GET` | `/api/admin/earnings/monthly` | Monthly breakdown |
| `GET` | `/api/admin/earnings/providers` | Per-provider (`?type=DOCTOR`) |

---

### Mobile — Doctor (`DOCTOR` role)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/mobile/doctor/register` | Self-registration → JWT |
| `GET` | `/api/mobile/doctor/dashboard` | Stats + today's appointments |
| `GET` | `/api/mobile/doctor/appointments` | Appointment list |
| `PUT` | `/api/mobile/doctor/appointments/{id}/start` | Start consultation |
| `GET` | `/api/mobile/doctor/consultation/{appointmentId}` | Fetch consultation |
| `POST` | `/api/mobile/doctor/consultation` | Save consultation + prescriptions |
| `GET` | `/api/mobile/doctor/availability` | Get availability |
| `PUT` | `/api/mobile/doctor/availability` | Update availability |
| `GET` | `/api/mobile/doctor/profile` | Get profile |
| `PUT` | `/api/mobile/doctor/profile` | Update profile |
| `GET` | `/api/mobile/doctor/earnings` | Earnings summary |
| `GET` | `/api/mobile/doctor/pharma-rewards` | Pharma rewards |
| `GET` | `/api/mobile/doctor/notifications` | Notifications |

---

### Mobile — Pharmacy (`PHARMACY` role)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/mobile/pharmacy/register` | Self-registration → JWT |
| `GET` | `/api/mobile/pharmacy/dashboard` | KPIs |
| `GET` | `/api/mobile/pharmacy/inventory` | Inventory (`?query=panadol`) |
| `POST` | `/api/mobile/pharmacy/inventory` | Add medicine |
| `PUT` | `/api/mobile/pharmacy/inventory/{id}` | Update / restock |
| `DELETE` | `/api/mobile/pharmacy/inventory/{id}` | Remove item |
| `GET` | `/api/mobile/pharmacy/orders` | Orders |
| `PUT` | `/api/mobile/pharmacy/orders/{id}/status` | Update order status |
| `GET` | `/api/mobile/pharmacy/wallet` | Wallet balance |
| `GET` | `/api/mobile/pharmacy/profile` | Get profile |
| `PUT` | `/api/mobile/pharmacy/profile` | Update profile |

---

### Mobile — Lab (`LAB` role)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/mobile/lab/register` | Self-registration → JWT |
| `GET` | `/api/mobile/lab/dashboard` | KPIs |
| `GET` | `/api/mobile/lab/tests` | Test catalogue (`?query=blood`) |
| `POST` | `/api/mobile/lab/tests` | Add test |
| `PUT` | `/api/mobile/lab/tests/{id}` | Update test |
| `DELETE` | `/api/mobile/lab/tests/{id}` | Remove test |
| `GET` | `/api/mobile/lab/appointments` | Lab appointments |
| `PUT` | `/api/mobile/lab/appointments/{id}/status` | Confirm / complete |
| `GET` | `/api/mobile/lab/wallet` | Wallet balance |
| `GET` | `/api/mobile/lab/profile` | Get profile |
| `PUT` | `/api/mobile/lab/profile` | Update profile |

---

## Security Design

- JWT signed with HS256, 24 h expiry.
- Token payload: `sub` (email), `role`, `providerId`.
- `/api/auth/**` and `/api/healthz` are public.
- `/api/admin/**` requires `ROLE_ADMIN`.
- `/api/mobile/doctor/**` requires `ROLE_DOCTOR`.
- `/api/mobile/pharmacy/**` requires `ROLE_PHARMACY`.
- `/api/mobile/lab/**` requires `ROLE_LAB`.
- Registration endpoints (`POST /api/mobile/*/register`) are public to allow self-onboarding.
- Platform commission is **5%** applied at the earnings level.

---

## Connecting the Frontend

In `lib/api-client-react/src/custom-fetch.ts`:

```typescript
import { setBaseUrl, setAuthTokenGetter } from './custom-fetch';

setBaseUrl('http://localhost:8080');
setAuthTokenGetter(() => AsyncStorage.getItem('token'));
```

---

## Notes

- **OTP**: The `OtpService` logs the OTP to the console (`[OTP] Sending OTP XXXX to phone ...`). Wire a real SMS gateway (e.g., Twilio, Jazz) before production.
- **File uploads**: Documents are stored under the `uploads/` directory. Configure `app.upload.dir` to an absolute path in production.
- **Database**: SQLite is single-writer; for multi-instance deployments, switch to PostgreSQL by updating the JDBC URL and dialect in `application.properties`.
