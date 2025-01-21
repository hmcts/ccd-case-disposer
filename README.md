# ccd-case-disposer

[![Build Status](https://travis-ci.org/hmcts/ccd-case-disposer.svg?branch=master)](https://travis-ci.org/hmcts/ccd-case-disposer)

## Purpose

This micro-service disposes off case records after a certain period of inactivity after a significant business event

## Getting Started

### Prerequisites
- [JDK 21](https://openjdk.org/)

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/ccd-case-disposer` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container and immediately attempt to delete qualifying case records from the data sources it's configured to connected to.

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable
is changed or any other script regarding docker image/container build, the suggested way to ensure all is
cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Developing

### Unit tests
To run all unit tests execute the following command:
```bash
./gradlew test
```

### Integration tests
To run all integration tests execute the following command:
```bash
./gradlew integration
```

### Functional tests
The functional tests require Elasticsearch, which is not enable by default on the local `ccd-docker` setup, thus it should be enabled along with logstash with this command:
```bash
./ccd enable elasticsearch logstash
```

The next step is to get both `ccd-definition-store-api` and `ccd-data-store-api` to use Elasticsearch and this is done by exporting the following environment variables:
```bash
export ES_ENABLED_DOCKER=true
export ELASTIC_SEARCH_ENABLED=$ES_ENABLED_DOCKER
export ELASTIC_SEARCH_FTA_ENABLED=$ES_ENABLED_DOCKER
```

Indices of the relevant case types are expected to be present in the Elasticsearch instance for the tests to work.
The easiest way to get the indices created is to run the `ccd-data-store-api` functional tests at least once prior to running these functional tests.
This is especially useful when testing locally.

When the above steps are completed, run the functional tests using the following command:
```bash
./gradlew functional
```

> Note: These are the tests run against an environment.
> Please see [ccd-docker/README.md](./ccd-docker/README.md) for local environment testing.
>
> If you would like to test against AAT dependencies then run `docker-compose up`.
> Also set the required environment variables that can be found by reviewing the contents of this project's
> [Jenkinsfile_CNP](./Jenkinsfile_CNP) script (particularly the `secrets` mappings, and the variables set by
> the `setBeftaEnvVariables` routine).
>

### Code quality checks
We use [checkstyle](http://checkstyle.sourceforge.net/) and [PMD](https://pmd.github.io/).

To run all checks execute the following command:

```bash
./gradlew clean checkstyleMain checkstyleTest checkstyleIntegrationTest pmdMain pmdTest pmdIntegrationTest
```

To run all checks alongside the unit tests execute the following command:

```bash
./gradlew checks
```

or to run all checks, all tests and generate a code coverage report execute the following command:

```bash
./gradlew check integration functional jacocoTestReport
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

