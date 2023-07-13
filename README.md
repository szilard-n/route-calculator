# Route Calculator Backend Challenge

## Project Overview

The purpose of this challenge was to create a simple API that will calculate
the shortest route from a country to another. The API uses Dijkstra's shortest
path algorithm to return the shortest route in the form of a list containing
the country codes.

## Challenge Requirements

- Spring Boot, Maven
- Data link: https://raw.githubusercontent.com/mledoze/countries/master/countries.json
- The application exposes REST endpoint `/routing/{origin}/{destination}` that
  returns a list of border crossings to get from origin to destination
- Single route is returned if the journey is possible
- Algorithm needs to be efficient
- If there is no land crossing, the endpoint returns `HTTP 400`
- Countries are identified by `cca3` field in country data
- HTTP request sample (land route from the Czech Republic to Italy):
    - Request: `GET /routing/CZE/ITA HTTP/1.0`
    - Response: `{ "route": ["CZE", "AUT", "ITA"] }`

## Technology Stack

- **Docker and Docker Compose**: Used for containerization and managing the deployment of the service.
- **Spring Boot**: Framework used for building the service, providing a robust and scalable development environment.
- **RestAssured**: Testing framework for API testing, ensuring the correctness of endpoints.

## Installation:

1. Clone the project from GitHub
2. Navigate to the project directory by running `cd route-calculator`
3. The application can be run either by using docker (docker must be installed), or by
using maven (maven must be installed). _Note: Port `8080` must be available on your system._
   - Using docker: run `docker compose up --build -d`
   - Using maven: run `mvn spring-boot:run`

We can now use the API by sending `GET` requests to `http://localhost:8080/routing/{origin}/{destination}`. 

_Note: Origin and destination must be valid `cca3` values from the given data link: https://raw.githubusercontent.com/mledoze/countries/master/countries.json_.