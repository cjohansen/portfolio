(ns portfolio.dom
  (:require [portfolio.core :as portfolio]))

(defmacro defscene [id & opts]
  (when (portfolio/portfolio-active?)
    `(portfolio.data/register-scene!
      (portfolio.dom/create-scene
       ~(portfolio/get-options-map id (:line &env) opts)))))

(defmacro configure-scenes [opts]
  (when (portfolio/portfolio-active?)
    `(portfolio.data/register-collection!
      ~@(portfolio/get-collection-options opts))))
