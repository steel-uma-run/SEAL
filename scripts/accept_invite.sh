#!/usr/bin/env bash

invite_id=$(curl localhost:8080/api/v0/invites --header "Authorization: Bearer $(./invitee_login.sh)" | jq ".[0].id" --raw-output)
curl -X POST "localhost:8080/api/v0/invites/$invite_id/accept" --header "Authorization: Bearer $(./invitee_login.sh)"
