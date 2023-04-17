(ns portfolio.ui.components.markdown
  (:require [dumdom.core :as d]))

(def langs
  {"clj" "clojure"
   "cljs" "clojure"})

(d/defcomponent Markdown
  :on-render (fn [el props]
               (doseq [pre (.querySelectorAll el "pre")]
                 (let [code (.-firstElementChild pre)]
                   (when (= "CODE" (.-tagName code))
                     (set! (.-className pre) (str "language-" (or (langs (.-className code)) (.-className code))))
                     (js/Prism.highlightElement pre)))))
  [{:keys [markdown]}]
  [:div.md {:dangerouslySetInnerHTML {:__html markdown}}])
