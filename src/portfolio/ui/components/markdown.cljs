(ns portfolio.ui.components.markdown
  (:require [clojure.string :as str]
            [dumdom.core :as d]
            [markdown.core :as md]))

(def langs
  {"clj" "clojure"
   "cljs" "clojure"})

(defn autolink [s]
  (->> (for [w (str/split s #" ")]
         (if (re-find #"^https?://[^\s]+$" w)
           (str "[" w "](" w ")")
           w))
       (str/join " ")))

(defn space-lists [s]
  (->> (str/split-lines s)
       (partition-by #(boolean (re-find #"^(-|\d+\.) " %)))
       (map #(str/join "\n" %))
       (str/join "\n\n")))

(defn render-markdown [s]
  (-> (space-lists s)
      autolink
      md/md->html))

(d/defcomponent Markdown
  :on-render (fn [el props]
               (doseq [pre (.querySelectorAll el "pre")]
                 (let [code (.-firstElementChild pre)]
                   (when (= "CODE" (.-tagName code))
                     (set! (.-className pre) (str "language-" (or (langs (.-className code)) (.-className code))))
                     (js/Prism.highlightElement pre)))))
  [{:keys [markdown]}]
  [:div.md {:dangerouslySetInnerHTML {:__html (render-markdown markdown)}}])
