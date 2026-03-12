# Medicine

**Medicine** is an AI-assisted online pharmaceutical commerce platform designed to help users obtain basic medication guidance for common symptoms. Through an interactive consultation flow, users can describe their symptoms in natural language, and the system will guide them through follow-up questions before recommending suitable medicines available on the platform.

The idea for this project originated from a personal experience. About a year ago, I caught a cold due to a viral infection and urgently needed medicine. However, since I was at school and my family was not around, I had no one to consult. Although online pharmacies were available, I did not know which medication to choose. I eventually had to register for a medical consultation, and the consultation fee ended up costing more than half the price of the medicine itself. This experience motivated the development of **Medicine**, with the goal of using AI to make basic medication guidance more accessible for common and minor illnesses.

This repository contains the backend implementation of the system. It is a **multi-module backend project built with `Java 21` and `Spring Boot 3.5.x`**, providing the core services required to support the platform.

The project uses **Maven multi-module aggregation** to organize the codebase and follows a modular architecture. Each service can run independently while sharing common infrastructure components.

## Architecture Highlights

- Admin, Client, and Agent services are independently deployable modules  
- Shared capabilities are separated into infrastructure modules such as Redis, MongoDB, MyBatis-Plus, RabbitMQ, storage, and security  
- Inter-service communication is implemented using **Apache Dubbo**, supporting both local direct invocation and **Nacos-based service discovery**  
- Agent configuration is stored in **Redis**, allowing external **Python-based AI services** to read and coordinate AI workflows  

## Project Status

The project is currently in the **active development stage**, and the core architecture and services are being implemented and refined. The overall development of the system is expected to be completed **before April**, after which further testing, optimization, and potential open-source release will be considered.
