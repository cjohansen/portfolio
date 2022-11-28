(ns portfolio.components.side-effecty
  (:require [dumdom.core :as d]))

(d/defcomponent SideEffecty
  :on-mount
  (fn [el data]
    (set! (.-innerHTML el) (str (:text data) "!")))
  [data]
  [:h1])
