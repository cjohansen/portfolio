(ns portfolio.components.link
  (:require [portfolio.dumdom :refer-macros [defscene]]))

(defscene default
  :title "Link"
  [:a {:href "#"} "I am a link"])
