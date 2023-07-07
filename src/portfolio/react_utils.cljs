(ns portfolio.react-utils
  (:require [goog]
            ["react" :as react]
            [portfolio.ui.actions :as actions]
            [portfolio.adapter :as adapter]))

(defn create-scene [scene impl]
  (-> scene
      (update :component-fn (fn [f]
                              (fn [& args]
                                (react.createElement #(apply f args) #js {}))))
      (adapter/prepare-scene impl)))

(defn get-scene [this]
  (.. this -props -scene))

(defn create-safe-wrapper []
  (let [ctor (fn [])]
    (goog.inherits ctor react.Component)
    (set! (.-getDerivedStateFromError ctor)
          (fn [error]
            #js {:error error}))
    (specify! (.-prototype ctor)
      Object

      (componentDidCatch [this error info]
        (when-let [actions (:report-render-error (:actions (get-scene this)))]
          (actions/dispatch actions nil {:action/exception error
                                         :action/info (js->clj info)
                                         :action/cause "React error boundary caught error"})))

      (render [this]
        (.createElement
         react "div" #js {}
         (if (some-> this .-state .-error)
           ""
           (:component (get-scene this))))))
    ctor))
