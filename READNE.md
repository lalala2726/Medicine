# Medicine

**Medicine** is an AI-assisted pharmaceutical e-commerce platform designed to help users obtain basic medication guidance when facing common symptoms. Users can describe their symptoms in natural language, and the system will guide them through an interactive consultation process, asking follow-up questions and eventually recommending suitable medicines available on the platform.

The idea for this project originated from a personal experience. About a year ago, I caught a cold due to a viral infection and urgently needed medicine. At the time, I was at school and my family was not around, so I had no one to ask for advice. Although online pharmacies were available, I did not know which medicine I should choose because I lacked medical knowledge. In the end, I had to register for a medical consultation, and the consultation fee turned out to be more than half the price of the medicine itself. This experience made me realize that for common and minor illnesses, obtaining basic medication guidance can involve unnecessary time and financial costs. Therefore, I started developing **Medicine** to explore how AI could make basic medication guidance more accessible.

This repository contains the backend implementation of the system. It is a **multi-module backend project built with `Java 21` and `Spring Boot 3.5.x`**, providing the core services required to support the platform.

The project uses a **Maven multi-module structure** to organize the codebase and follows a modular architecture. Each service can be started and deployed independently while sharing common infrastructure modules.

## Architecture Highlights

- Admin, Client, and Agent services are independently deployable modules
- Shared capabilities are separated into infrastructure modules such as Redis, MongoDB, MyBatis-Plus, RabbitMQ, storage, and security
- Inter-service communication is implemented using **Apache Dubbo**, supporting both local direct invocation and **Nacos-based service discovery**
- Agent configurations are stored in **Redis** and read by an external **Python-based AI service** to coordinate AI workflows

## Project Status

The project has already been open-sourced and is still under active development. The core architecture and main functionalities have been largely completed, and further improvements will focus on feature enhancements, system stability, and continued optimization of the AI capabilities.
