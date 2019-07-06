# uniswag-backend
This is the backend for [uniswag](https://github.com/ramomar/uniswag).

## Features
- Export UANL Nexus assignments (Implementation of some [Todoist sync API (v7) endpoints](https://developer.todoist.com/sync/v7/#overview)).
  - Projects creation.
  - Items creation.
  - Batch requests.
- Creation of folders from UANL SIASE (Implementation of some [Google Drive (v3) endpoints](https://developers.google.com/drive/v3/reference/)).
  - Folders creation.
  - Batch requests.

## OAuth2 credentials
You can setup your own credentials in the `application.conf` located in the `resources` folder.

## Building
Run `sbt compile`

## Running
Run `sbt run`
