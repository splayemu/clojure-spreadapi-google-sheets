(ns user
  (:require [clojure.repl :refer :all]))

(comment

  (require '[table.protocol])
  (require '[table.spreadapi])

  (def credentials
    {:script-id "script-id"
     :key "key"})

  (def table
    (-> (table.spreadapi/create-spread-api-google-sheets-client credentials)
        (table.protocol/get-sheet "Lift Log")
        ))

  (-> (table.spreadapi/create-spread-api-google-sheets-client credentials)
      (table.protocol/update-rows "Lift Log" (get table "data")))

(-> (table.spreadapi/create-spread-api-google-sheets-client credentials)
      (table.protocol/update-row "Lift Log" 10 {"Lift" 1}))

(-> (table.spreadapi/create-spread-api-google-sheets-client credentials)
    (table.protocol/insert-row  "Lift Log" {:Date "2025-02-15T08:00:00.000Z",
                             :Lift "Bench Press",
                             :Weight 175,
                             :Repititions 3,
                             :Rounds "",
                             :Scheme ""})
      )

  )
