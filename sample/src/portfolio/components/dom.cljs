(ns portfolio.components.dom
  (:require [portfolio.dom :refer-macros [defscene]]))

(defscene dom-button
  :title "DOM element button"
  :args {:text "Hello, DOM!"}
  [{:keys [text]}]
  (let [el (js/document.createElement "button")]
    (set! (.. el -style -border) "2px solid red")
    (set! (.-innerHTML el) text)
    el))
