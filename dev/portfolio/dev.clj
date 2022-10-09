(ns portfolio.dev
  (:require [figwheel.main]
            [figwheel.main.api]))

(defn cljs []
  (if (get @figwheel.main/build-registry "dev")
    (figwheel.main.api/cljs-repl "dev")
    (figwheel.main.api/start "dev")))
