(ns portfolio.components.html
  (:require [portfolio.html :refer-macros [defscene]]))

(defscene html-button
  :title "HTML string button"
  :args {:text "Hello, stringy!"}
  [{:keys [text]}]
  (str "<button>" text "</button>"))
