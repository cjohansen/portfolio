(ns portfolio.components.reagent
  ;; If you are using an older version of react use the following:
  ;; (:require [portfolio.reagent :refer-macros [defscene]])
  ;; For react versions 18+ use the following:
  ;; This is due to the new API https://www.metosin.fi/blog/reagent-towards-react-18/
  (:require [portfolio.components.reagent.component :as rc]
            [portfolio.theme :as theme]
            [portfolio.reagent :refer-macros [defscene]]))

(defn button [text]
  [:button.button text])

(defscene standard-button
  (button "I am a Reagent button"))

(defn form-2-button [_]
  (let [add-num #(str % (rand-int 100))]
    (fn [{:keys [text]}]
      [:button.button (add-num text)])))

(defscene form2-component
  :params {:text "I am a Reagent \"form 2\" button component"}
  [params]
  [form-2-button params])

(defn my-form2-component [num]
  ;; This is a silly, non-real-world example.
  ;; Normally, you'd let a r/atom to have local state,
  ;; or maybe trigger side effects that should only
  ;; happen on-mount.
  ;; But nothing more than this is needed
  ;; To demonstrate the problem.
  (let [result (+ 5 num)]
    (fn [_]
      [:div result])))

(defn function-component []
  (let [theme (theme/use-theme)]
    [:button {:style {:background (name theme)}}
     "current theme is " (name theme)]))

(defn decorator-consumer-component []
  [:f> function-component])

(defscene first-demo
  :params 1
  [params]
  [my-form2-component params])

(defscene second-demo
  :params 2
  [params]
  [my-form2-component params])

(defscene component-in-other-file
  :params {:text "Hello world!!"
           :border-width 1}
  [params]
  [rc/component params])

(defscene decorator-consumer-demo
  [decorator-consumer-component])