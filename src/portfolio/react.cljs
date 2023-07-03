(ns portfolio.react
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data]
            ["react" :as react]
            ["react-dom" :as react-dom])
  (:require-macros [portfolio.react]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component]} el]
     (assert (some? el) "Asked to render component into null container.")
     (when-let [f (some-> el .-unmount)]
       (f))
     (react-dom/render component el))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))

(data/register-scene-renderer!
 (fn [x]
   (when-let [scene (cond
                      (react/isValidElement x)
                      {:component x}

                      (react/isValidElement (:component x))
                      x)]
     (create-scene scene))))
