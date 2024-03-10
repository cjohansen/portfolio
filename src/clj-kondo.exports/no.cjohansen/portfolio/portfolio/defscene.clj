(ns portfolio.defscene
  (:require [clj-kondo.hooks-api :as api]))

(def valid-opts #{:title
                  :params
                  :on-mount
                  :on-unmount
                  :collection
                  :icon
                  :selected-icon
                  :icon-color
                  :selected-icon-color})

(defn- extract-docstr
  [[docstr? & forms :as remaining-forms]]
  (if (api/string-node? docstr?)
    [docstr? forms]
    [(api/string-node "no docs") remaining-forms]))

(defn- extract-opts
  ([forms]
   (extract-opts forms []))
  ([[k v & forms :as remaining-forms] opts]
   (if (api/keyword-node? k)
     (do
       (when-not (valid-opts (api/sexpr k))
         (api/reg-finding! (assoc (meta k)
                                  :message (str "Invalid option: `" k "`")
                                  :type :portfolio/component-options)))
       (extract-opts forms (into opts [k v])))
     [(api/map-node opts) remaining-forms])))

(defn- extract-arg-list [forms]
  (if (and (api/vector-node? (first forms)) (next forms))
    [(first forms) (next forms)]
    [nil forms]))

(defn ^:export defscene [{:keys [node]}]
  (let [[name & forms] (rest (:children node))
        [docstr forms] (extract-docstr forms)
        [opts forms] (extract-opts forms)
        [arg-list forms] (extract-arg-list forms)]
    {:node
     (if arg-list
       (api/list-node
        (list*
         (api/token-node 'defn)
         name
         docstr
         arg-list
         opts
         forms))
       (api/list-node
        (list*
         (api/token-node 'do)
         opts
         forms)))}))

(comment
  (require '[clj-kondo.core :as clj-kondo])

  (defn get-findings [code]
    (:findings
     (with-in-str
       (str
        '(require '[portfolio.dumdom :refer [defscene]])

        code)
       (clj-kondo.core/run! {:lint ["-"]}))))

  (def code
    '(defscene heading
       :on-mount (fn [param1 param2]
                   (prn param1))
       :invalid (fn [])
       [data]
       [:h2 {:style {:background :black
                     :color :white}}
        (pr-str (:text data))]))

  (defscene {:node (api/parse-string (str code))})
  (get-findings code)

  (def less-code
    '(defscene heading [data]
       (pr-str (:text data))))

  (defscene {:node (api/parse-string (str less-code))})
  (get-findings less-code)

  )
