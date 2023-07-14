(ns portfolio.ui.docs
  (:require [clojure.string :as str])
  (:require-macros [portfolio.ui.docs]))

(defn fix-links
  "Source markdown uses relative file paths for links to other documents, so
  documentation is also browsable on Github. This function changes those links
  to something that can be navigated by the Portfolio app."
  [md]
  (str/replace md #"\(\./(.+)\.md\)" (str "(" js/window.location.pathname "?doc=$1)")))

(defn ->markdown [lines]
  {:kind :markdown
   :markdown (->> (str/join "\n" lines)
                  fix-links
                  str/trim)})

(defn prepare-doc [doc]
  (let [lines (str/split-lines (str/trim doc))]
    (if-let [[_ title] (re-find #"^#[^#](.*)" (first lines))]
      {:title title
       :sections [(->markdown (next lines))]}
      {:sections [(->markdown lines)]})))
