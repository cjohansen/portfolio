(ns portfolio.components.uix
  (:require [uix.core :refer [defui $ use-state]]
            [uix.dom]
            [portfolio.theme :as theme]
            [portfolio.react-18 :as p-react-18 :refer-macros [defscene]]))


(p-react-18/set-decorator! theme/react-18-decorator)
(defui counter []
  (let [[count set-count] (use-state 0)]
    ($ :div
       ($ :p "Count: " count)
       ($ :button {:on-click #(set-count inc)} "Increase"))))

(defscene uix-counter
  :title "Counter with React Hooks"
  ($ counter))

(defn decorator-consumer-component []
  (let [theme (theme/use-theme)]
    ($ :button {:style {:background (name theme)}}
       "current theme is " (name theme))))


(defscene decorator-consumer-demo
  :title "using global decorator context"
  ($ decorator-consumer-component))