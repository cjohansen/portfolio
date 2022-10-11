(ns portfolio.components.arguments-panel
  (:require [dumdom.core :as d]
            [portfolio.components.toggle :refer [Toggle]]))

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
       "Rendering unknown argument input type as text input"
       (clj->js data))
      (TextInput (assoc data :type :text)))))

(d/defcomponent ArgumentsPanel [data]
  [:div {:style {:padding 20
                 :display "flex"}}
   [:div {:style {:flex "1 0 50%"}}
    [:table
     (for [{:keys [label input clear-actions]} (:args data)]
       [:tr
        [:td {:style {:padding-right 20
                      :vertical-align "middle"}}
         [:label label]]
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
            "x"])]])]]
   (when-let [{:keys [title items]} (:arg-list data)]
     [:div {:style {:flex "1 0 50%"}}
      [:h2.h3 title]
      [:ul
       (for [{:keys [actions text]} items]
         [:li.hoverable
          {:on-click actions
           :style {:padding "5px 0"}}
          [:pre {:style {:font-family "monospace"}}
           text]])]])])
