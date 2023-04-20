(ns portfolio.ui.icons
  "https://phosphoricons.com/"
  (:require [portfolio.ui.code :as code]))

(def icons
  (->> {::arrow-counter-clockwise
        [:svg {:fill "none"
               :viewBox "0 0 256 256"}
         [:polyline
          {:points "79.8 99.7 31.8 99.7 31.8 51.7"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"}]
         [:path
          {:d "M65.8,190.2a88,88,0,1,0,0-124.4l-34,33.9"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"}]]

        ::bookmark
        [:svg
         {:fill "currentColor"
          :viewBox "0 0 256 256"}
         [:path {:d "M184,32H72A16,16,0,0,0,56,48V224a8,8,0,0,0,12.24,6.78L128,193.43l59.77,37.35A8,8,0,0,0,200,224V48A16,16,0,0,0,184,32Zm0,177.57-51.77-32.35a8,8,0,0,0-8.48,0L72,209.57V48H184Z"}]]

        ::brackets-square
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M48,48V208H80a8,8,0,0,1,0,16H40a8,8,0,0,1-8-8V40a8,8,0,0,1,8-8H80a8,8,0,0,1,0,16ZM216,32H176a8,8,0,0,0,0,16h32V208H176a8,8,0,0,0,0,16h40a8,8,0,0,0,8-8V40A8,8,0,0,0,216,32Z"}]]

        ::browsers
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M216,40H72A16,16,0,0,0,56,56V72H40A16,16,0,0,0,24,88V200a16,16,0,0,0,16,16H184a16,16,0,0,0,16-16V184h16a16,16,0,0,0,16-16V56A16,16,0,0,0,216,40ZM184,88v16H40V88Zm0,112H40V120H184v80Zm32-32H200V88a16,16,0,0,0-16-16H72V56H216Z"}]]

        ::caret-double-left
        [:svg {:fill "none"
               :viewBox "0 0 256 256"}
         [:rect {:height "256"
                 :width "256"}]
         [:polyline {:points "200 208 120 128 200 48"
                     :stroke "currentColor"
                     :stroke-linecap "round"
                     :stroke-linejoin "round"
                     :stroke-width "16"}]
         [:polyline {:points "120 208 40 128 120 48"
                     :stroke "currentColor"
                     :stroke-linecap "round"
                     :stroke-linejoin "round"
                     :stroke-width "16"}]]

        ::caret-double-right
        [:svg {:fill "none"
               :viewBox "0 0 256 256"}
         [:path {:d "M141.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L124.69,128,50.34,53.66A8,8,0,0,1,61.66,42.34l80,80A8,8,0,0,1,141.66,133.66Zm80-11.32-80-80a8,8,0,0,0-11.32,11.32L204.69,128l-74.35,74.34a8,8,0,0,0,11.32,11.32l80-80A8,8,0,0,0,221.66,122.34Z"
                 :fill "currentColor"}]]

        ::caret-down
        [:svg
         {:fill "currentColor"
          :viewBox "0 0 256 256"}
         [:path {:d "M213.66,101.66l-80,80a8,8,0,0,1-11.32,0l-80-80A8,8,0,0,1,53.66,90.34L128,164.69l74.34-74.35a8,8,0,0,1,11.32,11.32Z"}]]

        ::caret-right
        [:svg
         {:fill "currentColor"
          :viewBox "0 0 256 256"}
         [:path {:d "M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z"}]]

        ::caret-up
        [:svg
         {:fill "currentColor"
          :viewBox "0 0 256 256"}
         [:path {:d "M213.66,165.66a8,8,0,0,1-11.32,0L128,91.31,53.66,165.66a8,8,0,0,1-11.32-11.32l80-80a8,8,0,0,1,11.32,0l80,80A8,8,0,0,1,213.66,165.66Z"}]]

        ::check
        [:svg {:viewBox "0 0 256 256"}
         [:polyline
          {:points "216 72 104 184 48 128"
           :fill "none"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"}]]

        ::columns
        [:svg {:fill "none"
               :viewBox "0 0 256 256"}
         [:rect {:x "-4" :y "100" :width "176" :height "56" :rx "8" :transform "translate(212 44) rotate(90)" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]
         [:rect {:x "84" :y "100" :width "176" :height "56" :rx "8" :transform "translate(300 -44) rotate(90)" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]]

        ::cube
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M223.68,66.15,135.68,18h0a15.88,15.88,0,0,0-15.36,0l-88,48.17a16,16,0,0,0-8.32,14v95.64a16,16,0,0,0,8.32,14l88,48.17a15.88,15.88,0,0,0,15.36,0l88-48.17a16,16,0,0,0,8.32-14V80.18A16,16,0,0,0,223.68,66.15ZM128,32h0l80.34,44L128,120,47.66,76ZM40,90l80,43.78v85.79L40,175.82Zm96,129.57V133.82L216,90v85.78Z"}]]

        ::device-mobile
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M176,16H80A24,24,0,0,0,56,40V216a24,24,0,0,0,24,24h96a24,24,0,0,0,24-24V40A24,24,0,0,0,176,16ZM72,64H184V192H72Zm8-32h96a8,8,0,0,1,8,8v8H72V40A8,8,0,0,1,80,32Zm96,192H80a8,8,0,0,1-8-8v-8H184v8A8,8,0,0,1,176,224Z"}]]

        ::devices
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M224,72H208V64a24,24,0,0,0-24-24H40A24,24,0,0,0,16,64v96a24,24,0,0,0,24,24H152v8a24,24,0,0,0,24,24h48a24,24,0,0,0,24-24V96A24,24,0,0,0,224,72ZM40,168a8,8,0,0,1-8-8V64a8,8,0,0,1,8-8H184a8,8,0,0,1,8,8v8H176a24,24,0,0,0-24,24v72Zm192,24a8,8,0,0,1-8,8H176a8,8,0,0,1-8-8V96a8,8,0,0,1,8-8h48a8,8,0,0,1,8,8Zm-96,16a8,8,0,0,1-8,8H88a8,8,0,0,1,0-16h40A8,8,0,0,1,136,208Zm80-96a8,8,0,0,1-8,8H192a8,8,0,0,1,0-16h16A8,8,0,0,1,216,112Z"}]]

        ::file-doc
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M52,144H36a8,8,0,0,0-8,8v56a8,8,0,0,0,8,8H52a36,36,0,0,0,0-72Zm0,56H44V160h8a20,20,0,0,1,0,40Zm169.53-4.91a8,8,0,0,1,.25,11.31A30.06,30.06,0,0,1,200,216c-17.65,0-32-16.15-32-36s14.35-36,32-36a30.06,30.06,0,0,1,21.78,9.6,8,8,0,0,1-11.56,11.06A14.24,14.24,0,0,0,200,160c-8.82,0-16,9-16,20s7.18,20,16,20a14.24,14.24,0,0,0,10.22-4.66A8,8,0,0,1,221.53,195.09ZM128,144c-17.65,0-32,16.15-32,36s14.35,36,32,36,32-16.15,32-36S145.65,144,128,144Zm0,56c-8.82,0-16-9-16-20s7.18-20,16-20,16,9,16,20S136.82,200,128,200ZM48,120a8,8,0,0,0,8-8V40h88V88a8,8,0,0,0,8,8h48v16a8,8,0,0,0,16,0V88a8,8,0,0,0-2.34-5.66l-56-56A8,8,0,0,0,152,24H56A16,16,0,0,0,40,40v72A8,8,0,0,0,48,120ZM160,51.31,188.69,80H160Z"}]]

        ::folder
        [:svg
         {:fill "currentColor"
          :viewBox "0 0 256 256"}
         [:path {:d "M216,72H131.31L104,44.69A15.86,15.86,0,0,0,92.69,40H40A16,16,0,0,0,24,56V200.62A15.4,15.4,0,0,0,39.38,216H216.89A15.13,15.13,0,0,0,232,200.89V88A16,16,0,0,0,216,72ZM40,56H92.69l16,16H40ZM216,200H40V88H216Z"}]]

        ::folder-open
        [:svg
         {:fill "currentColor"
          :viewBox "0 0 256 256"}
         [:path {:d "M245,110.64A16,16,0,0,0,232,104H216V88a16,16,0,0,0-16-16H130.67L102.94,51.2a16.14,16.14,0,0,0-9.6-3.2H40A16,16,0,0,0,24,64V208h0a8,8,0,0,0,8,8H211.1a8,8,0,0,0,7.59-5.47l28.49-85.47A16.05,16.05,0,0,0,245,110.64ZM93.34,64l27.73,20.8a16.12,16.12,0,0,0,9.6,3.2H200v16H69.77a16,16,0,0,0-15.18,10.94L40,158.7V64Zm112,136H43.1l26.67-80H232Z"}]]

        ::git-diff
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M112,152a8,8,0,0,0-8,8v28.69L75.72,160.4A39.71,39.71,0,0,1,64,132.12V95a32,32,0,1,0-16,0v37.13a55.67,55.67,0,0,0,16.4,39.6L92.69,200H64a8,8,0,0,0,0,16h48a8,8,0,0,0,8-8V160A8,8,0,0,0,112,152ZM40,64A16,16,0,1,1,56,80,16,16,0,0,1,40,64Zm168,97V123.88a55.67,55.67,0,0,0-16.4-39.6L163.31,56H192a8,8,0,0,0,0-16H144a8,8,0,0,0-8,8V96a8,8,0,0,0,16,0V67.31L180.28,95.6A39.71,39.71,0,0,1,192,123.88V161a32,32,0,1,0,16,0Zm-8,47a16,16,0,1,1,16-16A16,16,0,0,1,200,208Z"}]]

        ::grid-four
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M200,40H56A16,16,0,0,0,40,56V200a16,16,0,0,0,16,16H200a16,16,0,0,0,16-16V56A16,16,0,0,0,200,40Zm0,80H136V56h64ZM120,56v64H56V56ZM56,136h64v64H56Zm144,64H136V136h64v64Z"}]]

        ::hamburger
        [:svg {:viewBox "0 0 256 256"}
         [:path
          {:d "M48.8,96A8,8,0,0,1,41,86.3C47.4,55.5,83.9,32,128,32s80.6,23.5,87,54.3a8,8,0,0,1-7.8,9.7Z"
           :fill "none"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"}]
         [:path
          {:d "M208,168v16a32,32,0,0,1-32,32H80a32,32,0,0,1-32-32V168"
           :fill "none"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"}]
         [:polyline
          {:fill "none"
           :points "28 176 68 160 108 176 148 160 188 176 228 160"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"}]
         [:line
          {:fill "none"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"
           :x1 "24"
           :x2 "232"
           :y1 "128"
           :y2 "128"}]]

        ::hourglass-high
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M184,24H72A16,16,0,0,0,56,40V76a16.07,16.07,0,0,0,6.4,12.8L114.67,128,62.4,167.2A16.07,16.07,0,0,0,56,180v36a16,16,0,0,0,16,16H184a16,16,0,0,0,16-16V180.36a16.09,16.09,0,0,0-6.35-12.77L141.27,128l52.38-39.6A16.05,16.05,0,0,0,200,75.64V40A16,16,0,0,0,184,24Zm0,16V56H72V40Zm0,176H72V180l56-42,56,42.35Zm-56-98L72,76V72H184v3.64Z"}]]

        ::list-plus
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M32,64a8,8,0,0,1,8-8H216a8,8,0,0,1,0,16H40A8,8,0,0,1,32,64Zm8,72H216a8,8,0,0,0,0-16H40a8,8,0,0,0,0,16Zm104,48H40a8,8,0,0,0,0,16H144a8,8,0,0,0,0-16Zm88,0H216V168a8,8,0,0,0-16,0v16H184a8,8,0,0,0,0,16h16v16a8,8,0,0,0,16,0V200h16a8,8,0,0,0,0-16Z"}]]

        ::magnifying-glass
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M229.66,218.34l-50.07-50.06a88.11,88.11,0,1,0-11.31,11.31l50.06,50.07a8,8,0,0,0,11.32-11.32ZM40,112a72,72,0,1,1,72,72A72.08,72.08,0,0,1,40,112Z"}]]

        ::magnifying-glass-minus
        [:svg {:fill "none"
               :viewBox "0 0 256 256"}
         [:line
          {:stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"
           :x1 "84"
           :x2 "148"
           :y1 "116"
           :y2 "116"}]
         [:circle
          {:cx "116"
           :cy "116"
           :r "84"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"}]
         [:line
          {:stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"
           :x1 "175.4"
           :x2 "224"
           :y1 "175.4"
           :y2 "224"}]]

        ::magnifying-glass-plus
        [:svg {:fill "none"
               :viewBox "0 0 256 256"}
         [:line
          {:stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"
           :x1 "84"
           :x2 "148"
           :y1 "116"
           :y2 "116"}]
         [:line
          {:stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"
           :x1 "116"
           :x2 "116"
           :y1 "84"
           :y2 "148"}]
         [:circle
          {:cx "116"
           :cy "116"
           :r "84"
           :stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"}]
         [:line
          {:stroke "currentColor"
           :stroke-linecap "round"
           :stroke-linejoin "round"
           :stroke-width "16"
           :x1 "175.4"
           :x2 "224"
           :y1 "175.4"
           :y2 "224"}]]

        ::package-icon
        [:svg
         {:fill "currentColor" :viewBox "0 0 256 256"}
         [:path {:d "M223.68,66.15,135.68,18a15.88,15.88,0,0,0-15.36,0l-88,48.17a16,16,0,0,0-8.32,14v95.64a16,16,0,0,0,8.32,14l88,48.17a15.88,15.88,0,0,0,15.36,0l88-48.17a16,16,0,0,0,8.32-14V80.18A16,16,0,0,0,223.68,66.15ZM128,32l80.34,44-29.77,16.3-80.35-44ZM128,120,47.66,76l33.9-18.56,80.34,44ZM40,90l80,43.78v85.79L40,175.82Zm176,85.78h0l-80,43.79V133.82l32-17.51V152a8,8,0,0,0,16,0V107.55L216,90v85.77Z"}]]

        ::palette
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M200.77,53.89A103.27,103.27,0,0,0,128,24h-1.07A104,104,0,0,0,24,128c0,43,26.58,79.06,69.36,94.17A32,32,0,0,0,136,192a16,16,0,0,1,16-16h46.21a31.81,31.81,0,0,0,31.2-24.88,104.43,104.43,0,0,0,2.59-24A103.28,103.28,0,0,0,200.77,53.89Zm13,93.71A15.89,15.89,0,0,1,198.21,160H152a32,32,0,0,0-32,32,16,16,0,0,1-21.31,15.07C62.49,194.3,40,164,40,128a88,88,0,0,1,87.09-88h.9a88.35,88.35,0,0,1,88,87.25A88.86,88.86,0,0,1,213.81,147.6ZM140,76a12,12,0,1,1-12-12A12,12,0,0,1,140,76ZM96,100A12,12,0,1,1,84,88,12,12,0,0,1,96,100Zm0,56a12,12,0,1,1-12-12A12,12,0,0,1,96,156Zm88-56a12,12,0,1,1-12-12A12,12,0,0,1,184,100Z"}]]

        ::rows
        [:svg {:fill "none"
               :viewBox "0 0 256 256"}
         [:rect {:x "40" :y "144" :width "176" :height "56" :rx "8" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]
         [:rect {:x "40" :y "56" :width "176" :height "56" :rx "8" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]]

        ::thumbs-up
        [:svg {:viewBox "0 0 256 256"}
         [:path {:d "M234,80.12A24,24,0,0,0,216,72H160V56a40,40,0,0,0-40-40,8,8,0,0,0-7.16,4.42L75.06,96H32a16,16,0,0,0-16,16v88a16,16,0,0,0,16,16H204a24,24,0,0,0,23.82-21l12-96A24,24,0,0,0,234,80.12ZM32,112H72v88H32ZM223.94,97l-12,96a8,8,0,0,1-7.94,7H88V105.89l36.71-73.43A24,24,0,0,1,144,56V80a8,8,0,0,0,8,8h64a8,8,0,0,1,7.94,9Z"
                 :fill "currentColor"}]]

        ::trash
        [:svg {:viewBox "0 0 256 256"}
         [:line {:x1 "216" :y1 "56" :x2 "40" :y2 "56" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]
         [:line {:x1 "104" :y1 "104" :x2 "104" :y2 "168" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]
         [:line {:x1 "152" :y1 "104" :x2 "152" :y2 "168" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]
         [:path {:d "M200,56V208a8,8,0,0,1-8,8H64a8,8,0,0,1-8-8V56" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]
         [:path {:d "M168,56V40a16,16,0,0,0-16-16H104A16,16,0,0,0,88,40V56" :fill "none" :stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "16"}]]

        ::warning
        [:svg {:viewBox "0 0 256 256"}
         [:path {:d "M236.8,188.09,149.35,36.22h0a24.76,24.76,0,0,0-42.7,0L19.2,188.09a23.51,23.51,0,0,0,0,23.72A24.35,24.35,0,0,0,40.55,224h174.9a24.35,24.35,0,0,0,21.33-12.19A23.51,23.51,0,0,0,236.8,188.09ZM222.93,203.8a8.5,8.5,0,0,1-7.48,4.2H40.55a8.5,8.5,0,0,1-7.48-4.2,7.59,7.59,0,0,1,0-7.72L120.52,44.21a8.75,8.75,0,0,1,15,0l87.45,151.87A7.59,7.59,0,0,1,222.93,203.8ZM120,144V104a8,8,0,0,1,16,0v40a8,8,0,0,1-16,0Zm20,36a12,12,0,1,1-12-12A12,12,0,0,1,140,180Z"
                 :fill "currentColor"}]]

        ::x
        [:svg {:fill "currentColor"
               :viewBox "0 0 256 256"}
         [:path {:d "M205.66,194.34a8,8,0,0,1-11.32,11.32L128,139.31,61.66,205.66a8,8,0,0,1-11.32-11.32L116.69,128,50.34,61.66A8,8,0,0,1,61.66,50.34L128,116.69l66.34-66.35a8,8,0,0,1,11.32,11.32L139.31,128Z"}]]}
       (map (fn [[id svg]]
              [id (with-meta
                    svg
                    {:id id
                     `code/format-code (constantly svg)})]))
       (into {})))

(defn render [icon & [{:keys [size color style on-click]}]]
  [:span {:on-click on-click
          :style
          (cond-> {:display "inline-block"
                   :line-height "1"}
            size (assoc :height size)
            size (assoc :width size)
            color (assoc :color color)
            on-click (assoc :cursor "pointer")
            style (into style))}
   (icons icon)])

(defn get-icon-ids []
  (keys icons))
