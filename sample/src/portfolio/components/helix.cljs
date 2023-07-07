(ns portfolio.components.helix
  (:require [goog]
            [helix.core :refer [defnc $]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            ;; If you are using an older version of react use the following:
            #_[portfolio.react :refer-macros [defscene]]
            ;; For react versions 18+ use the following:
            [portfolio.react-18 :refer-macros [defscene]]
            ["react" :as react]))

(defnc counter []
  (let [[count set-count] (hooks/use-state 0)]
    (d/div
     (d/p "Count: " count)
     (d/button {:on-click #(set-count inc)} "Increase"))))

(defscene helix-counter
  :title "Counter with React Hooks"
  ($ counter))

(defnc bogus [children]
  (d/div children))

(defscene helix-error
  :title "Helix component error"
  (bogus {:error "Oops"}))

(def bogus-component
  (let [ctor (fn [])]
    (goog.inherits ctor react.Component)
    (specify! (.-prototype ctor)
      Object
      (render [this]
        (when (.. this -props -error)
          (throw (js/Error. "BOOOOOM!")))
        "Oh, nice!!"))
    ctor))

(defscene react-error
  :title "React render error"
  :params (atom {:error false})
  :on-mount (fn [params]
              (when-not (:tick @params)
                (swap! params assoc :tick (js/setInterval #(swap! params update :error not) 2000))))
  :on-unmount (fn [params]
                (js/clearInterval (:tick @params)))
  [params]
  (react.createElement bogus-component (clj->js @params)))

(defn HookExample
  [props]
  (let [^js [date _set-date] (react.useState (js/Date.))]
    (react.createElement "div" nil (str date))))

(defscene indirect-hook
  (react.createElement HookExample))

(defscene inline-hook
  (let [^js [date _set-date] (react.useState (js/Date.))]
    (react.createElement "div" nil (str date))))
