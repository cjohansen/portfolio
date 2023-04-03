(ns portfolio.ui.screen)

(defn small-screen? [state]
  (< (-> state :win :w) 650))
