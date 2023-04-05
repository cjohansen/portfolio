(ns portfolio.collection-test
  (:require [clojure.test :refer [deftest is testing]]
            [portfolio.ui.collection :as sut]))

(defn ->map [coll]
  (->> coll
       (map (juxt :id identity))
       (into {})))

(deftest get-default-organization--test
  (testing "Organizes namespaces into packages and folders"
    (is (= (sut/get-default-organization
            [{:id :sasha.components.button-scenes/button}
             {:id :sasha.components.spinner-scenes/spinner-1}
             {:id :sasha.icon.scenes/icon-list}]
            nil)
           #{{:id :sasha.icon
              :title "Icon"
              :kind :folder}
             {:id :sasha.icon.scenes
              :title "Scenes"
              :kind :package
              :collection :sasha.icon}
             {:id :sasha.components
              :title "Components"
              :kind :folder}
             {:id :sasha.components.spinner-scenes
              :title "Spinner scenes"
              :kind :package
              :collection :sasha.components}
             {:id :sasha.components.button-scenes
              :title "Button scenes"
              :kind :package
              :collection :sasha.components}})))

  (testing "Gracefully handles variable length namespaces"
    (is (= (->> (sut/get-default-organization
                 [{:id :sasha.components.button-scenes/button}
                  {:id :sasha.components.spinner-scenes/spinner-1}
                  {:id :sasha.icon-scenes/icon-list}]
                 nil)
                (map #(select-keys % [:id :collection]))
                set)
           #{{:id :sasha.icon-scenes
              :collection :sasha}
             {:id :sasha.components.button-scenes
              :collection :sasha.components}
             {:id :sasha.components.spinner-scenes
              :collection :sasha.components}
             {:id :sasha.components}
             {:id :sasha}})))

  (testing "Does not create default collections for pre-organized scenes"
    (is (= (sut/get-default-organization
            [{:id :sasha.components.button-scenes/button
              :collection :components}]
            [{:id :components
              :title "Sasha Components"}])
           #{{:id :components
              :kind :package
              :title "Sasha Components"}})))

  (testing "Fills in the missing holes"
    (is (= (-> (sut/get-default-organization
                [{:id :sasha.components.button-scenes/button
                  :collection :components ;; Not defined
                  }
                 {:id :sasha.icon.scenes/icon-list}]
                nil)
               set)
           #{{:id :components
              :title "Components"
              :kind :package}
             {:id :sasha.icon.scenes
              :title "Scenes"
              :kind :package}})))

  (testing "Insists on setting collection names"
    (is (= (->> (sut/get-default-organization
                 [{:id :sasha.components.button-scenes/button
                   :collection :components}
                  {:id :sasha.icon.scenes/icon-list}]
                 [{:id :components
                   :title nil}])
                (map :title)
                set)
           #{"Components" "Scenes"})))

  (testing "Skips folders if every scene fits in the same one"
    (is (= (->> (sut/get-default-organization
                 [{:id :sasha.components.button-scenes/button}
                  {:id :sasha.components.icon-scenes/list}]
                 nil)
                (map :id)
                set)
           #{:sasha.components.button-scenes
             :sasha.components.icon-scenes}))))

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
