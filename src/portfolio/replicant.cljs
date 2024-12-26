(ns portfolio.replicant
  (:require [portfolio.adapter :as adapter]
            [portfolio.data :as data]
            [replicant.core :as hiccup]
            [replicant.dom :as replicant])
  (:require-macros [portfolio.replicant]))

::data/keep

(def render-options (atom nil))

(defn set-render-options! [opts]
  (reset! render-options opts))

(def component-impl
  {`adapter/render-component
   (fn [{:keys [component id updated-at]} el]
     (assert (some? el) "Asked to render Replicant component without an element.")
     (when-not (= "replicant" (.-unmountLib el))
       (set! (.-innerHTML el) "")
       (when-let [f (some-> el .-unmount)]
         (f)))
     (set! (.-unmountLib el) "replicant")
     (replicant/render el [:div {:replicant/key (str id "-" updated-at)} component] @render-options))})

(defn create-scene [scene]
  (adapter/prepare-scene scene component-impl))

(data/register-scene-renderer!
 (fn [x]
   (when-let [scene (cond
                      (hiccup/hiccup? x)
                      {:component x}

                      (hiccup/hiccup? (:component x))
                      x)]
     (create-scene scene))))
