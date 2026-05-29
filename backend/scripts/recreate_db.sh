#!/usr/bin/env bash

psql --dbname postgres --command "DROP DATABASE seal;"
psql --dbname postgres --command "CREATE DATABASE seal;"

psql --dbname seal --file ./init.sql
psql --dbname seal --file ./seed.sql
