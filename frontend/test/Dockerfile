# Build Stage
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies with legacy peer deps to handle Angular version conflicts
RUN npm ci --legacy-peer-deps

# Copy source code
COPY . .

# Build Angular app
RUN npm run build

# Production Stage
FROM nginx:alpine

# Copy built app to nginx
COPY --from=build /app/dist/test /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
