(ns portfolio.components.reagent.component
  (:require [portfolio.components.reagent.title :as title]))

(defn component [props]
  [title/title props])
