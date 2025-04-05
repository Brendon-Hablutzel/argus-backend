# Argus Backend

Scala + sbt monorepo for Argus' backend.

## Kafka

Kafka can be deployed locally with docker compose (see [this article](https://developer.confluent.io/confluent-tutorials/kafka-on-docker/) for details). Use `infra/docker-compose.yaml`.

## ingestor

Server that takes in messages from the extension and sends them to Kafka.

To run with hot reloading:

```bash
sbt

project ingestor

~reStart
```

To build and locally publish a docker image:

```bash
DOCKER_BUILDKIT=0 sbt "ingestor / docker"
```

## common

Contains common code, primarily data types used for communication between services.
