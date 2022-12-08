(ns portfolio.components.button
  (:require [portfolio.dumdom :refer-macros [defscene]]
            [dumdom.core :as d]))

(d/defcomponent Bomb [_]
  (throw (ex-info "Oh no!" {:data 42})))

(defn shuffle-text [ref texts]
  (js/setTimeout
   (fn [_]
     (when-let [text (first texts)]
       (when (:mounted? @ref)
         (swap! ref assoc :text text)
         (shuffle-text ref (next texts)))))
   10000))

(defscene default
  :title "Button!"
  [:button.button "I am a button"])

;; (defscene deleted
;;   [:button.button "BOINK"])

(defscene aggressive
  :title "Aggressive button"
  [:button.button "I am a damn button!"])

(defscene parameterized
  :title "Parameterized button"
  :args {:text "Hello, clicky!"}
  [{:keys [text]}]
  [:button.button text])

(defscene stateful
  :title "Stateful button"
  :args (atom {:text "I'm stateful!"})
  :on-mount (fn [ref]
              (swap! ref assoc :mounted? true)
              (shuffle-text ref (cycle ["Tick ..." "... tock"])))
  :on-unmount (fn [ref]
                (swap! ref assoc :mounted? false))
  [ref]
  [:button.button (:text @ref)])

(defscene bomb
  (Bomb {}))
