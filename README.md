# 🌍 GeoSentinel — AI-Powered Planetary Risk Intelligence Platform

## Problem Statement

* Monitoring global environmental and humanitarian risks requires aggregating data from multiple sources and presenting actionable intelligence in a unified platform.
* Existing solutions often focus on a single domain and lack AI-powered contextual analysis.

## Solution

* Developed a full-stack SaaS platform that provides AI-generated risk intelligence across 160+ countries and regions.
* Enables users to search any location and receive a comprehensive risk assessment covering Climate, Disaster, Water, Food, Health, and Conflict domains.
* Supports natural language interaction through an integrated AI assistant.

## Core Features

* Multi-domain planetary risk assessment.
* AI-generated risk intelligence reports.
* Location-aware conversational assistant.
* Real-time disaster and climate monitoring.
* Composite risk scoring using INFORM-inspired methodology.
* Role-based access control with JWT authentication.
* Event-driven alert generation and severity classification.

## System Architecture

* Designed a microservices ecosystem with six independent Spring Boot services.
* Implemented Spring Cloud Gateway for centralized routing, authentication, and rate limiting.
* Enabled asynchronous communication using Apache Kafka.
* Utilized Redis and PostgreSQL for high-performance caching and persistence.

## Data & Intelligence Layer

* Integrated USGS Earthquake API for disaster monitoring.
* Integrated Open-Meteo API for climate intelligence.
* Leveraged Anthropic Claude API for structured risk report generation.
* Persisted AI-generated reports using PostgreSQL JSONB storage.

## Performance Optimizations

* Implemented dual-layer caching using Redis and geospatial PostgreSQL queries.
* Reduced redundant AI inference requests through intelligent cache reuse.
* Achieved sub-100ms response times for cached reports.

## User Experience

* Built a React 18 + Vite frontend with a modern dark-themed dashboard.
* Added interactive risk scorecards, threat analysis panels, forecasts, and AI chat.
* Implemented automatic JWT refresh and secure session management.

## Technology Stack

**Backend:** Java 21, Spring Boot 3.2, Spring Security, Spring Cloud Gateway, Spring Kafka

**Frontend:** React 18, Vite, Axios

**Database:** PostgreSQL 16, Redis 7, Elasticsearch 8

**Infrastructure:** Docker, Kubernetes, Prometheus, Micrometer

**AI & Data Sources:** Anthropic Claude API, USGS API, Open-Meteo API
