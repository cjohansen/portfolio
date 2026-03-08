(ns ^{:dev/always true} portfolio.runner
  (:require [portfolio.ui :as ui])
  (:require-macros portfolio.runner))

(defn ^:export start []
  (ui/start!
   {:config (portfolio.runner/get-compiler-portfolio-config)}))

(start)
