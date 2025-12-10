FROM node:22 AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
# je nach Angular-Version:
# npm run build --prod ODER npm run build --configuration production
RUN npm run build

FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# outputPath aus angular.json prüfen, z. B. "dist/test" oder "dist/frontend"
# hier Beispiel: dist/test
COPY --from=build /app/dist/test ./

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
