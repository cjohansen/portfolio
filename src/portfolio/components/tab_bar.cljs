(ns portfolio.components.tab-bar
  (:require [dumdom.core :as d]))

(d/defcomponent TabBar [{:keys [tabs]}]
  [:nav
   [:ul
    (for [{:keys [title url selected?]} tabs]
      (if selected?
        [:span {:style {:padding "10px 20px"
                        :display "inline-block"
                        :border-bottom "2px solid var(--azure-radiance)"
                        :color "var(--azure-radiance)"}}
         title]
        [:a {:style {:padding "10px 20px"
                     :color "#fff"}
             :href url} title]))]])
