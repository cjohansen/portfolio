(ns portfolio.ui.components.document
  (:require [dumdom.core :as d]
            [portfolio.ui.components.markdown :refer [Markdown]]))

(d/defcomponent Document [{:keys [title sections]}]
  [:div.document.dark.contrast {:style {:padding 20}}
   [:h1.h1 title]
   (for [section sections]
     (case (:kind section)
       :markdown (Markdown section)))])
