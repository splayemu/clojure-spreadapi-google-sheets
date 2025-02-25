(ns table.spreadapi
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [cheshire.core :as json]
            [table.http :as http]
            [table.protocol :as protocol])
  (:import [java.net URI URLEncoder]))

(defn ^:private redirect? 
  "Checks if the given HTTP status code indicates a redirect."
  [status]
  (#{301 302 303 307 308} status))

(defn ^:private fetch-credentials
  "Fetches credentials, handling both map and function types."
  [credentials]
  (if (fn? credentials) (credentials) credentials))

(defn ^:private execute-redirect
  "Executes a GET request to the redirect URL."
  [{:keys [opts headers]}]
  (let [redirect-url (get headers :location)
        redirect-location (str (.resolve (java.net.URI. (:url opts)) redirect-url))]
    @(http/*http-request* {:method :get
                           :url redirect-url})))

(defn ^:private remove-sheets-keys
  "Removes keywords like :sheets/index."
  [m]
  (into {} (remove (fn [[k _]] (and (keyword? k) (= "sheets" (namespace k))))
                   m)))

(defn ^:private clean-map
  "Removes :sheets/ keys from a map."
  [m]
  (walk/postwalk #(if (map? %) (remove-sheets-keys %) %) m))

(defn ^:private add-key-to-body
  "Adds the API key to the request body."
  [body key]
  (if (sequential? body)
    (mapv #(assoc % :key key) body)
    (assoc body :key key)))

(defn ^:private replace-id-with-index
  "Replaces :_id with :sheets/index in a map."
  [m]
  (walk/postwalk-replace {"_id" :sheets/index} m))

(defn ^:private execute-spreadapi-request
  "Executes a request against the Spread API, following redirects if necessary."
  [credentials body]
  (let [{:keys [script-id key]} (fetch-credentials credentials)
        api-url (str "https://script.google.com/macros/s/" script-id "/exec")]
    (try
      (let [body (-> body
                     (add-key-to-body key)
                     clean-map)
            response @(http/*http-request* {:method :post
                                            :follow-redirects false
                                            :url api-url
                                            :body (json/encode body)
                                            :headers {"content-type" "application/json"}})]
        (-> (if (redirect? (:status response))
              (execute-redirect response)
              response)
            :body
            json/decode
            replace-id-with-index))
      (catch Exception e
        (println (str "An error occurred: " (.getMessage e)))
        e))))

;;; Google Sheets API implementation
(defrecord SpreadAPIGoogleSheets [credentials]
  protocol/Table
  (get-sheet [this table-name]
    (let [body {:method "GET" :sheet table-name}]
      (execute-spreadapi-request credentials body)))
  ;; Note that this won't update any attributes that don't exist as columns already
  (update-row [this table-name index row]
    (let [body {:method "PUT" :sheet table-name :id index :payload row}]
      (execute-spreadapi-request credentials body)))
  (update-rows [this table-name rows]
    (let [body (vec (for [{:sheets/keys [index] :as row} rows]
                      {:method "PUT" :sheet table-name :id index :payload row}))]
      (execute-spreadapi-request credentials body)))
  (insert-row [this table-name row]
    (let [body {:method "POST" :sheet table-name :payload row}]
      (execute-spreadapi-request credentials body))))

(defn create-spread-api-google-sheets-client
  "Creates a SpreadAPI Google Sheets client.

  Credentials can be a map containing :script-id and :key, or a function that returns such a map."
  [credentials]
  (map->SpreadAPIGoogleSheets {:credentials credentials}))

(defn ->credentials
  [spread-api-google-sheets]
  (fetch-credentials (:credentials spread-api-google-sheets)))
