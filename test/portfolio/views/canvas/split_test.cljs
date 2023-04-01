(ns portfolio.views.canvas.split-test
  (:require [portfolio.views.canvas.split :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest split-layout-horizontally--test
  (is (= (sut/split-layout-horizontally
          {:pane :a} [])
         {:kind :cols
          :xs [{:pane :a}
               {:pane :a}]}))

  (is (= (sut/split-layout-horizontally
          {:kind :cols
           :xs [{:pane :a}]} [0])
         {:kind :cols
          :xs [{:pane :a}
               {:pane :a}]}))

  (is (= (sut/split-layout-horizontally
          {:kind :rows
           :xs [{:pane :a}
                {:pane :b}]} [0])
         {:kind :rows
          :xs [{:kind :cols
                :xs [{:pane :a}
                     {:pane :a}]}
               {:pane :b}]}))

  (is (= (sut/split-layout-horizontally
          {:kind :rows
           :xs [{:kind :cols
                 :xs [{:pane :a}
                      {:pane :c}]}
                {:pane :b}]} [0 0])
         {:kind :rows
          :xs [{:kind :cols
                :xs [{:pane :a}
                     {:pane :a}
                     {:pane :c}]}
               {:pane :b}]}))

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
  (testing "Closes single pane"
    (is (= (sut/close-pane
            {:kind :rows
             :xs [{:kind :cols
                   :xs [{:pane :a}
                        {:pane :b}
                        {:pane :c}]}]}
            [0 0])
           {:kind :rows
            :xs [{:kind :cols
                  :xs [{:pane :b}
                       {:pane :c}]}]})))

  (testing "Collapses column with only one remaining pane"
    (is (= (sut/close-pane
            {:kind :rows
             :xs [{:kind :cols
                   :xs [{:pane :a}
                        {:pane :b}]}]}
            [0 0])
           {:kind :rows
            :xs [{:pane :b}]})))

  (testing "Collapses top level column"
    (is (= (sut/close-pane
            {:kind :cols
             :xs [{:pane :a}
                  {:pane :b}]}
            [0])
           {:pane :b}))))
