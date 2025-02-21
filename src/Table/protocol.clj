(ns table.protocol)

(defprotocol Table
  (get-sheet [this table-name] "Retrieves data from the specified sheet.")
  (update-row [this table-name index row] "Updates row in the specified sheet.")
  (update-rows [this table-name rows] "Updates rows in the specified sheet.")
  (insert-row [this table-name row] "Inserts a new row at end of sheet."))
