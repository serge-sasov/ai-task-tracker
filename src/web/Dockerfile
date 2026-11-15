# syntax=docker/dockerfile:1

# --- Build stage: install dependencies and build the Vue 3 SPA ---
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies first to leverage Docker layer caching.
COPY package.json package-lock.json ./
RUN npm ci

# Copy the remaining project sources and build the static bundle.
COPY . .
RUN npm run build

# --- Runtime stage: nginx serves the static build and proxies /api ---
FROM nginx:alpine

# Serve the built SPA.
COPY --from=build /app/dist /usr/share/nginx/html

# SPA configuration (history fallback + /api proxy to the backend service).
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
