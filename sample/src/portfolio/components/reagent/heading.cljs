(ns portfolio.components.reagent.heading)

(defn heading [{:keys [text border-width]}]
  [:h1 {:style {:background "yellow"
                :transition "border-width 0.25s"
                :border-color "#000"
                :border-style "solid"
                :border-width (or border-width 0)}} text])
