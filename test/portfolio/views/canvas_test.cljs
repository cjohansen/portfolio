(ns portfolio.views.canvas-test
  (:require [portfolio.views.canvas :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest inflate-layout--test
  (is (= (sut/inflate-layout [[{}]])
         {:kind :rows
          :xs [{:kind :cols
                :xs [{}]}]}))

  (is (= (sut/inflate-layout [[{:id [0 0]} {:id [0 1]}]
                              [{:id [1 0]} {:id [1 1]} {:id [1 2]}]])
         {:kind :rows
          :xs
          [{:kind :cols
            :xs [{:id [0 0]}
                 {:id [0 1]}]}
           {:kind :cols
            :xs [{:id [1 0]}
                 {:id [1 1]}
                 {:id [1 2]}]}]})))

(deftest prepare-layout--test
  (testing "Converts to renderable data"
    (is (= (sut/prepare-layout
            {}
            {}
            {}
            {:layout {:kind :cols
                      :xs [{}]}
             :source [:test]}
            [{:title "Test scene"}]
            false)
           {:kind :cols
            :xs [{:kind :pane
                  :canvases [{:scene {:title "Test scene"}
                              :opt {}}]}]
            :id :single-scene}))))
