(ns portfolio.ui.css
  (:require [clojure.string :as str]))

(defn find-link-by-href [el path]
  (->> (.querySelectorAll el "link")
       array-seq
       (filter #(str/includes? (.-href %) path))
       first))

(defn create-css-link [path & [{:keys [media]}]]
  (let [link (js/document.createElement "link")]
    (set! (.-href link) path)
    (set! (.-rel link) "stylesheet")
    (set! (.-type link) "text/css")
    (set! (.-portfolio link) "portfolio")
    (when media
      (set! (.-media link) media))
    link))

(defn reload-css-file [file]
  (doseq [iframe (array-seq (.querySelectorAll js/document.body "iframe"))]
    (let [iframe-head (some-> iframe .-contentWindow .-document .-head)
          original (find-link-by-href iframe-head file)
          reloaded (create-css-link (str file "?" (.getTime (js/Date.))))]
      (.addEventListener
       reloaded
       "load"
       (fn done [_]
         (when-let [parent (some-> original .-parentNode)]
           (.removeChild parent original))
         (.removeEventListener reloaded "load" done)))
      (.appendChild iframe-head reloaded))))

(defn load-css-files [paths]
  ;; Figwheel and shadow-cljs only reload CSS files loaded in the document. To
  ;; avoid user CSS files skewing the Portfolio UI design, the files are loaded
  ;; with a bogus media attribute.
  (doseq [path paths]
    (when-not (find-link-by-href js/document.head path)
      (.appendChild js/document.head (create-css-link path {:media "portfolio"})))))

(defn on-head-mutation [mutations paths]
  (let [paths (set paths)]
    (doseq [path (->> mutations
                      (mapcat #(.-addedNodes %))
                      (filter (comp #{"LINK"} #(.-tagName %)))
                      (map #(.-href %))
                      (map #(second (re-find #"(?:https?://[^/]+)?([^\?]+)" %)))
                      (filter paths))]
      (reload-css-file path))))

(defn watch-css-reloads [paths]
  (let [observer (js/MutationObserver.
                  (fn [ms]
                    (on-head-mutation ms paths)))]
    (.observe
     observer
     js/document.head
     #js {:attributes true
          :subtree true
          :childList true})
    observer))

(defn replace-loaded-css-files [paths]
  (doseq [iframe (array-seq (.querySelectorAll js/document.body "iframe"))]
    (let [head (some-> iframe .-contentWindow .-document .-head)]
      (doseq [link (->> (.querySelectorAll head "link")
                        array-seq
                        (filter #(.-portfolio %)))]
        (-> link .-parentNode (.removeChild link)))
      (doseq [path paths]
        (.appendChild head (create-css-link path))))))
