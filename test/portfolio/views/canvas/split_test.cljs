(ns portfolio.views.canvas.split-test
  (:require [portfolio.views.canvas.split :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest split-layout-horizontally--test
  (is (= (sut/split-layout-horizontally
          {:kind :cols
           :xs [{:pane :a}]} [0])
         {:kind :cols
          :xs [{:pane :a}
               {:pane :a}]}))

  (is (= (sut/split-layout-horizontally
          {:kind :rows
           :xs [{:pane :a}]} [0])
         {:kind :rows
          :xs [{:kind :cols
                :xs [{:pane :a}
                     {:pane :a}]}]}))

  (is (= (sut/split-layout-horizontally
          {:kind :rows
           :xs [{:kind :cols
                 :xs [{:pane :a}]}]} [0 0])
         {:kind :rows
          :xs [{:kind :cols
                :xs [{:pane :a}
                     {:pane :a}]}]}))

  (is (= (sut/split-layout-horizontally
          {:kind :cols
           :xs [{:kind :rows
                 :xs [{:pane :a}]}]} [0 0])
         {:kind :cols
          :xs [{:kind :rows
                :xs [{:kind :cols
                      :xs [{:pane :a} {:pane :a}]}]}]})))

(deftest split-layout-vertically--test
  (is (= (sut/split-layout-vertically
          {:kind :rows
           :xs [{:pane :a}]} [0])
         {:kind :rows
          :xs [{:pane :a}
               {:pane :a}]}))

  (is (= (sut/split-layout-vertically
          {:kind :cols
           :xs [{:pane :a}]} [0])
         {:kind :cols
          :xs [{:kind :rows
                :xs [{:pane :a}
                     {:pane :a}]}]}))

  (is (= (sut/split-layout-vertically
          {:kind :cols
           :xs [{:kind :rows
                 :xs [{:pane :a}]}]} [0 0])
         {:kind :cols
          :xs [{:kind :rows
                :xs [{:pane :a}
                     {:pane :a}]}]}))

  (is (= (sut/split-layout-vertically
          {:kind :rows
           :xs [{:kind :cols
                 :xs [{:pane :a}]}]} [0 0])
         {:kind :rows
          :xs [{:kind :cols
                :xs [{:kind :rows
                      :xs [{:pane :a} {:pane :a}]}]}]})))

(deftest close-pane--test
  (is (= (sut/close-pane
          {:kind :rows
           :xs [{:kind :cols
                 :xs [{:pane :a}
                      {:pane :b}]}]}
          [0 0])
         {:kind :rows
          :xs [{:kind :cols
                :xs [{:pane :b}]}]})))
