(ns portfolio.react-18
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data]
            [portfolio.react-utils :as react-util]
            ["react" :as react]
            ["react-dom/client" :as react-dom])
  (:require-macros [portfolio.react-18]))

::data/keep

(defn get-root [el]
  (when-not (.-reactRoot el)
    (set! (.-reactRoot el) (react-dom/createRoot el)))
  (.-reactRoot el))

(def Wrapper (react-util/create-safe-wrapper))

(def component-impl
  {`adapter/render-component
   (fn [scene el]
     (assert (some? el) "Asked to render component into null container.")
     (when-let [f (some-> el .-unmount)]
       (when-not (= "react18" (.-unmountLib el))
         (f)))
     (let [root (get-root el)]
       (set! (.-unmount el) (fn []
                              (.unmount root)
                              (set! (.-reactRoot el) nil)
                              (set! (.-innerHTML el) "")
                              (set! (.-unmount el) nil)))
       (set! (.-unmountLib el) "react18")
       (.render root (react.createElement Wrapper #js {:scene scene}))))})

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
