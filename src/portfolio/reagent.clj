(ns portfolio.reagent
  (:require [portfolio.scene :as scene]))

(defmacro defscene [id & opts]
  `(portfolio.data/register-scene!
    (portfolio.reagent/create-scene
     ~(scene/get-options-map id opts))))
