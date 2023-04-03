(ns portfolio.screen)

(defn small-screen? [state]
  (< (-> state :win :w) 650))
