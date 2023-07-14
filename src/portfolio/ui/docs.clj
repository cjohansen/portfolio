(ns portfolio.ui.docs
  (:require [clojure.java.io :as io]))

(defmacro load-doc [file]
  (slurp (io/resource (str "portfolio/" file))))

(comment

  (io/resource (str "portfolio/up-and-running.md"))

)
