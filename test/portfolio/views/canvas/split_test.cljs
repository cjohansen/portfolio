(ns portfolio.views.canvas.split-test
  (:require [portfolio.views.canvas.split :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest split-layout-horizontally--test
  (is (= (sut/split-layout-horizontally [[{}]] [0 0])
         [[{} {}]]))

  (is (= (sut/split-layout-horizontally [[{} {}]] [0 0])
         [[{} {} {}]]))

  (is (= (sut/split-layout-horizontally [[{:kind :a} {:kind :b}]] [0 1])
         [[{:kind :a} {:kind :b} {:kind :b}]])))
