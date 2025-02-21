(ns sheets-api.core
  (:require [clojure.string :as str])
  (:import
   (com.google.api.client.googleapis.javanet GoogleNetHttpTransport)
   (com.google.api.client.json.jackson2 JacksonFactory)
   (com.google.api.services.sheets.v4 Sheets Sheets$Builder)
   (com.google.api.services.sheets.v4.model ValueRange)))

;;; Protocol definition
(defprotocol SheetsAPI
  (get-sheet [this spreadsheet-id range] "Retrieves data from the specified range in the spreadsheet.")
  (update-sheet [this spreadsheet-id range values] "Updates data in the specified range in the spreadsheet."))

(defn- execute-sheets-request [credentials f & args]
  (let [transport (GoogleNetHttpTransport/newTrustedTransport)
        json-factory (JacksonFactory/getDefaultInstance)
        service (.build (Sheets$Builder. transport json-factory credentials))]
    (try
      (apply f service args)
      (catch Exception e
        (println (str "An error occurred: " (.getMessage e)))
        nil))))

;;; Google Sheets API implementation
(defrecord GoogleSheets [credentials]
  SheetsAPI
  (get-sheet [this spreadsheet-id range]
    (execute-sheets-request credentials
                            (fn [service]
                              (let [response (.execute (.get (.spreadsheets service) spreadsheet-id range))
                                    values (.getValues response)]
                                (if (nil? values)
                                  []
                                  (seq values))))))
  (update-sheet [this spreadsheet-id range values]
    (execute-sheets-request credentials
                            (fn [service]
                              (let [body (doto (ValueRange.) (.setValues (seq values)))
                                    request (.execute (.update (.values (.spreadsheets service) spreadsheet-id range body) "USER_ENTERED"))]
                                request)))))

(defn create-google-sheets-client [credentials]
  (GoogleSheets. credentials))
