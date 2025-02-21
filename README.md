# Table

A Clojure library for interacting with Google Sheets using [SpreadAPI](https://spreadapi.roombelt.com).

## Overview

Table provides a simple protocol and implementation for performing basic CRUD (Create, Read, Update, Delete) operations on Google Sheets. It uses the Google Sheets API behind the scenes and provides a convenient way to interact with spreadsheet data.

## Features

-   **get-sheet**: Retrieve data from a specified sheet.
-   **update-row**: Update individual row
-   **update-rows**: Update several rows
-   **insert-row**: Insert new row into a sheet.

## Getting Started

Choose a Google Sheet and follow these [instructions](https://spreadapi.roombelt.com/setup) to setup spreadapi.

### Usage

1.  **Create a table client**:

```clojure
(require '[table.spreadapi :as spreadapi])

(def credentials
  {:script-id "your-script-id"
   :key "your-api-key"})

(def table-client (spreadapi/create-spread-api-google-sheets-client credentials))
```

2.  **Retrieve data from a sheet**:

```clojure
(require '[table.protocol :as protocol])

(def data (protocol/get-sheet table-client "Sheet1"))
(println data)
```

3.  **Update a row**:

```clojure
(protocol/update-row client "Sheet1" 1 {"column1" "new value"})
```

4.  **Insert a row**:

```clojure
(protocol/insert-row client "Sheet1" {"column1" "new value"})
```

## Testing

To run the tests, use the following command:

```shell
clojure -M:test
```

## License

Distributed under the MIT License.
