(ns sheets-api.core
  (:require [clojure.string :as str]
            [org.httpkit.client :as http]
            [clojure.walk :as walk]
            [cheshire.core :as json])
  (:import [java.net URI URLEncoder]))

;;; Protocol definition
(defprotocol SheetsAPI
  (get-sheet [this sheet-name] "Retrieves data from the specified sheet.")
  (update-row [this sheet-name index row] "Updates row in the specified sheet.")
  (update-rows [this sheet-name rows] "Updates rows in the specified sheet.")
  (insert-row [this sheet-name row] "Inserts a new row at end of sheet."))

(defn ^:private redirect? 
  "Checks if the given HTTP status code indicates a redirect."
  [status]
  (#{301 302 303 307 308} status))

(defn ^:private execute-redirect
  "Executes a GET request to the redirect URL."
  [{:keys [headers]}]
  (let [redirect-url (get headers "location")
        redirect-location (str (.resolve (java.net.URI. redirect-url)))]
    @(http/request {:method :get
                    :url redirect-url})))

(defn ^:private remove-sheets-keys
  "Removes keywords like :sheets/index."
  [m]
  (into {} (remove (fn [[k _]] (and (keyword? k) (= "sheets" (namespace k))))
                   m)))

(defn ^:private execute-spreadapi-request
  "Executes a request against the Spread API, following redirects if necessary."
  [credentials body]
  (let [{:keys [script-id key] :as creds} credentials
        api-url (str "https://script.google.com/macros/s/" script-id "/exec")]
    (try
      (let [body (-> (if (sequential? body)
                       (mapv #(assoc % :key key) body)
                       (assoc body :key key))
                     ((partial walk/postwalk #(if (map? %) (remove-sheets-keys %) %))))
            response @(http/request {:method :post
                                     :url api-url
                                     :body (json/encode body)
                                     :headers {"content-type" "application/json"}})]
        (-> (if (redirect? (:status response))
              (execute-redirect response)
              response)
            :body
            (json/decode keyword)
            ((partial walk/postwalk-replace {:_id :sheets/index}))))
      (catch Exception e
        (println (str "An error occurred: " (.getMessage e)))
        e))))

;;; Google Sheets API implementation
(defrecord SpreadAPIGoogleSheets [credentials]
  SheetsAPI
  (get-sheet [this sheet-name]
    (let [body {:method "GET" :sheet sheet-name}]
      (execute-spreadapi-request credentials body)))
  (update-row [this sheet-name index row]
    (let [body {:method "PUT" :sheet sheet-name :id index :payload row}]
      (execute-spreadapi-request credentials body)))
  (update-rows [this sheet-name rows]
    (let [body (vec (for [{:sheets/keys [index] :as row} rows]
                      {:method "PUT" :sheet sheet-name :id index :payload row}))]
      (execute-spreadapi-request credentials body)))
  (insert-row [this sheet-name row]
    (let [body {:method "POST" :sheet sheet-name :payload row}]
      (execute-spreadapi-request credentials body))))

(defn create-spread-api-google-sheets-client [credentials]
  (map->SpreadAPIGoogleSheets {:credentials credentials}))


(comment


  (def tm (-> (create-spread-api-google-sheets-client {:script-id "AKfycbw4W8LbxBl-9eWP_2pooXHBiCsezNYsrQLhkxzxhTzNOBfz2noRGFJTwjJgHi0M03y4"
                                     :key "j5UnmpJ6sr24o8QkMph4FzNOK2nbUjhEmILew11HNiF7zm7rsu#"})
       (get-sheet "Lift Log") 
       ))

(-> (create-spread-api-google-sheets-client {:script-id "AKfycbw4W8LbxBl-9eWP_2pooXHBiCsezNYsrQLhkxzxhTzNOBfz2noRGFJTwjJgHi0M03y4"
                                     :key "j5UnmpJ6sr24o8QkMph4FzNOK2nbUjhEmILew11HNiF7zm7rsu#"})
    (update-rows "Lift Log" (:data tm))
       )

(-> (create-google-sheets-client {:script-id "AKfycbw4W8LbxBl-9eWP_2pooXHBiCsezNYsrQLhkxzxhTzNOBfz2noRGFJTwjJgHi0M03y4"
                                    :key "j5UnmpJ6sr24o8QkMph4FzNOK2nbUjhEmILew11HNiF7zm7rsu#"})
    (insert-row  "Lift Log" {:Date "2025-02-15T08:00:00.000Z",
                             :Lift "Bench Press",
                             :Weight 175,
                             :Repititions 3,
                             :Rounds "",
                             :Scheme ""})
      )

  )
