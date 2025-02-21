(ns table.http
  (:require [org.httpkit.client :as http-client]))

(def ^:dynamic *http-request* http-client/request)
