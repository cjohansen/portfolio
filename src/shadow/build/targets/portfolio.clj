(ns shadow.build.targets.portfolio
  ":portfolio build target for shadow-cljs."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [hiccup.page :refer [html5]]
            [shadow.build :as build]
            [shadow.build.modules :as modules]
            [shadow.build.targets.browser :as browser]
            [shadow.build.test-util :as test-util]
            [shadow.jvm-log :as log]))

;; This code is very much based on the :browser-test target
;; https://github.com/thheller/shadow-cljs/blob/ea54e4b943c762645178303778aef094387f1a85/src/main/shadow/build/targets/browser_test.clj

(defn modify-config [{::build/keys [config] :as state}]
  (let [{:keys [runner-ns html-file] :or {runner-ns 'portfolio.runner}} config
        index-html (some-> html-file :path io/file)]

    (when (and index-html (not (.exists index-html)))
      (io/make-parents index-html)
      (spit index-html
        (html5
          {}
          [:head
           [:title (or (:title html-file) (str runner-ns))]
           [:meta {:charset "utf-8"}]]
          [:body
           [:script {:src (str (some-> state
                                       ::build/config
                                       :output-dir
                                       (str/split #"public")
                                       second)
                               "/portfolio.js")}]])))

    (-> state
        (assoc-in [:compiler-options :portfolio.ui/config] (-> state ::build/config :portfolio.ui/config))
        (assoc-in [::build/config :modules :portfolio] {:entries []})
        (assoc-in [::build/config :compiler-options :source-map] true)
        (update :build-options merge {:greedy true
                                      :dynamic-resolve true})
        (assoc ::test-util/runner-ns runner-ns)
        (assoc-in [::build/config :devtools :after-load] (symbol (str runner-ns) "start")))))

(defn resolve-portfolio [{::build/keys [mode config] :as state}]
  (let [config (update config :ns-regexp #(or % "-scenes$"))
        scene-namespaces (test-util/find-test-namespaces state config)]

    (log/debug ::test-resolve {:config config
                               :scene-namespaces scene-namespaces})

    (-> state
        (assoc ::test-util/test-namespaces scene-namespaces)
        (assoc-in [::modules/config :portfolio :entries]
                  (conj scene-namespaces (::test-util/runner-ns state)))
        (cond->
          (and (= :dev mode) (:worker-info state))
          (update-in [::modules/config :portfolio] browser/inject-repl-client state config)

          (= :dev mode)
          (-> (update-in [::modules/config :portfolio] browser/inject-preloads state config)
              (update-in [::modules/config :portfolio] browser/inject-devtools-console state config)))
        (modules/analyze)
        (test-util/inject-extra-requires))))

(defn ^:export process [{::build/keys [stage] :as state}]
  (-> state
      (cond->
        (= :configure stage)
        (modify-config)

        (= :resolve stage)
        (resolve-portfolio))

      (browser/process)))
