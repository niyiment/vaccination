# 💉 Immunization Management System 
The Immunization Management System is a scalable microservices-based platform designed to support digital immunization workflows,
including beneficiary registration, vaccination events, facility management, reporting, and data synchronization.

## Key Features
- **Beneficiary Registration**: Secure and efficient registration of individuals for vaccination.
- **Vaccination Events**: Tracking and managing vaccination schedules and events.
- **Facility Management**: Centralized management of healthcare facilities and resources.
- **Reporting**: Comprehensive reporting capabilities for monitoring and analytics.
- **Data Synchronization**: Seamless synchronization across multiple systems and devices.
- **Role-based Access Control**: Flexible and secure access control for different user roles.
- **Mobile App**: Mobile application for easy access to vaccination information.

## Architecture Overview
                        ┌───────────────────┐
                        │    API Gateway    │
                        │ (Spring Cloud GW) │
                        └─────────┬─────────┘
                                  │
        ┌──────────────┬─────────┼─────────┬──────────────┐
        ▼              ▼         ▼         ▼              ▼
┌────────────┐ ┌─────────────┐ ┌────────────┐ ┌──────────────┐ ┌──────────────┐
│ Auth Svc   │ │ Facility Svc │ │ User Svc   │ │ Vaccine Svc   │ │ Reporting Svc │
│ JWT, OAuth │ │ Facility DB  │ │ Profiles   │ │ Vaccination   │ │ Analytics/ETL │
└────────────┘ └─────────────┘ └────────────┘ └──────────────┘ └──────────────┘
│              │              │               │                 │
└─────Kafka Topics for Events (Registration, Vaccination, Updates)───────┘

                         ┌──────────────────────────────────────┐
                         │         PostgreSQL + Redis           │
                         │  Master Data + Caching + Audit Logs  │
                         └──────────────────────────────────────┘
                                      
                         ┌──────────────────────────────────────┐
                         │               Mobile App             │
                         │   (Flutter: Offline-first Sync)      │
                         └──────────────────────────────────────┘

