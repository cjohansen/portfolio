(ns portfolio.components.button
  (:require [portfolio.dumdom :as portfolio :refer-macros [defscene]]
            [dumdom.core :as d]))

(d/defcomponent Bomb [{:keys [text data]}]
  (throw (ex-info text data)))

(defn shuffle-text [ref texts]
  (js/setTimeout
   (fn [_]
     (when-let [text (first texts)]
       (when (:mounted? @ref)
         (swap! ref assoc :text text)
         (shuffle-text ref (next texts)))))
   3000))

(portfolio/configure-scenes
 "Some collection docs"
 {:title "Button"})

(defscene default
  "This is the button, it is **really** quite nice.
   You can invoke it with `(Button {:text \"Lul\"})`, e.g.:

```clojure
(Button {:text \"Hello\"})
```"
  :title "Button!"
  [:button.button "I am a button"])

;; (defscene deleted
;;   [:button.button "BOINK"])

(defscene parameterized
  :title "Parameterized button"
  :param {:text {:en "Hello, clicky!" :nb "Heisann!"}}
  [{:keys [text]} opt]
  [:button.button (get text (:i18n/locale opt))])

(defn render-button [data]
  [:button.button (:text data)])

(defscene reusable-render-function
  :param {:text "I am a damn button!"}
  render-button)

(defscene stateful
  :title "Stateful button"
  :param (atom {:text "I'm stateful!"})
  :on-mount (fn [ref]
              (swap! ref assoc :mounted? true)
              (shuffle-text ref (cycle ["Tick ..." "... tock"])))
  :on-unmount (fn [ref]
                (swap! ref assoc :mounted? false))
  [ref]
  (prn "My implementation consists of two s-exps")
  [:button.button (:text @ref)])

(defscene multi-param-scene
  :title "Multiple params"
  :params [(atom {:text "I'm stateful!"}) {:text "And I'm static"}]
  :on-mount (fn [[ref _static]]
              (swap! ref assoc :mounted? true)
              (shuffle-text ref (cycle ["Tick ..." "... tock"])))
  :on-unmount (fn [[ref _static]]
                (swap! ref assoc :mounted? false))
  [[ref static]]
  [:button.button (:text @ref) " " (:text static)])

(defscene bomb
  (prn "Look ma, stray s-exps!")
  (Bomb {:text "Oh no!", :data {:data 42}}))

(defscene parameterized-bomb
  :params {:text "Boom!" :data {:secret "Ssh!"}}
  [params]
  (Bomb params))

(defscene multi-param-bomb
  :params [{:text "Multi-paramed explosion"} {:data {:id :multi-params}}]
  [params]
  (Bomb (first params)))

(defscene atom-param-bomb
  :params (atom {:text "Atom paramed overflow"})
  [params]
  (Bomb @params))

(defscene on-mount-error
  :params {:text "Mounting issues"}
  :on-mount (fn [params]
              (throw (ex-info "on-mount says boom!" {:number 42})))
  [params]
  [:button.button (:text params)])
