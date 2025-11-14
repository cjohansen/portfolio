(ns portfolio.replicant
  (:require [portfolio.adapter :as adapter]
            #?(:clj [portfolio.core :as portfolio])
            [portfolio.data :as data]
            [replicant.dom :as replicant]
            [replicant.hiccup :as hiccup])
  #?(:cljs (:require-macros [portfolio.replicant])))

#?(:clj
   (defmacro defscene [id & opts]
     (when (portfolio.core/portfolio-active?)
       `(portfolio.data/register-scene!
         (portfolio.replicant/create-scene
          ~(portfolio.core/get-options-map id (:line &env) opts))))))

#?(:clj
   (defmacro configure-scenes [& opts]
     (when (portfolio.core/portfolio-active?)
       `(portfolio.data/register-collection!
         ~@(portfolio.core/get-collection-options opts)))))

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
