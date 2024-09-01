# Cardrace Server

This is the server-side component of Cardrace, an exciting multiplayer board game. This server handles game logic, player interactions, and real-time communication for the Cardrace game.

## Table of Contents
1. [Overview](#overview)
2. [Technologies Used](#technologies-used)
3. [Setup](#setup)
4. [Key Components](#key-components)
5. [API Documentation](#api-documentation)
6. [Client-Side Repository](#client-side-repository)
7. [Contributing](#contributing)
8. [License](#license)

## Overview

The Cardrace server is built using Spring Boot and provides a robust backend for managing game states, player actions, and real-time updates. It utilizes websockets for live game interactions and Redis for in-game caching to ensure smooth gameplay.

## Technologies Used

- **Spring Boot**: Core framework for building the application
- **Spring Security with JWT**: For secure authentication and authorization
- **Websockets (netty-socketio)**: For real-time, bidirectional communication
- **Redis**: In-memory data structure store used for caching game states
- **Maven**: Dependency management and build tool

## Setup

1. Clone the repository:
   ```
   git clone https://github.com/your-username/cardrace-server.git
   ```
2. Navigate to the project directory:
   ```
   cd cardrace-server
   ```
3. Install dependencies:
   ```
   mvn install
   ```
4. Set up environment variables (see `application.properties` for required variables)
5. Run the application:
   ```
   mvn spring-boot:run
   ```

## Key Components

### Spring Security with JWT

We use Spring Security with JSON Web Tokens (JWT) for authentication. This ensures secure access to game resources and player data.

### Websockets (netty-socketio)

Real-time game updates and player actions are handled using websockets, specifically the netty-socketio library. This allows for efficient, bidirectional communication between the server and connected clients.

### Redis for In-Game Caching

Redis is used to cache game states and other frequently accessed data. This improves performance and allows for quick retrieval of game information across different server instances.

## API Documentation

### Authentication Endpoints

- **POST /api/auth/signup**: Register a new user
    - Request body: SignupRequest (username, password)
    - Response: AuthResponse (token, username)

- **POST /api/auth/login**: Authenticate a user
    - Request body: LoginRequest (username, password)
    - Response: AuthResponse (token, username)

### Game Endpoints

- **POST /api/games/create**: Create a new game
    - Query parameters: gameName (String), numPlayers (Integer)
    - Response: gameId (String)

- **GET /api/games/available/{gameId}**: Check if a game is available to join
    - Path variable: gameId (String)
    - Response: Boolean (true if available, false otherwise)

## Client-Side Repository

For full system architecture, gameplay instructions, and client-side implementation details, please refer to the [Cardrace Client Repository](https://github.com/zohaib-a-ahmed/cardrace-fe/blob/main/README.md).

## Contributing

We welcome contributions to the Cardrace server! If you'd like to contribute:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please ensure your code adheres to the project's coding standards and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.