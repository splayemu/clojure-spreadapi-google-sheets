(ns table.test-helpers.http
  (:require [clojure.test :refer :all]
            [cheshire.core :as json]))

(def redirect-location "https://script.googleusercontent.com/macros/echo?user_content_key=P2YRKE8GZtFuGyI8trGjdGD3chfApW_giFam9E3rygmzpOaL-85KHoaE4pSkfn5ktQ4pciQHGxO8sCAhe3wWQY7r9-ABv4Kpm5_BxDlH2jW0nuo2oDemN9CCS2h10ox_1xSncGQajx_ryfhECjZEnHtl5mwPlvYU42gRyGx9bDeKeeaUSfy-6q9bPUn49Pw7CFILEUQ5CCkmvrZrl0iN6wxh_JTIyLY5PY9K4OxYTVAuQOTXNUegWQ&lib=M1gEZzFjQU-h_Zf89XSMUfp8NR7mQDIq9")

(defn mock-http-response
  [request {:keys [status body headers]}]
  (delay
    {:opts request
     :headers headers
     :status status
     :body body}))

(defn mock-redirect-response
  [request]
  (mock-http-response
   request
   {:status 302
    :body ""
    :headers {:location redirect-location}}))

(defn mock-data-response
  [request body]
  (mock-http-response
   request
   {:body (json/encode body)
    :status 200
    :headers {:content-type "application/json; charset=utf-8"}}))

(defn mock-redirect-then-data-response
  [body redirect-assertions data-assertions]
  (let [calls (atom [])]
    (fn [r]
      (swap! calls conj r)
      (cond
        (= (count @calls) 1)
        (do
          (redirect-assertions (update r :body json/decode keyword))
          (mock-redirect-response r))

        (= (count @calls) 2)
        (do
          (data-assertions (update r :body json/decode keyword))
          (mock-data-response r body))

        :else
        (mock-http-response r {:status 500})))))
