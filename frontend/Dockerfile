# ===== Stage 1: Build Angular =====
FROM node:20-alpine AS builder

WORKDIR /app

# Copier les fichiers package
COPY package*.json ./

# Installer les dépendances (avec retries pour éviter les ECONNRESET)
RUN npm ci --prefer-offline --no-audit --fetch-retries=5 --fetch-retry-mintimeout=20000 --fetch-retry-maxtimeout=120000

# Copier le code source
COPY . .

# Compiler Angular en mode production
RUN npm run build -- --configuration production

# ===== Stage 2: Servir avec Nginx =====
FROM nginx:alpine

# Configuration Nginx avec proxy vers calculs-api
RUN cat > /etc/nginx/conf.d/default.conf << 'EOF'
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    # SPA routing
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy vers le backend calculs-api
    location /calculs/ {
        proxy_pass http://calculs-api:8081/calculs/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

# Copier les fichiers compilés depuis le stage de build
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Exposer le port 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
