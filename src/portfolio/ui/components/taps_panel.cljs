(ns portfolio.ui.components.taps-panel
  (:require [dumdom.core :as d]
            [cljsjs.codemirror]
            [cljsjs.codemirror.mode.clojure]
            [cljsjs.codemirror.keymap.emacs]))

(d/defcomponent Code
  :keyfn :code
  :on-render (fn [el]
               (js/Prism.highlightElement el))
  [{:keys [code]}]
  [:pre.language-clojure {:style {:font-family "monospace"}}
   code])

(d/defcomponent CodeMirror
  :keyfn :code
  :on-mount (fn [el]
              (js/CodeMirror.fromTextArea
               (.-firstChild el)
               #js {:lineNumbers true
                    :mode "clojure"}))
  [{:keys [code]}]
  [:form [:textarea code]])

(d/defcomponent TapsPanel [{:keys [title text current-item items]}]
  [:div {:style {:padding 20
                 :display "flex"
                 :max-height "100%"}}
   [:div {:style {:flex-grow 1}}
    (when title
      [:h2.h3 {:style {:margin-bottom 10}} title])
    (when text
      [:p text])
    (when (seq items)
      [:ul
       (for [{:keys [actions selected? text]} items]
         [:li.hoverable
          {:on-click actions
           :style {:background (when selected? "#f0f0f0")
                   :padding "5px"}}
          (Code {:code text})])])]
   (when current-item
     [:div {:style {:flex-grow 1
                    :flex-shrink 0
                    :flex-basis "60%"
                    :max-height "100%"}}
      [:div {:style {:margin "0 20px"
                     :max-height "100%"}}
       (CodeMirror {:code current-item})]])])
