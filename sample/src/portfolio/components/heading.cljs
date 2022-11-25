(ns portfolio.components.heading
  (:require [dumdom.core :as d]
            [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  :title "Heading"
  [:h1 "I am a heading"])

(d/defcomponent SideEffecty
  :on-mount
  (fn [el data]
    (set! (.-innerHTML el) (:text data)))
  [data]
  [:h1])

(defscene side-effecty-component-1
  :title "Side-effecty #1"
  (SideEffecty {:text "I am #1"}))

(defscene side-effecty-component-2
  :title "Side-effecty #2"
  (SideEffecty {:text "I am #2"}))
