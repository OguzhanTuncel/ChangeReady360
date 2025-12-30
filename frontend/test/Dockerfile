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
# Angular (@angular/build:application) outputs browser files into dist/<project>/browser
# We must copy that folder to nginx html root, otherwise nginx serves its default index.html.
COPY --from=build /app/dist/test/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
