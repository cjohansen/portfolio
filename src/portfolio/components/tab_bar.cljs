(ns portfolio.components.tab-bar
  (:require [dumdom.core :as d]))

(d/defcomponent TabBar [{:keys [tabs]}]
  [:nav {:style {:background "#fff"
                 :border-bottom "1px solid #e5e5e5"}}
   [:ul
    (for [{:keys [title url selected?]} tabs]
      (if selected?
        [:span {:style {:padding "10px 20px"
                        :display "inline-block"
                        :border-bottom "2px solid #1ea7fd"
                        :color "#1ea7fd"}}
         title]
        [:a {:style {:padding "10px 20px"
                     :color "#000"}
             :href url} title]))]])
