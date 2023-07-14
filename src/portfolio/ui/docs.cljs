(ns portfolio.ui.docs
  (:require [clojure.string :as str])
  (:require-macros [portfolio.ui.docs]))

(defn fix-links
  "Source markdown uses relative file paths for links to other documents, so
  documentation is also browsable on Github. This function changes those links
  to something that can be navigated by the Portfolio app."
  [md]
  (str/replace md #"\(\./(.+)\.md(#[^\)]+)?\)" (str "(" js/window.location.pathname "?doc=$1$2)")))

(defn fix-images
  "Source markdown uses relative file paths for images so they render on Github.
  It's hard for Portfolio to ensure that these images are available as resources
  over HTTP in the local setup, so it instead serves them from Github in the
  Portfolio UI."
  [md]
  (str/replace md #"\./([^\)\"]+\.png)" (str "https://github.com/cjohansen/portfolio/blob/main/docs/portfolio/$1?raw=true")))

(defn fix-source-links
  "Direct source file links to Github"
  [md]
  (str/replace md #"\(../../([^\)]+\.cljs)\)" (str "(https://github.com/cjohansen/portfolio/blob/main/$1)")))

(defn ->markdown [lines]
  {:kind :markdown
   :markdown (->> (str/join "\n" lines)
                  fix-links
                  fix-images
                  fix-source-links
                  str/trim)})

(defn prepare-doc [doc]
  (let [lines (str/split-lines (str/trim doc))]
    (if-let [[_ title] (re-find #"^#[^#](.*)" (first lines))]
      {:title title
       :sections [(->markdown (next lines))]}
      {:sections [(->markdown lines)]})))
