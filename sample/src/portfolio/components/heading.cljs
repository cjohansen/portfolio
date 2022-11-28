(ns portfolio.components.heading
  (:require [portfolio.dumdom :refer-macros [defscene]]
            [portfolio.components.side-effecty :refer [SideEffecty]]))

(defscene default
  :title "Heading"
  [:h1 "I am a heading"])

(defscene side-effecty-component-1
  :title "Side-effecty #1"
  (SideEffecty {:text "I am #1"}))

(defscene side-effecty-component-2
  :title "Side-effecty #2"
  (SideEffecty {:text "I am #2"}))
