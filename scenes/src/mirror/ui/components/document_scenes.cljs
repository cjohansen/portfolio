(ns mirror.ui.components.document-scenes
  (:require [portfolio.dumdom :as portfolio :refer-macros [defscene]]
            [portfolio.ui.components.document :refer [Document]]
            [portfolio.ui.document :as intro]))

(portfolio/configure-scenes
 {:title "Documents"})

(defscene setup-good
  (Document (intro/prepare-view {} {} :document/up-and-running)))
