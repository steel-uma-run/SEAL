#!/usr/bin/env bash

set -euo pipefail

event_id=$(psql --dbname seal -A -t -q --command "SELECT id FROM events")
curl localhost:8080/api/v0/teams \
  --header "Authorization: Bearer $(./login.sh)" \
  --json "{\"name\": \"Best team\", \"description\": \"\", \"event_id\": \"$event_id\"}"

team_id=$(psql --dbname seal --command "SELECT id FROM teams" -t -A -q)
curl -X POST "localhost:8080/api/v0/teams/$team_id/approve" --header "Authorization: Bearer $(./coord_login.sh)"
