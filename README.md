# Asset Manage

Enterprise asset management application — Java Spring Boot backend + Angular frontend.

## Structure

```
asset-manage/
├── backend/     — Spring Boot 3.2 REST API (Java 17, JPA, H2)
└── frontend/    — Angular 17 SPA
```

## Backend

```bash
cd backend && mvn spring-boot:run
```

API on http://localhost:8080/api

## Frontend

```bash
cd frontend && npm install && npm start
```

UI on http://localhost:4200

## URD

Specification documents are in a separate repo: [asset-manage-urd](https://github.com/iLoveGitHubs/asset-manage-urd)

## Features

- Asset CRUD with validation
- Asset categories
- Asset assignment to employees (assign/return workflow)
- Straight-line depreciation calculation
- Pagination, search, status transition validation
