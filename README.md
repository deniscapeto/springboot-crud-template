# CRUD REST API Template using Spring Boot

### Background
This project was created to serve as a reference or template for basic REST API using spring boot.

### The following features are implemented

* CRUD for one generic Entity
* API supporting POST, PUT, GET by id and GET all
* Spring Security using basic http authorization and roles
* Unit tests for all endpoints and data serialization and deserialization

### Requirements

* Java 21
* Spring Boot 3.3.1
* JUnit
* H2 In memory database

### Disclaimer

This project does not define any architecture. All classes (Entity, Controller, Repository) is in the root folder. Feel free to copy (or fork) this basic structure and extend it applying the architecture that fits better to your project. E.g. Simple Layered architecture, DDD, Clean Architecture, hexagonal architecture etc.