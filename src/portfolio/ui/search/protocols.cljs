(ns portfolio.ui.search.protocols)

(defprotocol Index
  (index [self document])
  (query [self q]))

(defprotocol SearchResult
  (prepare-result [self state location result]))
