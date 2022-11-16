(ns portfolio.scenes
  (:require [portfolio.components.button]
            [portfolio.components.dom]
            [portfolio.components.heading]
            [portfolio.components.html]
            [portfolio.components.link]
            [portfolio.components.reagent]
            [portfolio.components.rum]
            [portfolio.layouts.home-page]
            [portfolio.ui :as ui]))

(ui/start!
 {:config {:css-paths ["/portfolio/demo.css"]
           ;;:canvas/layout [[{:background/background-color "#f0f0f9"}]]

           :background/options
           [{:id :light-mode
             :title "Bright mode (.bright-mode)"
             :value {:background/background-color "#f8f8f8"
                     :background/body-class "light-mode"}}
            {:id :dark-mode
             :title "Bleak mode (.bleak-mode)"
             :value {:background/background-color "#000000"
                     :background/body-class "dark-mode"}}]

           :background/default-option-id :dark-mode

           }})
