Temporary doc to detail every API endpoint until we have an OpenAPI
document.

# API base

The base of the API is at `/api`, eg. `http://localhost:8080/api`.

There will be versioning in the future.

# Auth

These APIs are open access. No authentication are required to use them.

Register: `POST /auth/register`.

- JSON body
    - email: string, must be valid email
    - password: string
    - student_id: string
    - name: string
    - is_external: boolean
- Returns
    - 200 on success, with no body
    - 409 if the email is already used

Login: `POST /auth/login`

- JSON body
    - email: string, must be valid email
    - password: string
- Returns
    - 200 on success, with body
        - token: string, the JWT
        - user: object, the user object
            - id: uuid string
            - email: string
            - role: enum
            - full_name: string
            - student_id: string, only if user is a student
            - student_type: enum, only if user is a student
            - student_status: enum, only if user is a student
    - 403 if credentials are invalid

# Profile

Get self profile: `GET /profile`

- Bearer token required
- Returns:
    - 200 along with user profile on success
    - 403 if unauthenticated

Approve student: `POST /profile/{id}/approve`

- Only accessible by coordinator users
- Returns:
    - 200 on success, with no body
    - 400 if the profile is not a student
    - 403 if unauthorized
    - 404 if the profile with `id` does not exist

# Coordinator

Get a list of unapproved student users: `GET /coordinator/unapproved`

- Returns: 200 along with a list of unapproved students

# Seasons

Get all seasons: `GET /seasons`

- Open access, no authentication required
- Returns: 200 along with a list of all seasons

Create a season: `POST /seasons`

- Only accessible by coordinator users
- JSON body:
    - description: string
    - start_time: timestamp string
    - end_time: timestamp string, must be after start_time
- Returns:
    - 200 on success, along with the newly created season
        - id: uuid string
        - description: string
        - start_time: timestamp string
        - end_time: timestamp string, guaranteed to be after start_time
    - 403 if unauthorized

# Teams

Create a new team: `POST /teams`

- Only accessible by coordinators and students
- JSON body:
    - name: string
    - description: string
    - season_id: the uuid of the season this team should belong to
    - leader_id: the uuid of the user that should be the team's leader
- Returns:
    - 201 on success along with the newly created team
    - 403 is unauthorized

# Tracks

Create a track: `POST /seasons/{season_id}/tracks`

- Only accessible by coordinator users
- Bearer token required
- JSON body:
    - name: string, cannot be blank
    - description: string
- Returns:
    - 201 on success, along with the newly created track
        - id: uuid string
        - name: string
        - description: string
        - season_id: uuid string
    - 400 if validation fails
    - 403 if unauthorized
    - 404 if the season with `season_id` does not exist