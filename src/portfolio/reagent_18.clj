(ns portfolio.reagent-18
  (:require [portfolio.core :as portfolio]
            [portfolio.data :as data]))

(defmacro defscene [id & opts]
  (when (portfolio/portfolio-active?)
    `(portfolio.data/register-scene!
      (portfolio.reagent-18/create-scene
       ~(portfolio/get-options-map id (:line &env) opts)))))

(defmacro configure-scenes [& opts]
  (when (portfolio/portfolio-active?)
    `(portfolio.data/register-collection!
      ~@(portfolio/get-collection-options opts))))
