(ns portfolio.components.html
  (:require [portfolio.html :refer-macros [defscene]]))

(defscene html-button
  :title "HTML string button"
  :param {:text "Hello, stringy!"}
  [{:keys [text]}]
  (str "<button>" text "</button>"))

(defscene inline-html-button
  :title "Inline HTML string button"
  "<button>Hello!</button>")
