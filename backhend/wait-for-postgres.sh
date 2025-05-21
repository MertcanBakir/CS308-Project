#!/bin/sh

echo "🔁 Waiting for PostgreSQL to be ready..."

while ! pg_isready -h postgres -p 5432 -U postgres > /dev/null 2>&1; do
  echo "⏳ PostgreSQL not ready yet. Retrying in 1 second..."
  sleep 1
done

echo "✅ PostgreSQL is ready. Starting the app..."
exec "$@"