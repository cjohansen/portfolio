(ns portfolio.components.arguments-panel
  (:require [dumdom.core :as d]))

(d/defcomponent ArgumentsPanel [data]
  [:div {:style {:padding 20}}
   [:table
    (for [{:keys [label value actions clear-actions]} (:args data)]
      [:tr
       [:td {:style {:padding-right 20}}
        [:label label]]
       [:td {:style {:padding "5px 20px 5px 0"}}
        [:input.input
         {:type "text"
          :on-input actions
          :value value}]]
       [:td
        (when clear-actions
          [:div
           {:on-click clear-actions
            :style {:width 24
                    :height 24
                    :background "#f0f0f0"
                    :cursor "pointer"
                    :border-radius "50%"
                    :font-weight "bold"
                    :text-align "center"
                    :line-height "1.5"
                    :color "#999"}}
           "x"])]])]])
