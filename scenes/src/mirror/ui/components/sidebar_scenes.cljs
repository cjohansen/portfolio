(ns mirror.ui.components.sidebar-scenes
  (:require [portfolio.dumdom :as portfolio :refer-macros [defscene]]
            [portfolio.ui.components.sidebar :refer [Folder Package Unit]]))

(portfolio/configure-scenes
 {:title "Sidebar"})

(def package-illustration
  {:icon :ui.icons/package
   :color "var(--azure-radiance)"})

(defscene folder-title
  (Folder
   {:title "Layouts"
    :illustration {:icon :ui.icons/folder}}))

(defscene package-title
  (Package
   {:title "Some package"
    :illustration package-illustration
    :toggle {:icon :ui.icons/caret-down
             :actions []}}))

(defscene open-package
  (Package
   {:title "Some scenes"
    :illustration package-illustration
    :toggle {:icon :ui.icons/caret-right
             :actions []}
    :items [{:title "A scene"
             :context [:package]
             :illustration {:icon :ui.icons/bookmark
                            :color "var(--silver-tree)"}}]}))

(defscene open-folder
  (Folder
   {:title "Components"
    :kind :folder
    :illustration {:icon :ui.icons/folder-open}
    :items [{:title "Some scenes"
             :kind :package
             :illustration package-illustration
             :context [:folder]
             :toggle {:icon :ui.icons/caret-right
                      :actions []}
             :items [{:title "A scene"
                      :context [:folder :package]
                      :illustration {:icon :ui.icons/bookmark
                                     :color "var(--silver-tree)"}}]}]}))

(defscene nested-folder
  (Folder
   {:title "UI Kit"
    :illustration {:icon :ui.icons/folder-open}
    :kind :folder
    :items
    [{:title "Process samples"
      :kind :folder
      :context [:folder]
      :illustration {:icon :ui.icons/folder-open}
      :items [{:title "UI Malpractice: Don'ts"
               :kind :package
               :context [:folder :folder]
               :illustration package-illustration
               :toggle {:icon :ui.icons/caret-right
                        :actions []}
               :items [{:title "Poor legibility"
                        :context [:folder :folder :package]
                        :selected? true
                        :illustration {:icon :ui.icons/bookmark}}
                       {:title "Poor spacing"
                        :context [:folder :folder :package]
                        :illustration {:icon :ui.icons/bookmark
                                       :color "var(--silver-tree)"}}]}]}]}))

(defscene nested-package
  (Package
   {:title "UI Kit"
    :illustration package-illustration
    :kind :package
    :toggle {:icon :ui.icons/caret-right}
    :items
    [{:title "Some scenes"
      :kind :package
      :context [:package]
      :illustration package-illustration
      :toggle {:icon :ui.icons/caret-right
               :actions []}
      :items [{:title "A scene"
               :context [:package :package]
               :illustration {:icon :ui.icons/bookmark
                              :color "var(--silver-tree)"}}]}]}))
