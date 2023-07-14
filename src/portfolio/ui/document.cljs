(ns portfolio.ui.document
  (:require [portfolio.ui.view :as view]
            [portfolio.ui.components.document :refer [Document]]
            [portfolio.ui.docs :as docs]))

(def docs
  {:document/up-and-running (docs/prepare-doc (docs/load-doc "up-and-running.md"))})

(defn get-document [id]
  (when-let [doc (get docs id)]
    (assoc doc :id id)))

(defn prepare-view [state location document]
  (when document
    (with-meta document {`view/render-view #'Document})))
