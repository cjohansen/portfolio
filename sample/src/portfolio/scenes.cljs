(ns portfolio.scenes
  (:require [gadget.inspector :as inspector]
            [portfolio.components.box]
            [portfolio.components.button]
            [portfolio.components.dom]
            [portfolio.components.heading]
            [portfolio.components.helix]
            [portfolio.components.html]
            [portfolio.components.link]
            [portfolio.components.reagent]
            [portfolio.components.replicant]
            [portfolio.components.rum]
            [portfolio.components.uix]
            [portfolio.layouts.home-page]
            [portfolio.layouts.responsive-page]
            [portfolio.layouts.tall-page]
            [portfolio.ui :as ui]
            [portfolio.ui.search :as search]
            [portfolio.ui.search.memsearch-index :as memsearch]))

(defonce app
  (ui/start!
   {:on-render (fn [page-data]
                 (inspector/inspect "Page data" page-data))

    :index (search/create-index)
    ;;:index (memsearch/create-index)

    :config
    {:css-paths ["/portfolio/demo.css"]
     :log? true

     ;;:title "Portfolio"

     #_#_:canvas/gallery-defaults {:viewport/width 390
                                   :viewport/height 400}

     :viewport/defaults {:viewport/padding [16]
                         #_#_#_#_:viewport/width 390
                         :viewport/height 400}
     #_#_:canvas/layout {:kind :cols
                         :xs [{:viewport/width 390
                               :viewport/height "100%"}
                              {:kind :rows
                               :xs [{:viewport/width 390
                                     :viewport/height 400}
                                    {:viewport/width 390
                                     :viewport/height 400}]}]}

     ;;:canvas/layout [[{:background/background-color "#ff3300 "}]]

     :grid/options [{:title "8 x 8px"
                     :value {;;:grid/offset 8
                             :grid/size 8
                             :grid/group-size 8}}
                    {:title "No grid"
                     :value {:grid/size 0}}]

     ;; :background/options
     ;; [{:id :light-mode
     ;;   :title "Bright mode (.bright-mode)"
     ;;   :value {:background/background-color "#f8f8f8"
     ;;           :background/body-class "light-mode"}}
     ;;  {:id :dark-mode
     ;;   :title "Bleak mode (.bleak-mode)"
     ;;   :value {:background/background-color "#000000"
     ;;           :background/body-class "dark-mode"}}]

     ;; :background/default-option-id :dark-mode
     }}))

(swap! app update ::heartbeat (fnil inc 0))

(comment

  (->> [:set-css-files ["/portfolio/demo2.css"]]
       (portfolio.actions/execute-action! app))

  (->> [:set-css-files ["/portfolio/demo.css"]]
       (portfolio.actions/execute-action! app))

)
