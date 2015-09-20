# Pathfinder WebServer

This is the code responsible for serving the Web Application found at [https://github.com/CSSE497/pathfinder-webclient](https://github.com/CSSE497/pathfinder-webclient).

This is a placeholder file until we create a more comprehensive README.

# Running the server

### Locally

```
activator ~run
```

### Docker

```
activator docker:publishLocal
docker run -p 9000:9000 pathfinder-webserver:1.0-SNAPSHOT
```

### Tests

```
activator test
```
