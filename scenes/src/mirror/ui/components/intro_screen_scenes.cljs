(ns mirror.ui.components.intro-screen-scenes
  (:require [portfolio.dumdom :as portfolio :refer-macros [defscene]]
            [portfolio.ui.components.document :refer [Document]]
            [portfolio.ui.intro-screen :as intro]))

(portfolio/configure-scenes
 {:title "Intro screen"})

(defscene setup-good
  (Document (intro/prepare-view {} {})))
