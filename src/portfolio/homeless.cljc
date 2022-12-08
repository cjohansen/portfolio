(ns portfolio.homeless)

(defn debounce [f ms]
  #?(:cljs
     (let [timer (atom nil)]
       (fn [& args]
         (some-> @timer js/clearTimeout)
         (reset! timer (js/setTimeout #(apply f args) ms))))
     :clj (fn [& args] (apply f args))))
