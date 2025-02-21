(ns table.spreadapi-test
  (:require [clojure.test :refer :all]
            [table.http :as http]
            [table.test-helpers.http :as test-helpers.http]
            [table.spreadapi :as spreadapi]))

(def script-id "test-script-id")
(def test-key "test-key")
(def table-name "test-table-name")

(defn redirect-assertions
  [body]
  (fn [{:keys [method url body follow-redirects]}]
    (is (clojure.string/includes? url script-id))
    (is (= method :post))
    (is (= follow-redirects false))
    (is (= body body))))

(defn data-assertions
  [{:keys [method url]}]
  (is (= method :get))
  (is (= url test-helpers.http/redirect-location)))

(deftest get-sheet-test
  (testing "get rows from a sheet"
    (let [spread-api-google-sheets (table.spreadapi/map->SpreadAPIGoogleSheets
                                    {:credentials {:script-id script-id
                                                   :key test-key}})]
      (binding [http/*http-request*
                (test-helpers.http/mock-redirect-then-data-response
                 {"status" 200
                  "data" {"_id" 1
                          "Derp meow" "hehe"}}
                 (redirect-assertions {:method "GET" :sheet table-name :key test-key})
                 data-assertions)]
        (let [response (table.protocol/get-sheet spread-api-google-sheets table-name)]
          (is (= {"status" 200
                  "data" {:sheets/index 1
                          "Derp meow" "hehe"}}
                 response)))))))

(deftest update-row-test
  (testing "update a row in a sheet"
    (let [index 5
          row {"column1" "new value" "column2" "another value"}
          spread-api-google-sheets (table.spreadapi/map->SpreadAPIGoogleSheets
                                    {:credentials {:script-id script-id
                                                   :key test-key}})]
      (binding [http/*http-request*
                (test-helpers.http/mock-redirect-then-data-response
                 {"status" 201}
                 (redirect-assertions {:method "PUT" :sheet table-name :id index :payload row :key test-key})
                 data-assertions)]
        (let [response (table.protocol/update-row spread-api-google-sheets table-name index row)]
          (is (= {"status" 201} response)))))))

(deftest update-rows-test
  (testing ""))

(deftest insert-row-test
  (testing ""))
