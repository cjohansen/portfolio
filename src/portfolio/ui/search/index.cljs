(ns portfolio.ui.search.index)

(defprotocol Index
  (index [self document])
  (query [self q]))
