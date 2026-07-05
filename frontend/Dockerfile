# Stage 1: Build Vue application
FROM node:20-alpine AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Runtime environment using compiled static assets
FROM nginxinc/nginx-unprivileged:alpine
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration for Vue Router support
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose Nginx port (using non-privileged port)
EXPOSE 8080

USER nginx

CMD ["nginx", "-g", "daemon off;"]
