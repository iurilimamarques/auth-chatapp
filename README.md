# auth-chatapp

This project is responsible for authenticating, authorizing, and validating incoming requests from the user into the system. It generates a valid JWT token upon the user's login, which is then used to validate every incoming request from the client side (Postman or [frontend-chatapp](https://github.com/iurilimamarques/frontend-chatapp)). When the user logs out, it saves the JWT token on a blacklist in Redis to ensure that the token is invalid. The auth-chat app uses the JWT token alongside the Spring Boot Security library to provide authentication and authorization features.

## Involved projects
- https://github.com/iurilimamarques/chat-components
- https://github.com/iurilimamarques/gateway-chatapp
- https://github.com/iurilimamarques/api-chatapp
- https://github.com/iurilimamarques/frontend-chatapp
