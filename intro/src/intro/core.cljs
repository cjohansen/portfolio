(ns intro.core
  (:require [portfolio.ui :as portfolio]
            [portfolio.dumdom :refer-macros [defscene]]
            ))

(defscene hello
  [:div "Hello there!"])

(portfolio/start!
 {:config {:log? true}})
