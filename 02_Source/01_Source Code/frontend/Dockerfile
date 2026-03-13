# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# These are baked into the bundle at build time
ARG VITE_API_BASE=http://localhost:8080/api/v1
ARG VITE_KEYCLOAK_URL=http://localhost:9090
ENV VITE_API_BASE=$VITE_API_BASE
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL

RUN npm run build

# ── Stage 2: Serve with nginx ─────────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
