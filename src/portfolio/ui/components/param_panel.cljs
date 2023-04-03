(ns portfolio.ui.components.param-panel
  (:require [dumdom.core :as d]
            [portfolio.ui.components.toggle :refer [Toggle]]))

(d/defcomponent BooleanInput [{:keys [actions value]}]
  (Toggle {:actions actions
           :on-label "True"
           :off-label "False"
           :on? value}))

(d/defcomponent TextInput [{:keys [kind actions value]}]
  [:input.input
   {:type (name kind)
    :on-input actions
    :value value}])

(defn render-input [data]
  (case (:kind data)
    :boolean (BooleanInput data)
    :number (TextInput data)
    :text (TextInput data)
    (do
      (js/console.error
       "Rendering unknown param input type as text input"
       (clj->js data))
      (TextInput (assoc data :type :text)))))

(d/defcomponent ParamPanel [data]
  [:div {:style {:padding 20}}
   [:table
    (for [{:keys [label input clear-actions]} (:param data)]
      [:tr
       [:td {:style {:padding-right 20
                     :vertical-align "middle"}}
        [:label {:style {:font-family "monospace"}} label]]
       [:td {:style {:padding "5px 10px 5px 0"
                     :vertical-align "middle"}}
        (render-input input)]
       [:td {:style {:vertical-align "middle"}}
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
