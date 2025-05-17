#!/bin/bash
set -e

host="db"
shift
until pg_isready -h "$host" -U postgres; do
  echo "Waiting for PostgreSQL at $host..."
  sleep 2
done

exec "$@"

