(ns portfolio.components.html
  (:require [portfolio.html :refer-macros [defscene defns]]))

(defns HTML
  :canvas/layout
  {:kind :rows
   :xs [{:viewport/width 390
         :viewport/height 500}
        {}]})

(defscene html-button
  :title "HTML string button"
  :param {:text "Hello, stringy!"}
  [{:keys [text]}]
  (str "<button>" text "</button>"))

(defscene inline-html-button
  :title "Inline HTML string button"
  "<button>Hello!</button>")
