(ns portfolio.icon)

(defmacro deficon [binding svg]
  `(let [sym# (symbol ~(str *ns* "/" binding))]
     (def ~binding
       (with-meta
         ~svg
         {:ns sym#
          `portfolio.code/format-code (constantly sym#)}))))
