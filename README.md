# Argus Backend

Scala + sbt monorepo for Argus' backend.

## ingestor

Server that takes in messages from the extension and sends them to Kafka.

To run with hot reloading:

```bash
sbt

project ingestor

~reStart
```

## common

Contains common code, primarily data types used for communication between services.
