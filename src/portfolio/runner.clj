(ns portfolio.runner
  (:require [cljs.env]))

(defmacro ^:export get-compiler-portfolio-config []
  (some-> cljs.env/*compiler* deref :options :portfolio.ui/config))
