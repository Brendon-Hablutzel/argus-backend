# Argus Backend

Monorepo for Argus' backend. Services are written in Scala and Bazel is used as the build tool.

## Development

Run a service with Bazel using `bazel run`, for example `api` can be run as follows:

```bash
bazel run //api:api_binary
```

It can be run with hot reloading using [`ibazel`](https://github.com/bazelbuild/bazel-watcher), for example again with
`api` as follows:

```bash
ibazel run //api:api_binary
```

## Components

### Kafka

Kafka can be deployed locally with docker compose (see [this article](https://developer.confluent.io/confluent-tutorials/kafka-on-docker/) for details). 
Use `infra/docker-compose.yaml`.

### TimescaleDB

A Postgres extension that adds better support for handling timeseries data. See [here](https://github.com/timescale/timescaledb) for more details. Like 
Kafka, configuration for a local TimescaleDB deployment is included in `infra/`, and an instance can be deployed 
locally with `infra/docker-compose.yaml`

### api

HTTP server that sits between clients and TimescaleDB--fetches data and exposes it on several endpoints, performing 
analytics and aggregation as necessary.

### ingestor

HTTP server that listens for messages from the extension and sends them to Kafka.

### processor

Kafka consumer--continuously polls for messages from Kafka and saves these messages in the database.

### common

Contains common code, primarily data types used for communication between services.
