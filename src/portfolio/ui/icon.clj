(ns portfolio.ui.icon)

(defmacro deficon [binding svg]
  `(let [sym# (symbol ~(str *ns* "/" binding))]
     (def ~binding
       (with-meta
         ~svg
         {:ns sym#
          `portfolio.ui.code/format-code (constantly sym#)}))))
