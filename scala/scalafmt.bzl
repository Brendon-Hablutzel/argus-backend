load("@rules_scala//scala:advanced_usage/scala.bzl", "make_scala_binary", "make_scala_library")
load("@rules_scala//scala/scalafmt:phase_scalafmt_ext.bzl", "ext_scalafmt")

scala_binary = make_scala_binary(ext_scalafmt)
scala_library = make_scala_library(ext_scalafmt)
