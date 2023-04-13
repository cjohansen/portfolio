(ns portfolio.ui.color)

(defn hex->rgba [hex]
  (let [[r g b a]
        (take 4
              (concat
               (map #(js/parseInt % 16)
                    (if (#{4 5} (count hex))
                      (map #(str % %) (re-seq #"[^#]" hex))
                      (re-seq #"[^#]{2}" hex)))
               [255]))]
    [r g b (/ a 255)]))

(def rgb-re #"rgb\((\d+), (\d+), (\d+)\)")
(def rgba-re #"rgba\((\d+), (\d+), (\d+), (.*)\)")

(defn perceived-rgb [bg rgb-or-rgba]
  (if (= 3 (count rgb-or-rgba))
    rgb-or-rgba
    (let [[bg-r bg-g bg-b] bg
          [fg-r fg-g fg-b a] rgb-or-rgba
          bg-a (- 1 a)]
      [(int (+ (* bg-r bg-a) (* fg-r a)))
       (int (+ (* bg-g bg-a) (* fg-g a)))
       (int (+ (* bg-b bg-a) (* fg-b a)))])))

(defn rgba-str->rgb [color]
  (if (re-find rgba-re color)
    (->> (re-find rgba-re color)
         (drop 1)
         (map #(js/parseFloat %))
         (perceived-rgb [255 255 255]))
    (->> (re-find rgb-re color)
         (drop 1)
         (take 3)
         (map #(js/parseInt % 10)))))

(defn ->rgb [color]
  (if (re-find #"#.+" color)
    (perceived-rgb [255 255 255] (hex->rgba color))
    (rgba-str->rgb color)))

(defn- pct [decimal]
  (js/Math.floor (* decimal 100)))

(defn- round [n]
  (/ (pct n) 100))

(defn rgb->hsl [rgb]
  (let [[r g b] (map #(round (/ % 255.0)) rgb)
        minv (min r g b)
        maxv (max r g b)
        diff (- maxv minv)
        luminace (js/Math.ceil (* 100 (/ (+ maxv minv) 2)))
        saturation (pct
                    (cond
                      (= minv maxv) 0
                      (< luminace 50) (/ diff (+ maxv minv))
                      :else (/ diff (- 2 maxv minv))))
        hue (if (= saturation 0)
              0
              (js/Math.ceil
               (* 60 (cond
                       (= maxv r) (/ (- g b) diff)
                       (= maxv g) (+ 2 (/ (- b r) diff))
                       (= maxv b) (+ 4 (/ (- r g) diff))))))]
    {:h hue
     :s saturation
     :l luminace}))

(comment
  (rgb->hsl (->rgb "#212227"))
  (rgb->hsl (->rgb "#555555"))
  (rgb->hsl (->rgb "#777777"))
  (rgb->hsl (->rgb "#fff"))
  )
