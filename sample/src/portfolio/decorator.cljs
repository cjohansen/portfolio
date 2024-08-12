(ns portfolio.decorator
  (:require ["react" :refer [createElement createContext useContext]]
            [reagent.core :as r]))


(def ThemeContext (createContext nil))
(def Provider (.-Provider ThemeContext))

(defn use-theme []
  (useContext ThemeContext))

(defn reagent-decorator [child]
  (createElement Provider #js{:value :red}
                 (r/as-element child)))

(defn react-18-decorator [props]
  (createElement Provider #js{:value :red}
                 (.-children props)))