(ns portfolio.collection-test
  (:require [clojure.test :refer [deftest is testing]]
            [portfolio.ui.collection :as sut]))

(deftest get-paths--test
  (testing "Discards shared prefix, but prefers 2 as min length"
    (is (= (sut/get-paths
            ["sasha.components.button-scenes"
             "sasha.components.spinner-scenes"])
           {:prefix "sasha"
            :paths [["components" "button-scenes"]
                    ["components" "spinner-scenes"]]})))

  (testing "Discards longer shared prefix"
    (is (= (sut/get-paths
            ["sasha.ui.components.button-scenes"
             "sasha.ui.components.spinner-scenes"])
           {:prefix "sasha.ui"
            :paths [["components" "button-scenes"]
                    ["components" "spinner-scenes"]]})))

  (testing "Can't discard prefixes when not shared by all entries"
    (is (= (sut/get-paths
            ["sasha.components.button-scenes"
             "sasha.components.spinner-scenes"
             "ui.icon.scenes"])
           {:paths [["sasha" "components" "button-scenes"]
                    ["sasha" "components" "spinner-scenes"]
                    ["ui" "icon" "scenes"]]}))))

(defn ->map [coll]
  (->> coll
       (map (juxt :id identity))
       (into {})))

(deftest suggest-collections--test
  (testing "Generates folders of packages"
    (is (= (sut/suggest-collections
            [{:id :sasha.components.button-scenes/button}
             {:id :sasha.components.button-scenes/button-2}
             {:id :sasha.components.spinner-scenes/spinner-1}])
           [{:id :sasha.components
             :title "Components"
             :kind :folder}
            {:id :sasha.components.button-scenes
             :title "Button scenes"
             :collection :sasha.components
             :kind :package}
            {:id :sasha.components.button-scenes
             :title "Button scenes"
             :collection :sasha.components
             :kind :package}
            {:id :sasha.components.spinner-scenes
             :title "Spinner scenes"
             :collection :sasha.components
             :kind :package}])))

  (testing "Generates folders for every top-level collection"
    (is (= (sut/suggest-collections
            [{:id :sasha.components.button-scenes/button}
             {:id :sasha.components.spinner-scenes/spinner-1}
             {:id :sasha.icon.scenes/icon-list}])
           [{:id :sasha.components
             :title "Components"
             :kind :folder}
            {:id :sasha.components.button-scenes
             :title "Button scenes"
             :collection :sasha.components
             :kind :package}
            {:id :sasha.components.spinner-scenes
             :title "Spinner scenes"
             :collection :sasha.components
             :kind :package}
            {:id :sasha.icon
             :title "Icon"
             :kind :folder}
            {:id :sasha.icon.scenes
             :title "Scenes"
             :collection :sasha.icon
             :kind :package}]))))

(deftest get-default-organization--test
  (testing "Generates a default hierarchy"
    (is (= (-> (sut/get-default-organization
                [{:id :sasha.components.button-scenes/button}
                 {:id :sasha.components.spinner-scenes/spinner-1}
                 {:id :sasha.icon.scenes/icon-list}]
                nil)
               keys)
           [:sasha.components
            :sasha.components.button-scenes
            :sasha.components.spinner-scenes
            :sasha.icon
            :sasha.icon.scenes])))

  (testing "Does not create default collections for pre-organized scenes"
    (is (= (-> (sut/get-default-organization
                [{:id :sasha.components.button-scenes/button
                  :collection :components}
                 {:id :sasha.icon.scenes/icon-list}]
                [{:id :components
                  :title "Sasha Components"}])
               vals
               set)
           #{{:id :components
              :kind :package
              :title "Sasha Components"}
             {:id :sasha.icon
              :kind :folder
              :title "Icon"}
             {:id :sasha.icon.scenes
              :title "Scenes"
              :collection :sasha.icon
              :kind :package}})))

  (testing "Fills in the missing holes"
    (is (= (-> (sut/get-default-organization
                {:sasha.components.button-scenes/button
                 {:id :sasha.components.button-scenes/button
                  :collection :components ;; Not defined
                  }
                 :sasha.icon.scenes/icon-list
                 {:id :sasha.icon.scenes/icon-list}}
                nil)
               vals
               set)
           #{{:id :components
              :title "Components"
              :kind :package}
             {:id :sasha.icon
              :title "Icon"
              :kind :folder}
             {:id :sasha.icon.scenes
              :title "Scenes"
              :collection :sasha.icon
              :kind :package}}))))

(deftest get-collection-path--test
  (testing "Returns collection and all its parents"
    (is (= (->> (sut/get-collection-path
                 {:scenes
                  {:sasha.components.button-scenes/default
                   {:id :sasha.components.button-scenes/default
                    :collection :sasha.components.button-scenes}}

                  :collections
                  {:sasha.control
                   {:id :sasha.control}

                   :sasha.ui
                   {:id :sasha.ui}

                   :sasha.components
                   {:id :sasha.components
                    :collection :sasha.ui}

                   :sasha.components.button-scenes
                   {:id :sasha.components.button-scenes
                    :collection :sasha.components}}}
                 :sasha.components.button-scenes/default)
                (map :id))
           [:sasha.ui
            :sasha.components
            :sasha.components.button-scenes
            :sasha.components.button-scenes/default])))

  (testing "Does not trip on circular collections"
    (is (= (->> (sut/get-collection-path
                 {:scenes
                  {:sasha.components.button-scenes/default
                   {:id :sasha.components.button-scenes/default
                    :collection :sasha.components.button-scenes}}

                  :collections
                  {:sasha.control
                   {:id :sasha.control}

                   :sasha.ui
                   {:id :sasha.ui}

                   :sasha.components
                   {:id :sasha.components
                    :collection :sasha.components}

                   :sasha.components.button-scenes
                   {:id :sasha.components.button-scenes
                    :collection :sasha.components}}}
                 :sasha.components.button-scenes/default)
                (map :id))
           [:sasha.components
            :sasha.components.button-scenes
            :sasha.components.button-scenes/default]))))

(deftest get-selected-scenes--test
  (testing "Returns currently selected scene, given its id"
    (is (= (->> (sut/get-selected-scenes
                 {:scenes
                  {:sasha.components.button-scenes/default
                   {:id :sasha.components.button-scenes/default
                    :collection :sasha.components.button-scenes}}}
                 :sasha.components.button-scenes/default)
                (map :id))
           [:sasha.components.button-scenes/default])))

  (testing "Returns currently selected scenes, given a collection id"
    (is (= (->> (sut/get-selected-scenes
                 {:scenes
                  {:sasha.components.button-scenes/default
                   {:id :sasha.components.button-scenes/default
                    :collection :sasha.components.button-scenes}}

                  :collections
                  {:sasha.control
                   {:id :sasha.control}

                   :sasha.ui
                   {:id :sasha.ui}

                   :sasha.components
                   {:id :sasha.components
                    :collection :sasha.ui}

                   :sasha.components.button-scenes
                   {:id :sasha.components.button-scenes
                    :collection :sasha.components}}}
                 :sasha.components.button-scenes)
                (map :id))
           [:sasha.components.button-scenes/default])))

  (testing "Returns all selected scenes under parent collection"
    (is (= (->> (sut/get-selected-scenes
                 {:scenes
                  {:sasha.components.button-scenes/default
                   {:id :sasha.components.button-scenes/default
                    :collection :sasha.components.button-scenes}

                   :sasha.components.icon-scenes/other
                   {:id :sasha.components.icon-scenes/other
                    :collection :sasha.components.icon-scenes}}

                  :collections
                  {:sasha.control
                   {:id :sasha.control}

                   :sasha.ui
                   {:id :sasha.ui}

                   :sasha.components
                   {:id :sasha.components
                    :collection :sasha.ui}

                   :sasha.components.button-scenes
                   {:id :sasha.components.button-scenes
                    :collection :sasha.components}

                   :sasha.components.icon-scenes
                   {:id :sasha.components.icon-scenes
                    :collection :sasha.components}}}
                 :sasha.ui)
                (map :id)
                set)
           #{:sasha.components.button-scenes/default
             :sasha.components.icon-scenes/other}))))
