# LEVIA Backend (Slice 1: auth)

## Run on your phone (AndroidIDE terminal / Termux-like, Node 18+)
    cd backend
    cp .env.example .env     # then edit .env with nano
    npm install
    npm start
Test: `curl http://localhost:3000/health`

## Environment variables
DATABASE_URL, JWT_ACCESS_SECRET (>=32 chars), REFRESH_TOKEN_DAYS, CORS_ORIGINS, APP_BASE_URL,
SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS, MAIL_FROM. Never commit `.env`.

## Deploy on Render
1. Push this `backend/` folder to a GitHub repo (separate from the FreeLLMAPI repo).
2. Render > New > Web Service > pick the repo. Root directory: `backend`.
3. Build command: `npm install`  Start command: `npm start`
4. Add the environment variables above in Render > Environment.
5. Check `https://YOUR-SERVICE.onrender.com/health`

## Endpoints (base: /api/v1)
| Method | Path | Auth | Body |
|---|---|---|---|
| POST | /auth/register | no | email, username, displayName, password |
| POST | /auth/login | no | email, password |
| POST | /auth/refresh | no | refreshToken |
| POST | /auth/logout | no | refreshToken |
| POST | /auth/logout-all | Bearer | - |
| POST | /auth/change-password | Bearer | currentPassword, newPassword |
| POST | /auth/forgot-password | no | email |
| POST | /auth/reset-password | no | token, newPassword |
| GET | /me | Bearer | - |

Success: `{ "success": true, "data": ... }`  Error: `{ "success": false, "error": { "code", "message" } }`
Register returns `data.user` and `data.tokens` (`accessToken`, `refreshToken`). No email verification step.

## Quick test
    curl -X POST localhost:3000/api/v1/auth/register -H 'Content-Type: application/json' \
      -d '{"email":"a@b.com","username":"alice","displayName":"Alice","password":"hello1234"}'
