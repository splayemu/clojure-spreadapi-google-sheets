(ns sheets-api.core
  (:require [clojure.string :as str
             [clj-http.client :as client]
             [cheshire.core :as json]]))

;;; Protocol definition
(defprotocol SheetsAPI
  (get-sheet [this sheet-name params] "Retrieves data from the specified sheet.")
  (update-sheet [this sheet-name params] "Updates data in the specified sheet."))

(defn- execute-spreadapi-request [credentials params]
  (let [{:keys [script-id key] :as creds} credentials
        api-url (str "https://script.google.com/macros/s/" script-id "/exec")]
    (try
      (let [response (client/post api-url
                                  {:form-params (assoc params :key key)
                                   :content-type :json
                                   :accept :json})
            body (-> response :body (json/parse-string true))]
        body)
      (catch Exception e
        (println (str "An error occurred: " (.getMessage e)))
        e))))

;;; Google Sheets API implementation
(defrecord GoogleSheets [credentials]
  SheetsAPI
  (get-sheet [this sheet-name params]
    (let [default-params {:method "GET" :sheet sheet-name}]
      (execute-spreadapi-request credentials (merge default-params params))))
  (update-sheet [this sheet-name params]
    (let [default-params {:method "PUT" :sheet sheet-name}]
      (execute-spreadapi-request credentials (merge default-params params)))))

(defn create-google-sheets-client [credentials]
  (GoogleSheets. credentials))
