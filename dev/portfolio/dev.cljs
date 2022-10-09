(ns ^:figwheel-hooks portfolio.dev
  (:require [dumdom.component]
            [gadget.inspector :as inspector]
            [portfolio.actions :as actions]
            [portfolio.adapters.dumdom :as pd]
            [portfolio.kitchen-sink :as portfolio]))

(set! dumdom.component/*render-eagerly?* true)

(def button-atom (atom {:text "I'm stateful!"}))

(defn shuffle-text [ref texts]
  (js/setTimeout
   (fn [_]
     (when-let [text (first texts)]
       (when (:mounted? @ref)
         (swap! ref assoc :text text)
         (shuffle-text ref (next texts)))))
   2000))

(def config
  {:canvas/layout [[{}]]

   :scenes
   [(pd/create-scene
     {:id :portfolio.components.button/default
      :title "Button!"
      :component [:button.button "I am a button"]})

    (pd/create-scene
     {:id :portfolio.components.button/aggressive
      :title "Aggressive button"
      :component [:button.button "I am a damn button!"]})

    (pd/create-scene
     {:id :portfolio.components.button/parameterized
      :title "Parameterized button"
      :component-fn (fn [{:keys [text]}]
                      [:button.button text])
      :args {:text "Hello, clicky!"}})

    (pd/create-scene
     {:id :portfolio.components.button/stateful
      :title "Stateful button"
      :component-fn (fn [ref]
                      [:button.button (:text @ref)])
      :args button-atom
      :on-mount (fn [ref]
                  (swap! ref assoc :mounted? true)
                  (shuffle-text ref (cycle ["Tick ..." "... tock"])))
      :on-unmount (fn [ref]
                    (swap! ref assoc :mounted? false))})

    (pd/create-scene
     {:id :portfolio.components.heading/default
      :title "Heading"
      :component [:h1 "I am a heading"]
      :canvas/layout [[{} {:background/background-color "#000"
                           :background/body-class "dark-mode"}]]})

    (pd/create-scene
     {:id :portfolio.components.link/default
      :title "Link"
      :component [:a {:href "#"} "I am a link"]})

    (pd/create-scene
     {:id :portfolio.layouts.home-page/default
      :title "Default homepage"
      :component [:div
                  [:h1 "Heading"]
                  [:p [:a {:href "#"} "I am a link"]]
                  [:button.button "Click it"]]})]

   :namespaces
   [{:namespace "portfolio.components.button"
     :collection :elements
     :title "Button"}

    {:namespace "portfolio.components.heading"
     :collection :elements
     :title "Heading"}

    {:namespace "portfolio.components.link"
     :collection :elements
     :title "Link"}

    {:namespace "portfolio.layouts.home-page"
     :collection :layouts
     :title "Home page"}]

   :collections
   [{:id :elements
     :canvas/layout [[{:grid/size 22
                       :grid/offset -2}]
                     [{:viewport/width 306
                       :viewport/height :auto
                       :grid/size 22
                       :grid/offset -2}
                      {:viewport/width 350
                       :viewport/height 300}]]
     :title "Elements"}

    {:id :layouts
     :title "Layouts"}]

   })

(defonce app
  (let [app (portfolio/start! config {:on-render #(inspector/inspect "Page data" %)})]
    (inspector/inspect "Application data" app)
    app))

(defn ^:after-load render []
  (actions/execute-action! app [:go-to-location {}])
  (swap! app merge config)
  (actions/execute-action! app [:go-to-current-location]))
