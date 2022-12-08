(ns portfolio.components.dom
  (:require [portfolio.dom :refer-macros [defscene]]))

(defscene dom-button
  :title "DOM element button"
  :param {:text "Hello, DOM!"}
  [{:keys [text]}]
  (let [el (js/document.createElement "button")]
    (set! (.. el -style -border) "2px solid red")
    (set! (.-innerHTML el) text)
    el))

(defscene inline-dom-button
  :title "Inline DOM button"
  (let [el (js/document.createElement "button")]
    (set! (.. el -style -border) "2px solid red")
    (set! (.-innerHTML el) "Hello, inline DOM!")
    el))
