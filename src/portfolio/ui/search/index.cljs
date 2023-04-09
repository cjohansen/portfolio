(ns portfolio.ui.search.index)

(defprotocol Index
  (index-document [self document])
  (search [self q]))
