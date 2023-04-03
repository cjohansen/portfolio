(ns portfolio.components.box
  (:require [portfolio.dumdom :refer-macros [defscene]]))

(defscene shadowed-box
  :icon :ui.icons/hamburger
  [:div {:style {:background "#fff"
                 :box-shadow "0px 4px 4px rgba(0, 0, 0, 0.03), 0px 8px 24px rgba(0, 0, 0, 0.05)"
                 :border-radius "8px"}}
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :cursor "pointer"
                  :padding "20px"}}
    "A nice, shadowed box"]])
