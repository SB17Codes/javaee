# Deploy on Koyeb (Free)

This project can run for free on Koyeb with **persistence** using a Koyeb PostgreSQL database.

## 1) Push to GitHub
- Create a GitHub repo and push this project.

## 2) Create a PostgreSQL database on Koyeb
1. Koyeb → **Create Service** → **Database** → PostgreSQL.
2. Note the connection string or host/port/user/password.

## 3) Create the Koyeb web service
1. Go to Koyeb → **Create App** → **GitHub**.
2. Select your repository and the default branch.
3. Service type: **Web service**.
4. Build: **Dockerfile** (auto‑detected).
5. Port: `8080` (Koyeb reads `PORT` automatically).

## 4) Environment variables
Add one of the following (in order of preference):
- `DATABASE_URL=postgres://USER:PASS@HOST:PORT/DB`
  - The app auto‑converts this to a JDBC URL at runtime.
- or `JDBC_DATABASE_URL=jdbc:postgresql://HOST:PORT/DB`
  - plus `DB_USER` / `DB_PASS` if not embedded.

Optional:
- `MAPBOX_TOKEN=your_mapbox_token` (needed for maps + routing)

## 5) Deploy
Click **Deploy**. Once live, open the public URL.

## Notes
- If no database variables are provided, the app falls back to a local H2 file DB.
