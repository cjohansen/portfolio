(ns portfolio.ui.canvas.taps
  (:require [clojure.string :as str]
            [portfolio.ui.canvas.protocols :as canvas]
            [portfolio.ui.components.taps-panel :refer [TapsPanel]]
            [portfolio.ui.scene :as scene]
            [portfolio.ui.view :as view]))

(def render-impl
  {`view/render-view #'TapsPanel})

(def visible-taps 15)

(defn abbreviate [n s]
  (if (< n (count s))
    (let [sep " ,,, "]
      (loop [xs (str/split s #"([ \[({})\]])")
             result ""
             closers '()]
        (if (nil? xs)
          result
          (let [x (first xs)]
            (if (< n (+ (count x) (count result)))
              (str/join (concat [result sep] closers))
              (recur
               (next xs)
               (str result x)
               (cond-> closers
                 (= "{" x) (conj "}")
                 (= "[" x) (conj "]")
                 (= "(" x) (conj ")"))))))))
    s))

(defn prepare-taps [scene overrides taps]
  (if-let [items (seq (take (* 2 visible-taps) taps))]
    (let [items (vec items)]
      {:current-item (when-let [current (->> items (filter #{overrides}) first)]
                       (with-out-str
                         (cljs.pprint/pprint current)))
       :items
       (->> items
            set
            (take visible-taps)
            (sort-by #(.indexOf items %))
            (map (fn [v]
                   (let [selected? (= v overrides)]
                     {:text (abbreviate 80 (pr-str v))
                      :selected? selected?
                      :actions [(if selected?
                                  [:remove-scene-argument (:id scene)]
                                  [:set-scene-argument (:id scene) v])]}))))})
    {:title "Your taps go here"
     :text [:span
            "Data you " [:code "tap>"]
            " will show up here, available for use as arguments in components. "
            "After you've tapped some data, render the current component with it "
            "by clicking it in this panel."]}))

(defn prepare-panel-content [panel state scene]
  (when (:param scene)
    (with-meta
      (let [overrides (scene/get-param-overrides state scene)]
        (prepare-taps scene overrides (:taps state)))
      render-impl)))

(def data-impl
  {`canvas/prepare-panel-content #'prepare-panel-content})

(defn create-taps-panel [config]
  (with-meta
    {:id :canvas/taps-panel
     :title "Taps"}
    data-impl))
