(ns portfolio.ui.document
  (:require [portfolio.ui.view :as view]
            [portfolio.ui.components.document :refer [Document]]
            [portfolio.ui.docs :as docs]))

(def docs
  {:document/up-and-running (docs/prepare-doc (docs/load-doc "up-and-running.md"))
   :document/defscene (docs/prepare-doc (docs/load-doc "defscene.md"))
   :document/organization (docs/prepare-doc (docs/load-doc "organization.md"))
   :document/custom-css (docs/prepare-doc (docs/load-doc "custom-css.md"))
   :document/custom-html (docs/prepare-doc (docs/load-doc "custom-html.md"))
   :document/customize-ui (docs/prepare-doc (docs/load-doc "customize-ui.md"))
   :document/index (docs/prepare-doc (docs/load-doc "index.md"))})

(defn get-document [id]
  (when-let [doc (get docs id)]
    (assoc doc :id id)))

(defn prepare-view [state location document]
  (when document
    (with-meta document {`view/render-view #'Document})))
