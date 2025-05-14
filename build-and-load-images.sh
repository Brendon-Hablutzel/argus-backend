#!/bin/bash

bazel run //ingestor:ingestor_docker
bazel run //processor:processor_docker
bazel run //api:api_docker
