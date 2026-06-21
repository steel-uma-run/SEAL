#!/usr/bin/env bash

curl localhost:8080/api/v0/auth/login --json '{"email": "coordinator@fpt.edu.vn", "password": "admin"}' | jq ".token" --raw-output
