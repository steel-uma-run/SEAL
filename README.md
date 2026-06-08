# SEAL: Software Engineering Hackathon Management System

FPT University's Software Engineering Department organizes the SEAL
Hackathon 3 times per year (Spring / Summer / Fall). Each season attracts
20–40 competing teams, 5–8 expert judges, and 10+ mentors.

In its current state, everything is fully manual:

- Registration via Google Forms
- Scoring via Excel sheets
- Notifications via email
- Rankings announced manually.

This project aims to build a fully automated, clearly audited, easy to
use and deploy system for organizers of a hackathon event, in order to
reduce manual work and human errors.

## Features

- Season & Round creation
- Configure the competition timeline (start/end dates, submission windows)
  ensuring no overlapping periods and a minimum 24‑hour gap between submission
  deadline and scoring deadline.  
- Initialize each round in a Draft state.  
- Verify that only the Event Coordinator can perform these setup actions.
- Log in via OAuth2 providers (Google, GitHub).  
- New users start as Guests and must fill a profile form to become Students or
  request Judge/Mentor roles, which are queued for approval.  
- Prevent users from selecting more than one role simultaneously.  
- Block accounts that are banned or deactivated from logging in.  
- Allow creation of a team; a single‑member team is permitted, but teams with
  multiple members require a designated Leader.  
- Enforce unique team names and one‑person‑one‑team rule.  
- Assign at most one Mentor per team, and limit each Mentor to a maximum of
  three teams.  
- Permit deletion of a newly created team only if no submissions have been
  made.  
- Freeze team composition; no members can be added or removed after the
  competition starts.  
- Provide a submission portal where Team Leaders upload code and supply at
  least three valid GitHub URLs.  
- Allow continuous resubmission before the deadline, automatically versioning
  each upload while retaining a complete history.  
- Send an email confirmation to the team after each successful submission.  
- Lock all submissions at the deadline, making them immutable.  
- Run a conflict‑of‑interest check so a Mentor never reviews their own team’s
  work.  
- Automatically assign each submission to at least three reviewers, balancing
  the load so the difference between any two reviewers’ workloads does not
  exceed three assignments.  
- Keep the official scoring interface hidden until each Judge completes two
  sample reviews for calibration.  
- Provide a scoring form requiring a numeric score (0‑100) for each criterion,
  with no empty fields allowed.  
- Calculate the total score using the weighted scheme 40‑30‑20‑10.  
- Require judges to add a comment explaining each score.  
- Auto‑save drafts and allow judges to edit their scores up until the scoring
  deadline.  
- Email reminders to judges who have not completed their reviews 12 hours
  before the deadline.  
- Ensure judges cannot see other judges’ scores while they are entering their
  own, preserving impartiality.  
- Compute final scores by averaging weighted scores, handling missing judges by
  averaging over the available reviewers (minimum two required).  
- Resolve ties by prioritizing technical scores over innovation scores.  
- Detect and flag outlier scores that deviate more than 20 % from the group
  average for coordinator review.  
- Generate an automatically filtered list of qualified teams for the next round
  based on rankings.  
- Send a result announcement email to all participants.  
- Switch the entire round to a read‑only mode, preventing any further edits.  
- Prohibit any post‑closure score changes except through a formal appeal
  handled by the Coordinator.  
- Record every score update and appeal action in an immutable audit log; no
  hard deletions are allowed.

## Members

| MSSV     | Name                 | Role           |
|----------|----------------------|----------------|
| SE203403 | Nguyễn Thế Triết     | Project leader |
| SE203419 | Nguyễn Khoa Điền     | Backend        |
| SE203654 | Nguyễn Thành Trung   | Frontend       |
| SE203450 | Trương Hoàng Mỹ Xuân | Backend        |
| SE203435 | Nguyễn Thành Trung   | Frontend       |

## Tech stack

Frontend: [Svelte](https://svelte.dev) (framework) +
[TailwindCSS](https://tailwindcss.com) + JavaScript/TypeScript

Backend: Java 21 + Spring Boot

Database: [PostgreSQL](https://www.postgresql.org)

For development documentation, see [docs/](docs/).
