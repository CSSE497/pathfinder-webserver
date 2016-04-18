# Pathfinder Webserver
[![Build Status](https://travis-ci.org/CSSE497/pathfinder-webserver.svg?branch=dev)](https://travis-ci.org/CSSE497/pathfinder-webserver)

This server hosts [https://thepathfinder.xyz](https://thepathfinder.xyz), the main website and user dashboard for the Pathfinder webservice. It allows users to create accounts, register applications, modify route and authentication settings and interact with the transports and commodities in their cluster in real-time.

## Development Guide
These instructions exist mostly as a reference for the development team.


### Local development
The server can be run and debugged locally with

    activator ~run

### Standalone release
A standalone release can be built with

    activator dist

This release can be run with

    unzip -d /opt target/universal/pathfinder-webserver-<version>.zip
    /opt/pathfinder-webserver-<version>/bin/pathfinder-webserver -Dhttp.port=disabled -Dhttps.port=443 -Dplay.server.https.keyStore.path=<path to jks> -Dplay.server.https.keyStore.password=<jks password>

### Docker

A Docker image cant be built with

    activator docker:publishLocal

The image can be run locally with

    docker run -p 9000:9000 pathfinder-webserver:<version>

The image can be pushed to gcloud with

    gcloud docker push beta.gcr.io/<gcloud project id>/pathfinder-webserver:<version>

The running Docker container can be updated with

    kubectl rolling-update pathfinder-webserver --image=beta.gcr.io/<gcloud project id>/pathfinder-webserver:<version>

## License

[MIT](https://raw.githubusercontent.com/CSSE497/pathfinder-webserver/master/LICENSE).