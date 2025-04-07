# Argus Backend

Scala + sbt monorepo for Argus' backend.

## Development

To run one of the scala services with hot reloading:

```bash
sbt

project <project name>

~reStart
```

To build and locally publish a docker image for one of the scala services:

```bash
DOCKER_BUILDKIT=0 sbt "<project name> / docker"
```

## Components

### Kafka

Kafka can be deployed locally with docker compose (see [this article](https://developer.confluent.io/confluent-tutorials/kafka-on-docker/) for details). Use `infra/docker-compose.yaml`.

### TimescaleDB

A Postgres extension that adds better support for handling timeseries data. See [here](https://github.com/timescale/timescaledb) for more details. Like Kafka, configuration for a local TimescaleDB deployment is included in `infra/`, and an instance can be deployed locally with `infra/docker-compose.yaml`

### api

HTTP server that sits between clients and TimescaleDB--fetches data and exposes it on several endpoints, performing analytics and aggregation as necessary.

### ingestor

HTTP server that listens for messages from the extension and sends them to Kafka.

### processor

Kafka consumer--continuously polls for messages from Kafka and saves these messages in the database.

### common

Contains common code, primarily data types used for communication between services.
