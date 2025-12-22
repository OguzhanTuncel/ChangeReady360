FROM node:22 AS build
WORKDIR /app

COPY package*.json ./
RUN npm install --legacy-peer-deps

COPY . .
# je nach Angular-Version:
# npm run build --prod ODER npm run build --configuration production
RUN npm run build

FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Angular 21 Build-Ausgabe liegt im browser Unterverzeichnis
COPY --from=build /app/dist/test/browser ./

# Nginx-Konfiguration für Angular SPA Routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
