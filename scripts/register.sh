#!/usr/bin/env bash

curl localhost:8080/api/v0/auth/register --json '{"email": "nhc@fpt.edu.vn", "name": "NHC", "password": "hunter2", "is_external": true, "student_id": "SE11111"}'

id=$(psql --dbname seal --command "SELECT id FROM students" -t -A -q)
curl -X POST "localhost:8080/api/v0/account/$id/approve" --header "Authorization: Bearer $(./coord_login.sh)"
