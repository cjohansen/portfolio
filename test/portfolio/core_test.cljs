(ns portfolio.core-test
  (:require [portfolio.core :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest organization-test
  (testing "Breaks shared prefix into collection and namespaces"
    (is (= (sut/get-default-organization
            {}
            {}
            {:sasha.components.button-scenes/button
             {:id :sasha.components.button-scenes/button}
             :sasha.components.button-scenes/button-2
             {:id :sasha.components.button-scenes/button-2}
             :sasha.components.spinner-scenes/spinner-1
             {:id :sasha.components.spinner-scenes/spinner-1}})
           {:namespaces
            {"sasha.components.button-scenes"
             {:namespace "sasha.components.button-scenes"
              :title "button-scenes"
              :collection :components}
             "sasha.components.spinner-scenes"
             {:namespace "sasha.components.spinner-scenes"
              :title "spinner-scenes"
              :collection :components}},
            :collections
            {:components
             {:id :components
              :title "Components"}}})))

  (testing "Separates collections"
    (is (= (sut/get-default-organization
            {}
            {}
            {:sasha.components.button-scenes/button
             {:id :sasha.components.button-scenes/button}
             :sasha.components.spinner-scenes/spinner-1
             {:id :sasha.components.spinner-scenes/spinner-1}
             :sasha.icon.scenes/icon-list
             {:id :sasha.icon.scenes/icon-list}})
           {:namespaces
            {"sasha.components.button-scenes"
             {:namespace "sasha.components.button-scenes"
              :title "button-scenes"
              :collection :components}
             "sasha.components.spinner-scenes"
             {:namespace "sasha.components.spinner-scenes"
              :title "spinner-scenes"
              :collection :components}
             "sasha.icon.scenes"
             {:namespace "sasha.icon.scenes"
              :title "scenes"
              :collection :icon}}
            :collections
            {:components {:id :components, :title "Components"}
             :icon {:id :icon, :title "Icon"}}}))))

