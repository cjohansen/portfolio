(ns portfolio.code
  (:require #?(:cljs [cljs.pprint :as pprint]
               :clj [clojure.pprint :as pprint])
            [clojure.walk :as walk]))

(defprotocol ICodeString
  :extend-via-metadata true
  (format-code [x]))

#?(:cljs
   (extend-type default
     ICodeString
     (format-code [x]
       x)))

(defn blank? [x]
  (or (nil? x)
      (and (coll? x) (empty? x))
      (and (string? x) (empty? x))))

(defn code-str [data]
  (when (not (blank? data))
    (with-out-str
      (pprint/pprint
       (walk/postwalk format-code data)))))
