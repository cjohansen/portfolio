(ns portfolio.react
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data]
            [portfolio.react-utils :as react-util]
            ["react" :as react]
            ["react-dom" :as react-dom])
  (:require-macros [portfolio.react]))

::data/keep

(def component-impl
  {`adapter/render-component
   (fn [scene el]
     (assert (some? el) "Asked to render component into null container.")
     (when-let [f (some-> el .-unmount)]
       (f))
     (let [Wrapper (react-util/create-safe-wrapper)]
       (react-dom/render (react/createElement Wrapper #js {:scene scene}) el)))})

(defn create-scene [scene]
  (react-util/create-scene scene component-impl))

(data/register-scene-renderer!
 (fn [x]
   (when-let [scene (cond
                      (react/isValidElement x)
                      {:component x}

                      (react/isValidElement (:component x))
                      x)]
     (create-scene scene))))
