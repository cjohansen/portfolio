(ns portfolio.components.html
  (:require [portfolio.html :as portfolio :refer-macros [defscene]]))

(portfolio/configure-scenes
  {:title "HTML"})

(defscene html-button
  :title "HTML string button"
  :param {:text "Hello, stringy!"}
  [{:keys [text]}]
  (str "<button>" text "</button>"))

(defscene inline-html-button
  :title "Inline HTML string button"
  "<button>Hello!</button>")
