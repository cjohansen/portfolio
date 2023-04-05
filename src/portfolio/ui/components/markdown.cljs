(ns portfolio.ui.components.markdown
  (:require [dumdom.core :as d]))

(d/defcomponent Markdown
  :on-render (fn [el props]
               (doseq [pre (.querySelectorAll el "pre")]
                 (let [code (.-firstElementChild pre)]
                   (when (= "CODE" (.-tagName code))
                     (set! (.-className pre) (str "language-" (.-className code)))
                     (js/Prism.highlightElement pre)))))
  [{:keys [markdown]}]
  [:div.md {:dangerouslySetInnerHTML {:__html markdown}}])
