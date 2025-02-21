(ns sheets-api.core
  (:require [clojure.string :as str])
  (:import
   (com.google.api.client.googleapis.javanet GoogleNetHttpTransport)
   (com.google.api.client.json.jackson2 JacksonFactory)
   (com.google.api.services.sheets.v4 Sheets Sheets$Builder)
   (com.google.api.services.sheets.v4.model ValueRange)))

;;; Protocol definition
(defprotocol SheetsAPI
  (get-data [this spreadsheet-id range] "Retrieves data from the specified range in the spreadsheet.")
  (update-data [this spreadsheet-id range values] "Updates data in the specified range in the spreadsheet."))

;;; Google Sheets API implementation
(defrecord GoogleSheets [credentials]
  SheetsAPI
  (get-data [this spreadsheet-id range]
    (let [transport (GoogleNetHttpTransport/newTrustedTransport)
          json-factory (JacksonFactory/getDefaultInstance)
          service (.build (Sheets$Builder. transport json-factory credentials))]
      (try
        (let [response (.execute (.get (.spreadsheets service) spreadsheet-id range))
              values (.getValues response)]
          (if (nil? values)
            []
            (seq values)))
        (catch Exception e
          (println (str "An error occurred: " (.getMessage e)))
          nil))))
  (update-data [this spreadsheet-id range values]
    (let [transport (GoogleNetHttpTransport/newTrustedTransport)
          json-factory (JacksonFactory/getDefaultInstance)
          service (.build (Sheets$Builder. transport json-factory credentials))
          body (doto (ValueRange.) (.setValues (seq values)))
          request (.execute (.update (.values (.spreadsheets service) spreadsheet-id range body) "USER_ENTERED")]
      (try
        request
        (catch Exception e
          (println (str "An error occurred: " (.getMessage e)))
          nil)))))

(defn create-google-sheets-client [credentials]
  (GoogleSheets. credentials))
