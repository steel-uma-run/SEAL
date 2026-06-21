#!/usr/bin/env bash

curl localhost:8080/api/v0/invites --header "Authorization: Bearer $(./invitee_login.sh)"
