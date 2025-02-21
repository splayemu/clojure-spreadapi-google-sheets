(ns table.spreadapi-test
  (:require [clojure.test :refer :all]
            [table.http :as http]
            [table.test-helpers.http :as test-helpers.http]
            [table.spreadapi :as spreadapi]))

(def script-id "test-script-id")
(def test-key "test-key")
(def table-name "test-table-name")

(deftest get-sheet-test
  (testing ""
    (let [spread-api-google-sheets (table.spreadapi/map->SpreadAPIGoogleSheets
                                    {:credentials {:script-id script-id
                                                   :key test-key}})]
      (binding [http/*http-request* (test-helpers.http/mock-redirect-then-data-response
                                     {"_id" 1
                                      "Derp meow" "hehe"})]
        (let [response (table.protocol/get-sheet spread-api-google-sheets table-name )]
          (is (= 1 response)))
        ))))

(deftest update-row-test
  (testing ""))

(deftest update-rows-test
  (testing ""))

(deftest insert-row-test
  (testing ""))
