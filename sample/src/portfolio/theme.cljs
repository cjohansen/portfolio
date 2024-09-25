(ns portfolio.theme
  (:require ["react" :as react]
            [reagent.core :as r]))


(def ThemeContext (react/createContext :blue))
(def Provider (.-Provider ThemeContext))

(defn use-theme []
  (react/useContext ThemeContext))

(defn reagent-decorator [child]
  (react/createElement Provider #js{:value :red}
                       (r/as-element child)))

(defn react-18-decorator [props]
  (react/createElement Provider #js{:value :red}
                       (.-children props)))