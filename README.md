# TOOLBARTER

> **Borrow Nearby. Build Together.**
> *The Hyper-Local Peer-to-Peer Tool Barter Platform*

TOOLBARTER is a B.Tech CSE full-stack capstone project for hyper-local peer-to-peer tool lending and geo-discovery. The platform connects neighbours who have idle tools with those who need specialised equipment for short, scheduled windows — backed by real-time Haversine proximity ranking, MySQL spatial coordinates, conflict-free scheduling, and a rule-based trust-scoring system.

**Project Owner:** Ch. Rithwik · Roll No: 24EG105P07 · B.Tech CSE

---

## Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21 LTS, Spring Boot 3.3.4, Spring Data JPA, Spring Security, JWT (JJWT 0.12.6), BCrypt, Maven |
| **Database** | MySQL 8.0+ with `POINT` spatial columns (SRID 4326), `SPATIAL INDEX`, Flyway-style `schema.sql` |
| **Frontend** | React 18 + Vite 5, React Router v6, Axios, Leaflet.js, HTML5 Canvas radar sweep |
| **Testing** | JUnit 5, Spring Boot Test, Mockito, H2 in-memory (MODE=MySQL) |

---

## Project Structure

```
TOOLBARTER/
├── backend/                  # Spring Boot 3.x Maven project (Java 21)
│   ├── .env.example
│   └── src/
│       ├── main/java/com/toolbarter/
│       │   ├── config/       # SecurityConfig, DataInitializer
│       │   ├── controller/   # AuthController, UserController, ItemController, …
│       │   ├── dto/          # Request + Response DTOs (no JPA entities exposed)
│       │   ├── entity/       # JPA entities (User, Item, AvailabilitySlot, …)
│       │   ├── enums/        # Role, ItemStatus, BorrowStatus, TrustLevel, …
│       │   ├── exception/    # GlobalExceptionHandler, custom exceptions
│       │   ├── repository/   # Spring Data JPA repositories
│       │   ├── security/     # JwtTokenProvider, JwtAuthFilter, UserDetailsService
│       │   └── service/      # Business logic (GeoProximity, Trust, Scheduling, …)
│       └── test/             # 36 unit tests, all passing
├── frontend/                 # React + Vite SPA
│   └── src/
│       ├── api/              # apiClient.js (Axios), config.js (endpoint map)
│       ├── components/       # Navbar, Footer, RadarMap, LocationPicker, …
│       ├── context/          # AuthContext (JWT storage, role switching)
│       └── pages/            # LandingPage, DiscoverPage, Dashboards, …
├── README.md
└── .gitignore
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 21 LTS |
| Maven | 3.9+ |
| Node.js | 18+ (tested on 24.x) |
| npm | 9+ |
| MySQL | 8.0+ (running as a service) |

---

## Quick Start

### 1 — Clone & set up environment

```powershell
cd TOOLBARTER
cp backend/.env.example backend/.env   # then fill in DB_PASSWORD and JWT_SECRET
```

### 2 — Create the database

```sql
CREATE DATABASE toolbarter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3 — Start the backend

```powershell
# PowerShell
$env:DB_PASSWORD = "your_mysql_root_password"
$env:JWT_SECRET  = "ChangeThisToASecureBase64KeyOfAtLeast256Bits"

cd backend
mvn spring-boot:run
```

Spring Boot auto-runs `src/main/resources/db/schema.sql` on first start. Hibernate validates the schema and `DataInitializer` seeds four demo accounts if the `users` table is empty.

Backend runs at: **http://localhost:8080**

### 4 — Start the frontend

```powershell
cd frontend
npm install          # first time only
npm run dev
```

Frontend runs at: **http://localhost:5173** (Vite proxies `/api` → `http://localhost:8080`)

---

## Environment Variables

Copy `backend/.env.example` to `backend/.env` and fill in:

| Variable | Required | Description |
|---|---|---|
| `DB_HOST` | No | Default: `localhost` |
| `DB_PORT` | No | Default: `3306` |
| `DB_NAME` | No | Default: `toolbarter` |
| `DB_USERNAME` | No | Default: `root` |
| `DB_PASSWORD` | **Yes** | Your MySQL root password |
| `JWT_SECRET` | **Yes** | Base64-encoded key ≥ 256 bits |
| `JWT_EXPIRATION_MS` | No | Default: `86400000` (24 h) |

---

## Demo Accounts (seeded automatically)

| Name | Email | Password | Role | Trust |
|---|---|---|---|---|
| Alice Reddy | alice@toolbarter.com | password123 | Owner + Borrower | 45 pts — Reliable Lender |
| Bob Kumar | bob@toolbarter.com | password123 | Owner + Borrower | 78 pts — Trusted Neighbour |
| Carol Nair | carol@toolbarter.com | password123 | Borrower | 20 pts — Regular Borrower |
| David Singh | david@toolbarter.com | password123 | Owner | 105 pts — Street Champion |

---

## API Endpoints

### Auth
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, returns JWT |

### Users
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/users/{id}` | Any | Get user profile |
| PUT | `/api/users/{id}` | Owner | Update profile |
| GET | `/api/users/{id}/statistics` | Any | Stats + trust level |
| GET | `/api/users/{id}/reviews` | Any | Reviews received |

### Items
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/items` | Public | List/search items |
| GET | `/api/items/nearby` | Public | Haversine radius search |
| GET | `/api/items/{id}` | Public | Item detail |
| POST | `/api/items` | User | Create item |
| PUT | `/api/items/{id}` | Owner | Update item |
| DELETE | `/api/items/{id}` | Owner | Unlist item |
| GET | `/api/items/my` | User | My items |

### Slots & Requests
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/items/{id}/slots` | Owner | Add availability slot |
| GET | `/api/items/{id}/slots` | Any | View slots |
| POST | `/api/items/{id}/requests` | User | Submit borrow request |
| PUT | `/api/requests/{id}/approve` | Owner | Approve |
| PUT | `/api/requests/{id}/decline` | Owner | Decline |
| PUT | `/api/requests/{id}/pickup` | Owner | Confirm pickup |
| PUT | `/api/requests/{id}/return` | Owner | Confirm return |
| PUT | `/api/requests/{id}/cancel` | Borrower | Cancel |

### Reviews & Stats
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/requests/{id}/review` | Participant | Submit review |
| GET | `/api/stats/summary` | Public | Neighbourhood impact |

---

## Trust Scoring System

Trust points accumulate on each user's account and unlock higher trust levels.

### Point Awards

| Event | Owner | Borrower |
|---|---|---|
| Return confirmed (undisputed) | +5 | +5 |
| 5-star review received | +2 | +2 |
| 4-star review received | +1 | +1 |
| 3-star review received | 0 | 0 |
| 2-star review received | −2 | −2 |
| 1-star review received | −4 | −4 |
| Late return (>2 h overdue) | 0 | −3 |

Completion points are **idempotent** — awarded only once per borrow via the `pointsAwarded` flag on `BorrowRequest`.

### Trust Levels

| Level | Min Points | Min Borrows | Extra Conditions |
|---|---|---|---|
| **New Neighbour** | 0 | < 2 | — |
| **Regular Borrower** | 15 | ≥ 2 | — |
| **Reliable Lender** | 35 | ≥ 5 | ≥ 1 item listed |
| **Trusted Neighbour** | 65 | ≥ 10 | Avg rating ≥ 4.2 |
| **Street Champion** | 100 | ≥ 20 | Avg rating ≥ 4.6 |

---

## Geo-Discovery

Items are stored with `latitude` / `longitude` `DOUBLE` columns **and** a `POINT` spatial column (SRID 4326) for future native spatial query support. The current implementation uses the **Haversine formula** in `GeoProximityService.java` to calculate real distances in Java after an approximate bounding-box DB pre-filter, avoiding MySQL SRID 4326 axis-order issues.

---

## Running Tests

```powershell
cd backend
mvn test
```

Expected output: **Tests run: 36, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS**

Test suites:
- `GeoProximityServiceTest` — Haversine accuracy
- `SchedulingServiceTest` — double-booking prevention
- `TrustServiceTest` — point awards, idempotency, level recalc
- `AuthServiceTest` — register, login, duplicate email
- `ItemServiceTest` — CRUD, ownership, proximity filter
- `BorrowRequestServiceTest` — lifecycle state machine, auth
- `ReviewServiceTest` — eligibility, duplicate prevention, trust linkage
- `ToolbarterApplicationTests` — full Spring context loads against H2

---

## Frontend Build (Production)

```powershell
cd frontend
npm run build          # outputs to frontend/dist/
npm run preview        # serves dist/ locally on port 4173
```

---

## Security Notes

- Passwords are hashed with BCrypt (strength 12).
- JWTs are signed with HMAC-SHA256. The secret must be ≥ 256-bit entropy.
- All sensitive endpoints verify the authenticated user's identity server-side.
- Never commit `.env` or any file containing real credentials.
- `DB_PASSWORD` and `JWT_SECRET` are **always** read from environment variables at runtime.

---

## Known Limitations & Future Work

- Email notifications (borrow confirmations, reminders) — placeholder, not implemented.
- Photo upload — currently URL-only; S3/local storage integration planned.
- Push notifications — browser Notification API integration planned.
- Admin dashboard — moderation, dispute resolution UI not yet implemented.
- Real-time updates — WebSocket / SSE for live request status changes planned.
