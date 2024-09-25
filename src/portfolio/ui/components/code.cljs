(ns portfolio.ui.components.code
  (:require [dumdom.core :as d]))

(d/defcomponent Code
  :keyfn :code
  :on-render (fn [el]
               (when js/window.Prism
                 (js/Prism.highlightElement el)))
  [{:keys [code]}]
  [:pre.language-clojure {:style {:font-family "monospace"}}
   code])
