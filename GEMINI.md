# Project Overview

This is a comprehensive PC parts quoting system built with a microservices architecture. It consists of two Spring Boot backend services, a Vue.js 3 frontend, and an Nginx gateway, all containerized with Docker.

## Backend

The backend is split into two microservices:

*   **`ms-cotizador`**: The core business logic for managing PC components, quotes, orders, and more. It follows a layered architecture inspired by **Domain-Driven Design (DDD)**, with distinct packages for domain, application, and infrastructure layers.
*   **`ms-seguridad`**: Handles security, authentication, and authorization using **JWT**. It also uses a **Quartz Scheduler** for tasks like session cleanup.

Both microservices are built with **Java 21** and **Spring Boot 3**. They use **Spring Data JPA** for database interaction with **MySQL**, and **OpenAPI** for API documentation.

## Frontend

The frontend is a single-page application built with **Vue.js 3**. It uses:

*   **Pinia** for state management.
*   **Vue Router** for client-side routing.
*   **Axios** for making HTTP requests to the backend.
*   **Tailwind CSS** for styling.
*   **Vite** as the build tool.

# Building and Running

The recommended way to build and run the project is with Docker Compose.

## Prerequisites

*   Docker
*   Docker Compose

## Running the Application

1.  **Initialize the environment:**
    *   For Linux/macOS: `./init-env.sh`
    *   For Windows PowerShell: `./init-env.ps1`

2.  **Start the services:**
    ```bash
    docker-compose up -d
    ```

3.  **Access the application:**
    *   **Portal Web:** http://localhost
    *   **API Gateway:** http://localhost:8080
    *   **Swagger UI:** http://localhost:8080/swagger-ui.html

## Development

### Backend

To run the backend services locally without Docker, you'll need:

*   Java 21
*   Maven
*   MySQL

1.  **Set up the database:** Create the necessary databases and users, and run the SQL scripts in the `sql` and `scripts` directories.
2.  **Configure environment variables:** Set the database connection details and other required variables.
3.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```

### Frontend

To run the frontend locally, you'll need:

*   Node.js
*   npm

1.  **Install dependencies:**
    ```bash
    npm install
    ```
2.  **Run the development server:**
    ```bash
    npm run dev
    ```

# Testing

## Backend

To run the backend tests, use the following Maven command:

```bash
mvn test
```

The project uses **JUnit 5**, **Testcontainers**, and **REST Assured** for testing.

## Frontend

The frontend uses **ESLint** for linting and **Prettier** for formatting.

```bash
npm run lint
npm run format
```

# Development Conventions

*   **Backend**: The backend follows the principles of Domain-Driven Design (DDD) and uses design patterns like Strategy, Builder, and Decorator. The code is organized into layers: domain, application (services), and infrastructure (controllers, repositories).
*   **Frontend**: The frontend uses the Composition API and a store pattern with Pinia for state management. The structure is feature-oriented, with dedicated directories for components, views, stores, and services.
*   **Code Style**: The project uses ESLint and Prettier to enforce a consistent code style.