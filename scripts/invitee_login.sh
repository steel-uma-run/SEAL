#!/usr/bin/env bash

curl localhost:8080/api/v0/auth/login --json '{"email": "invitee@fpt.edu.vn", "password": "hunter2"}' | jq ".token" --raw-output
