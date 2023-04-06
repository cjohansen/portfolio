(ns portfolio.ui.components.elastic-container)

(defn clean-up-after-transition [node callback ms]
  (js/setTimeout (fn [_]
                   (set! (.. node -style -overflow) "auto")
                   (set! (.. node -style -height) "auto")
                   (.setAttribute node "data-ec-anim" "done")
                   (callback))
                 ms))

(defn enter [& [opt]]
  (fn [node callback val]
    (if (= "animating" (.getAttribute node "data-ec-anim"))
      (do (.setAttribute node "data-ec-anim" "cancelled")
          (callback))
      (do
        (.setAttribute node "data-ec-anim" "animating")
        (set! (.. node -style -overflow) "hidden")
        (let [target (.-height (.getBoundingClientRect node))]
          (clean-up-after-transition node callback 250)
          (set! (.. node -style -height) 0)
          (set! (.. node -style -transition) (or (:transition opt) "height 0.25s ease-in"))
          (js/window.requestAnimationFrame
           #(set! (.. node -style -height) (str target "px"))))))))

(defn leave [& [opt]]
  (fn [node callback]
    (if (= "animating" (.getAttribute node "data-ec-anim"))
      (do (.setAttribute node "data-ec-anim" "cancelled")
          (callback))
      (do
        ;; Start by ensuring that the CSS height property is explicitly set,
        ;; otherwise there will be no transition, which means that the callback will
        ;; never trigger. No good.
        (set! (.. node -style -transition) "none")
        (set! (.. node -style -height) (str (.-height (.getBoundingClientRect node)) "px"))

        (set! (.. node -style -overflow) "hidden")
        (clean-up-after-transition node callback 150)
        (set! (.. node -style -transition) (or (:transition opt) "height 0.15s ease-out"))
        (js/window.requestAnimationFrame
         #(set! (.. node -style -height) 0))))))
