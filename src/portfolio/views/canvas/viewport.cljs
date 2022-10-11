(ns portfolio.views.canvas.viewport
  (:require [portfolio.components.canvas :as canvas]
            [portfolio.views.canvas.addons :as addons]))

(defn get-width [frame frame-body width]
  (cond
    (= :auto width)
    (let [style (js/window.getComputedStyle frame)]
      (str (+ (js/parseInt (.getPropertyValue style "padding-left") 10)
              (js/parseInt (.getPropertyValue style "padding-right") 10)
              (.-scrollWidth frame-body)) "px"))

    (number? width) (str width "px")

    (nil? width) "100%"

    :else width))

(defn get-style-px [style prop]
  (if-let [v (some-> style (.getPropertyValue prop) not-empty)]
    (js/parseInt v 10)
    0))

(defn get-height [frame frame-body height]
  (cond
    (= :auto height)
    (let [style (js/window.getComputedStyle frame)
          root-el-style (some-> frame-body
                                .-firstElementChild
                                .-firstElementChild
                                js/window.getComputedStyle)]
      (str (+ (get-style-px style "padding-top")
              (get-style-px style "padding-bottom")
              (get-style-px root-el-style "margin-top")
              (get-style-px root-el-style "margin-bottom")
              (.-scrollHeight frame-body)) "px"))

    (number? height) (str height "px")

    (nil? height) "100%"

    :else height))

(defn prepare-canvas [data el {:viewport/keys [width height]}]
  (let [frame (canvas/get-iframe el)
        frame-body (canvas/get-iframe-body el)
        w (get-width frame frame-body width)]
    (set! (.. el -style -width)
          (if (and (= "100%" w) (not= "100%" (or height "100%")))
            (str "calc(100% - 40px)")
            w))
    (js/setTimeout
     (fn []
       (let [h (get-height frame frame-body height)
             [margin shadow] (if (or (not= "100%" w) (not= "100%" h))
                               ["20px" "rgba(0, 0, 0, 0.1) 0px 1px 5px 0px"]
                               ["0" "none"])]
         (set! (.. el -style -height) h)
         (set! (.. el -style -margin) margin)
         (set! (.. el -style -boxShadow) shadow)))
     100)))

(defn create-viewport-tool [config]
  (addons/create-toolbar-menu-button
   {:id :canvas/viewport
    :title "Viewport"
    :options [{:title "Auto"
               :value {:viewport/width "100%"
                       :viewport/height "100%"}
               :type :desktop}
              {:title "iPhone 12 / 13 Prop"
               :value {:viewport/width 390
                       :viewport/height 844}
               :type :mobile}]
    :prepare-canvas #'prepare-canvas}))
