CREATE DATABASE argus;

\c argus;

CREATE TABLE IF NOT EXISTS activetabs (
  id SERIAL PRIMARY KEY,
  "timestamp" TIMESTAMPTZ,
  title TEXT,
  "url" TEXT,
  "status" VARCHAR(20),
  -- whether a tab was selected at all. if false, title, url, and status will be empty
  tab_selected BOOLEAN
);
