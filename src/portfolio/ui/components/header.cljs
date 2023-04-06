(ns portfolio.ui.components.header
  (:require [dumdom.core :as d]
            [portfolio.ui.components.browser :as browser]
            [portfolio.ui.components.elastic-container :as ec]
            [portfolio.ui.icons :as icons]))

(defn render-action [action]
  (when (:icon action)
    (icons/render-icon
     (:icon action)
     {:size 16
      :on-click (:actions action)})))

(d/defcomponent HeaderMenu
  :will-enter (ec/enter)
  :will-leave (ec/leave)
  [{:keys [items]}]
  (browser/render-items items))

(d/defcomponent Header [{:keys [illustration title left-action right-action menu]}]
  [:div
   [:div {:style {:display "flex"
                  :gap 20
                  :flex-shrink "0"
                  :transition "height 0.25s ease"
                  :height 0
                  :overflow "hidden"
                  :align-items "center"
                  :padding 20
                  :border-bottom "1px solid var(--header-border)"}
          :mounted-style {:height 56}
          :leaving-style {:height 0}}
    (render-action left-action)
    [:div {:style {:display "flex" :gap 10}}
     (when illustration
       (icons/render-icon
        (:icon illustration)
        {:size 24
         :color (:color illustration)}))
     [:h1.h3 {:style {:display "flex"
                      :align-items "center"
                      :gap 8}}
      (->> (for [{:keys [text url]} title]
             (if url
               [:a {:style {:color "var(--subdued-link)"}
                    :href url} text]
               text))
           (interpose
            (icons/render-icon
             :ui.icons/caret-right
             {:size 16})))]]
    (render-action right-action)]
   (when menu
     (HeaderMenu menu))])
