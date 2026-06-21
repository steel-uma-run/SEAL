#!/usr/bin/env bash

curl localhost:8080/api/v0/auth/register --json '{"email": "invitee@fpt.edu.vn", "name": "InviteeNHC", "password": "hunter2", "is_external": true, "student_id": "SE11112"}'

id=$(psql --dbname seal --command "SELECT students.id FROM students JOIN users ON users.id = user_id WHERE full_name = 'InviteeNHC'" -t -A -q)
curl -X POST "localhost:8080/api/v0/account/$id/approve" --header "Authorization: Bearer $(./coord_login.sh)"

team_id=$(psql --dbname seal --command "SELECT id FROM teams" -t -A -q)
curl -X POST "localhost:8080/api/v0/teams/$team_id/invite/$id" --header "Authorization: Bearer $(./login.sh)"
