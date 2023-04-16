(ns portfolio.ui.intro-screen
  (:require [portfolio.ui.view :as view]
            [portfolio.ui.components.document :refer [Document]]))

(defn prepare-view [state location]
  (with-meta
    {:title "Welcome to Portfolio!"
     :sections [{:kind :markdown
                 :markdown "Everything is set up right, all you need to do now is to define some scenes with `defscene`, and require it from the namespace where you called `portfolio.ui/start!`."}]}
    {`view/render-view #'Document}))
