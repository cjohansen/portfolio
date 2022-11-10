(ns portfolio.rum
  (:require [portfolio.scene :as scene]))

(defmacro defscene [id & opts]
  `(portfolio.data/register-scene!
    (portfolio.rum/create-scene
     ~(scene/get-options-map id opts))))
