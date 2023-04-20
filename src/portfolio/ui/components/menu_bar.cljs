(ns portfolio.ui.components.menu-bar
  (:require [dumdom.core :as d]
            [portfolio.ui.icons :as icons]))

(d/defcomponent MenuBar [{:keys [title action illustration size]}]
  [:div {:style {:display "flex"
                 :gap 20
                 :align-items "center"}}
   [:div {:style {:display "flex"
                  :gap 10
                  :align-items "center"}}
    (when illustration
      (icons/render
       (:icon illustration)
       {:size (if (= :small size) 16 24)
        :color (:color illustration)}))
    [:h1 {:class (if (= :small size) :h4 :h3)
          :style {:display "flex"
                  :align-items "center"
                  :gap 8}}
     (->> (for [{:keys [text url]} title]
            (if url
              [:a {:style {:color "var(--subdued-link)"}
                   :href url} text]
              text))
          (interpose
           (icons/render
            ::icons/caret-right
            {:size 16})))]]
   (when (:icon action)
     (icons/render
      (:icon action)
      {:size 16
       :on-click (:actions action)}))])
