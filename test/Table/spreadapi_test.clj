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
  (testing "update multiple rows in a sheet"
    (let [rows [{:sheets/index 5 "column1" "new value" "column2" "another value"}
                {:sheets/index 6 "column1" "yet another value" "column2" "and another"}]
          spread-api-google-sheets (table.spreadapi/map->SpreadAPIGoogleSheets
                                    {:credentials {:script-id script-id
                                                   :key test-key}})]
      (binding [http/*http-request*
                (test-helpers.http/mock-redirect-then-data-response
                 [{"status" 201}
                  {"status" 201}]
                 (redirect-assertions {:method "PUT" :sheet table-name :key test-key :payload (mapv #(dissoc % :sheets/index) rows)})
                 data-assertions)]
        (let [response (table.protocol/update-rows spread-api-google-sheets table-name rows)]
          (is (= [{"status" 201}
                  {"status" 201}] response)))))))

(deftest insert-row-test
  (testing "insert a row in a sheet"
    (let [row {"column1" "new value" "column2" "another value"}
          spread-api-google-sheets (table.spreadapi/map->SpreadAPIGoogleSheets
                                    {:credentials {:script-id script-id
                                                   :key test-key}})]
      (binding [http/*http-request*
                (test-helpers.http/mock-redirect-then-data-response
                 {"status" 201}
                 (redirect-assertions {:method "POST" :sheet table-name :payload row :key test-key})
                 data-assertions)]
        (let [response (table.protocol/insert-row spread-api-google-sheets table-name row)]
          (is (= {"status" 201} response)))))))
