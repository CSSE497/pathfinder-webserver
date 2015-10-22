# Pathfinder WebServer
[![Build Status](https://travis-ci.org/CSSE497/pathfinder-webserver.svg?branch=dev)](https://travis-ci.org/CSSE497/pathfinder-webserver)

[![Stories in Ready](https://badge.waffle.io/CSSE497/pathfinder-webserver.svg?label=ready&title=Ready)](http://waffle.io/CSSE497/pathfinder-webserver)
[![Stories in Progress](https://badge.waffle.io/CSSE497/pathfinder-webserver.svg?label=In%20Progress&title=In%20Progress)](http://waffle.io/CSSE497/pathfinder-webserver)
[![Stories under Review](https://badge.waffle.io/CSSE497/pathfinder-webserver.svg?label=Under%20Review&title=Under%20Review)](http://waffle.io/CSSE497/pathfinder-webserver)

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
