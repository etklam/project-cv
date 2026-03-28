# CapRover Deployment Guide

## Your CapRover Instance
- URL: `https://captain.rnsj.913555.xyz`
- Password: `Ihave2jj`

---

## What is CapRover?

CapRover is a PaaS (Platform as a Service) that simplifies deployment:
- Automatic SSL certificates (Let's Encrypt)
- Git-based deployment (push to deploy)
- One-click apps (PostgreSQL, Redis, etc.)
- Web dashboard management

---

## Quick Deploy (10 minutes)

### Step 1: Push Code to Git Repository

```bash
# Commit and push your code
git add .
git commit -m "Ready for CapRover deployment"
git push origin main
```

### Step 2: Create PostgreSQL Database

1. Log in to `https://captain.rnsj.913555.xyz` with password `Ihave2jj`
2. Go to **Apps** tab
3. Click **One-Click Apps/Apps**
4. Find and click **PostgreSQL**
5. Configure:
   - **App Name**: `project-cv-db`
   - **Container HTTP Port**: leave blank (internal only)
   - **App Definition**:
     ```json
     {
       "schemaVersion": 2,
       "dockerCompose": {
         "services": {
           "postgres": {
             "image": "postgres:16-alpine",
             "volumes": [
               {
                 "source": "postgres-data",
                 "target": "/var/lib/postgresql/data",
                 "type": "volume"
               }
             ],
             "environment": {
               "POSTGRES_DB": "postgres",
               "POSTGRES_USER": "postgres",
               "POSTGRES_PASSWORD": "changeme"
             }
           }
         },
         "volumes": {
           "postgres-data": {}
         }
       }
     }
     ```
6. **IMPORTANT**: Change `"POSTGRES_PASSWORD": "changeme"` to a strong password
7. Click **Create**

> Save the password you set - you'll need it for the backend app!

### Step 3: Create Backend App

1. Go to **Apps** → **Create New App**
2. **App Name**: `project-cv-backend`
3. Click **Create**
4. Open the app and go to **Deployment** tab

5. **Deployment Method**: Select **Docker Compose (git)**

6. Configure Git:
   | Field | Value |
   |-------|-------|
   | Repository URL | Your GitHub/GitLab repo URL |
   | Branch | `main` |
   | Compose Path | `docker-compose.caprover.yml` |

7. Go to **Environment Variables** tab, add:
   | Key | Value |
   |-----|-------|
   | `POSTGRES_PASSWORD` | (your PostgreSQL password from Step 2) |
   | `JWT_SECRET` | Generate: `openssl rand -base64 64` |
   | `APP_BASE_URL` | `https://captain.rnsj.913555.xyz` |

8. Go to **Networking** tab:
   - **Container HTTP Port**: `8080`

9. Click **Update & Deploy**

### Step 4: Create Frontend App

1. Go to **Apps** → **Create New App**
2. **App Name**: `project-cv-frontend`
3. Click **Create**
4. Open the app and go to **Deployment** tab

5. **Deployment Method**: Select **Dockerfile (git)**

6. Configure Git:
   | Field | Value |
   |-------|-------|
   | Repository URL | Your GitHub/GitLab repo URL |
   | Branch | `main` |
   | Dockerfile Path | `frontend/Dockerfile` |

7. Go to **Environment Variables** tab, add:
   | Key | Value |
   |-----|-------|
   | `VITE_API_BASE_URL` | `https://project-cv-backend.captain.rnsj.913555.xyz/api` |

8. Go to **Networking** tab:
   - **Container HTTP Port**: `80`

9. Go to **Domains** tab:
   - Add custom domain: `captain.rnsj.913555.xyz`
   - Enable HTTPS (Let's Encrypt)

10. Click **Update & Deploy**

---

## Verify Deployment

1. Check **App Logs** for both apps
2. Visit `https://captain.rnsj.913555.xyz` - should see the login page
3. Try logging in:
   - Email: `admin@project.cv`
   - Password: `admin123`
   - Should redirect to `/admin/dashboard`

---

## Environment Variables Summary

### Backend (`project-cv-backend`)
| Variable | Value |
|----------|-------|
| `POSTGRES_PASSWORD` | Your PostgreSQL password |
| `JWT_SECRET` | Strong random string |
| `APP_BASE_URL` | `https://captain.rnsj.913555.xyz` |

### Frontend (`project-cv-frontend`)
| Variable | Value |
|----------|-------|
| `VITE_API_BASE_URL` | `https://project-cv-backend.captain.rnsj.913555.xyz/api` |

---

## Troubleshooting

### Backend won't start
- Check **App Logs** tab
- Verify PostgreSQL app is running
- Check `POSTGRES_PASSWORD` matches between apps

### Frontend can't reach backend
- Verify backend URL in environment variable
- Check backend app is running
- Ensure no firewall blocking

### Database connection issues
- PostgreSQL internal DNS: `project-cv-db.sail`
- In CapRover, services on the same app can connect via `service-name`
- For one-click apps, use `app-name.sail`

### SSL issues
- Domain must point to CapRover server
- Enable HTTPS in **Domains** tab
- Wait a few minutes for Let's Encrypt

---

## Updating

```bash
# Make changes locally
git add .
git commit -m "Update feature"
git push origin main

# In CapRover, click "Update & Deploy" for each app
```

Or enable **Auto-Deploy** in the Deployment tab!
