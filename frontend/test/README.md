# ChangeReady360 Frontend

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.0.3.

## 🚀 Quick Start

### Development Server (mit Hot Reload)

```bash
# Dependencies installieren (einmalig)
npm install

# Development Server starten
npm start
# oder
ng serve
```

Öffne `http://localhost:4200` im Browser. Die Anwendung lädt automatisch neu bei Code-Änderungen.

**Wichtig**: Das Backend muss auf `http://localhost:8080` laufen (siehe [DEV.md](./DEV.md)).

## 📋 Development Workflow

1. **Backend starten** (Docker Compose):
   ```bash
   # Im Projekt-Root
   docker-compose up -d
   ```

2. **Frontend starten** (lokal):
   ```bash
   cd frontend/test
   npm start
   ```

3. **Code ändern** → Browser aktualisiert automatisch! ✨

Siehe [DEV.md](./DEV.md) für detaillierte Anleitung.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
