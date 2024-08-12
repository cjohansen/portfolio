(ns portfolio.components.rum
  (:require #_[portfolio.rum :refer-macros [defscene]] ;; For react versions 18+ use the following:
 ;; Beware newer versions of react may not work as expected rum is
 ;; not actively maintained https://github.com/tonsky/rum/issues/165
 ;; For react versions 18+ use the following:
 ;; Beware newer versions of react may not work as expected rum is
            [portfolio.decorator :refer [use-theme]]
            [portfolio.react-18 :refer-macros [defscene]]
            [rum.core :as rum] ;; If you are using an older version of react use the following:
))

(rum/defc button [text]
  [:button.button text])

(defscene standard-button
  (button "I am a Rum button"))

(rum/defc decorator-consumer-component []
  (let [theme (use-theme)]
    [:button {:style {:background (name theme)}}
     "current theme is " (name theme)]))


(defscene decorator-consumer-demo
  (decorator-consumer-component))