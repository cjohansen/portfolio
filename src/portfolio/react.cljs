(ns portfolio.react
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data]
            [react :as react]
            [react-dom :as react-dom])
  (:require-macros [portfolio.react]))

(defn get-root [el]
  (when-not (.-reactRoot el)
    (set! (.-reactRoot el) (react-dom/createRoot el)))
  (.-reactRoot el))

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component]} el]
     (assert (some? el) "Asked to render component into null container.")
     (let [root (get-root el)]
       (.render root component)))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))
