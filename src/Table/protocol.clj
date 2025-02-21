(ns Table.protocol)

(defprotocol SheetsAPI
  (get-sheet [this sheet-name] "Retrieves data from the specified sheet.")
  (update-row [this sheet-name index row] "Updates row in the specified sheet.")
  (update-rows [this sheet-name rows] "Updates rows in the specified sheet.")
  (insert-row [this sheet-name row] "Inserts a new row at end of sheet."))
