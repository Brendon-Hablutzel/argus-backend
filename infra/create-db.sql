CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE DATABASE argus;

\c argus;

CREATE TABLE IF NOT EXISTS activetabs (
  "timestamp" TIMESTAMPTZ NOT NULL,
  title TEXT NOT NULL,
  "url" TEXT NOT NULL,
  "status" VARCHAR(20) NOT NULL,
  -- whether a tab was selected at all. if false, title, url, and status will be empty
  tab_selected BOOLEAN NOT NULL
);

SELECT create_hypertable('activetabs', by_range('timestamp'));
