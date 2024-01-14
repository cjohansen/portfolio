(ns portfolio.ui
  (:require [portfolio.data :as data]
            [portfolio.homeless :as h]
            [portfolio.ui.actions :as actions]
            [portfolio.ui.canvas :as canvas]
            [portfolio.ui.canvas.accessibility :as accessibility]
            [portfolio.ui.canvas.background :as canvas-bg]
            [portfolio.ui.canvas.code :as code]
            [portfolio.ui.canvas.compare :as compare]
            [portfolio.ui.canvas.docs :as docs]
            [portfolio.ui.canvas.grid :as canvas-grid]
            [portfolio.ui.canvas.split :as split]
            [portfolio.ui.canvas.viewport :as canvas-vp]
            [portfolio.ui.canvas.zoom :as canvas-zoom]
            [portfolio.ui.client :as client]
            [portfolio.ui.collection :as collection]
            [portfolio.ui.routes :as routes]
            [portfolio.ui.search :as search]
            [portfolio.ui.search.protocols :as index]))

(defonce app (atom nil))

(defn get-collections [scenes collections]
  (->> (collection/get-default-organization (vals scenes) (vals collections))
       (map (juxt :id identity))
       (into {})))

(defn portfolio-docs? [user-v]
  (if (nil? user-v)
    (boolean (or (= "localhost" js/location.hostname)
                 (re-find #"\d+\.\d+\.\d+\.\d+" js/location.href)))
    user-v))

(defn create-app [config canvas-tools extra-canvas-tools]
  (-> config
      (update :portfolio-docs? portfolio-docs?)
      (assoc :scenes @data/scenes)
      (assoc :collections (get-collections @data/scenes @data/collections))
      (assoc :views [(canvas/create-canvas
                      {:canvas/layout (:canvas/layout config)
                       :tools (into (or canvas-tools
                                        [(canvas-bg/create-background-tool config)
                                         (canvas-vp/create-viewport-tool config)
                                         (canvas-grid/create-grid-tool config)
                                         (canvas-zoom/create-zoom-tool config)
                                         (split/create-split-tool config)
                                         (docs/create-docs-tool config)
                                         (code/create-code-tool config)
                                         (accessibility/create-accessibility-tool config)
                                         (compare/create-compare-tool config)
                                         (split/create-close-tool config)])
                                    extra-canvas-tools)})])))

(def eventually-execute (h/debounce actions/execute-action! 250))

(defn index-content [app & [{:keys [ids]}]]
  (let [{:keys [index scenes collections log?]} @app]
    (when index
      (js/requestAnimationFrame
       (fn [_]
         (doseq [doc (cond->> (concat (vals scenes) (vals collections))
                       ids (filter (comp (set ids) :id)))]
           (when log?
             (println "Index" (:id doc)))
           (index/index index doc)))))))

(defn render-scene [x]
  (when-let [scene (data/get-tapped-scene x)]
    (data/register-repl-scene! scene)
    (actions/execute-action! app [:go-to-location (routes/get-scene-location (routes/get-current-location) scene)])))

(defn start! [& [{:keys [on-render config canvas-tools extra-canvas-tools index get-indexable-data] :as opt}]]
  (let [->diffable (partial search/get-diffables (or get-indexable-data search/get-indexable-data))]
    (swap! app merge (create-app config canvas-tools extra-canvas-tools) {:index index})

    (when-not (client/started? app)
      (add-watch data/scenes ::app
        (fn [_ _ old-scenes scenes]
          (let [collections (get-collections scenes (:collections @app))
                old-collections (get-collections old-scenes (:collections @app))]
            (swap! app (fn [state]
                         (-> state
                             (assoc :scenes scenes)
                             (assoc :collections collections))))
            (when (:reindex? opt true)
              (index-content
               app
               {:ids (concat
                      (search/get-diff-keys (->diffable scenes) (->diffable old-scenes))
                      (search/get-diff-keys (->diffable collections) (->diffable old-collections)))})))
          (eventually-execute app [:go-to-current-location])))

      (add-watch data/collections ::app
        (fn [_ _ _ collections]
          (let [old-collections (:collections @app)
                collections (get-collections (:scenes @app) collections)]
            (swap! app assoc :collections collections)
            (when (:reindex? opt true)
              (index-content app {:ids (search/get-diff-keys (->diffable collections) (->diffable old-collections))})))))

      (add-tap render-scene)

      (js/window.addEventListener
       "message"
       (fn [e]
         (when (.. e -data -action)
           (when-let [action (actions/get-action (.-data e))]
             (actions/execute-action! app action)))))))

  (when-not (client/started? app)
    (index-content app))

  (client/start-app app {:on-render on-render}))

(comment
  {:toolOptions {:reporter "v1"}
   :testRunner {:name "axe"}
   :incomplete []
   :passes [{:id "aria-allowed-role"
             :impact nil
             :tags ["cat.aria" "best-practice"]
             :description "Ensures role attribute has an appropriate value for the element"
             :help "ARIA role should be appropriate for the element"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-allowed-role?application=axeAPI"
             :nodes [{:any [{:id "aria-allowed-role"
                             :data nil
                             :relatedNodes []
                             :impact "minor"
                             :message "ARIA role is allowed for given element"}]
                      :all []
                      :none []
                      :impact nil
                      :html "<main id=\"canvas\" role=\"main\" data-dumdom-id=\"1\"><div><button class=\"button\">Hello, clicky!</button></div></main>"
                      :target ["#canvas"]}]}
            {:id "aria-required-attr"
             :impact nil
             :tags ["cat.aria" "wcag2a" "wcag412"]
             :description "Ensures elements with ARIA roles have all required ARIA attributes"
             :help "Required ARIA attributes must be provided"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-required-attr?application=axeAPI"
             :nodes [{:any [{:id "aria-required-attr"
                             :data nil
                             :relatedNodes []
                             :impact "critical"
                             :message "All required ARIA attributes are present"}]
                      :all []
                      :none []
                      :impact nil
                      :html "<main id=\"canvas\" role=\"main\" data-dumdom-id=\"1\"><div><button class=\"button\">Hello, clicky!</button></div></main>"
                      :target ["#canvas"]}]}
            {:id "aria-roles"
             :impact nil
             :tags ["cat.aria" "wcag2a" "wcag412"]
             :description "Ensures all elements with a role attribute use a valid value"
             :help "ARIA roles used must conform to valid values"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-roles?application=axeAPI"
             :nodes [{:any []
                      :all []
                      :none [{:id "invalidrole"
                              :data nil
                              :relatedNodes []
                              :impact "critical"
                              :message "ARIA role is valid"}
                             {:id "abstractrole"
                              :data nil
                              :relatedNodes []
                              :impact "serious"
                              :message "Abstract roles are not used"}
                             {:id "unsupportedrole"
                              :data nil
                              :relatedNodes []
                              :impact "critical"
                              :message "ARIA role is supported"}
                             {:id "deprecatedrole"
                              :data nil
                              :relatedNodes []
                              :impact "minor"
                              :message "ARIA role is not deprecated"}]
                      :impact nil
                      :html "<main id=\"canvas\" role=\"main\" data-dumdom-id=\"1\"><div><button class=\"button\">Hello, clicky!</button></div></main>"
                      :target ["#canvas"]}]}
            {:id "button-name"
             :impact nil
             :tags ["cat.name-role-value" "wcag2a" "wcag412" "section508" "section508.22.a" "ACT" "TTv5" "TT6.a"]
             :description "Ensures buttons have discernible text"
             :help "Buttons must have discernible text"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/button-name?application=axeAPI"
             :nodes [{:any [{:id "button-has-visible-text"
                             :data nil
                             :relatedNodes []
                             :impact "critical"
                             :message "Element has inner text that is visible to screen readers"}]
                      :all []
                      :none []
                      :impact nil
                      :html "<button class=\"button\">Hello, clicky!</button>"
                      :target ["button"]}]}
            {:id "color-contrast"
             :impact nil
             :tags ["cat.color" "wcag2aa" "wcag143" "ACT" "TTv5" "TT13.c"]
             :description "Ensures the contrast between foreground and background colors meets WCAG 2 AA minimum contrast ratio thresholds"
             :help "Elements must meet minimum color contrast ratio thresholds"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/color-contrast?application=axeAPI"
             :nodes [{:any [{:id "color-contrast"
                             :data {:fgColor "#ffffff"
                                    :bgColor "#db162f"
                                    :contrastRatio 5.03
                                    :fontSize "10.0pt (13.3333px)"
                                    :fontWeight "normal"
                                    :expectedContrastRatio "4.5:1"}
                             :relatedNodes []
                             :impact "serious"
                             :message "Element has sufficient color contrast of 5.03"}]
                      :all []
                      :none []
                      :impact nil
                      :html "<button class=\"button\">Hello, clicky!</button>"
                      :target ["button"]}]}
            {:id "duplicate-id"
             :impact nil
             :tags ["cat.parsing" "wcag2a" "wcag411"]
             :description "Ensures every id attribute value is unique"
             :help "id attribute value must be unique"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/duplicate-id?application=axeAPI"
             :nodes [{:any [{:id "duplicate-id"
                             :data "canvas"
                             :relatedNodes []
                             :impact "minor"
                             :message "Document has no static elements that share the same id attribute"}]
                      :all []
                      :none []
                      :impact nil
                      :html "<main id=\"canvas\" role=\"main\" data-dumdom-id=\"1\"><div><button class=\"button\">Hello, clicky!</button></div></main>"
                      :target ["#canvas"]}]}
            {:id "landmark-main-is-top-level"
             :impact nil
             :tags ["cat.semantics" "best-practice"]
             :description "Ensures the main landmark is at top level"
             :help "Main landmark should not be contained in another landmark"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-main-is-top-level?application=axeAPI"
             :nodes [{:any [{:id "landmark-is-top-level"
                             :data {:role "main"}
                             :relatedNodes []
                             :impact "moderate"
                             :message "The main landmark is at the top level."}]
                      :all []
                      :none []
                      :impact nil
                      :html "<main id=\"canvas\" role=\"main\" data-dumdom-id=\"1\"><div><button class=\"button\">Hello, clicky!</button></div></main>"
                      :target ["#canvas"]}]}
            {:id "landmark-no-duplicate-main"
             :impact nil
             :tags ["cat.semantics" "best-practice"]
             :description "Ensures the document has at most one main landmark"
             :help "Document should not have more than one main landmark"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-no-duplicate-main?application=axeAPI"
             :nodes [{:any [{:id "page-no-duplicate-main"
                             :data nil
                             :relatedNodes []
                             :impact "moderate"
                             :message "Document does not have more than one main landmark"}]
                      :all []
                      :none []
                      :impact nil
                      :html "<main id=\"canvas\" role=\"main\" data-dumdom-id=\"1\"><div><button class=\"button\">Hello, clicky!</button></div></main>"
                      :target ["#canvas"]}]}
            {:id "landmark-unique"
             :impact nil
             :tags ["cat.semantics" "best-practice"]
             :help "Ensures landmarks are unique"
             :description "Landmarks should have a unique role or role/label/title (i.e. accessible name) combination"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-unique?application=axeAPI"
             :nodes [{:any [{:id "landmark-is-unique"
                             :data {:role "main"
                                    :accessibleText nil}
                             :relatedNodes []
                             :impact "moderate"
                             :message "Landmarks must have a unique role or role/label/title (i.e. accessible name) combination"}]
                      :all []
                      :none []
                      :impact nil
                      :html "<main id=\"canvas\" role=\"main\" data-dumdom-id=\"1\"><div><button class=\"button\">Hello, clicky!</button></div></main>"
                      :target ["#canvas"]}]}
            {:id "nested-interactive"
             :impact nil
             :tags ["cat.keyboard" "wcag2a" "wcag412" "TTv5" "TT4.a"]
             :description "Ensures interactive controls are not nested as they are not always announced by screen readers or can cause focus problems for assistive technologies"
             :help "Interactive controls must not be nested"
             :helpUrl "https://dequeuniversity.com/rules/axe/4.7/nested-interactive?application=axeAPI"
             :nodes [{:any [{:id "no-focusable-content"
                             :data nil
                             :relatedNodes []
                             :impact "serious"
                             :message "Element does not have focusable descendants"}]
                      :all []
                      :none []
                      :impact nil
                      :html "<button class=\"button\">Hello, clicky!</button>"
                      :target ["button"]}]}]
   :testEnvironment {:userAgent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"
                     :windowWidth 507
                     :windowHeight 76
                     :orientationAngle 0
                     :orientationType "landscape-primary"}
   :testEngine {:name "axe-core"
                :version "4.7.0"}
   :url "http://localhost:5995/portfolio/canvas.html"
   :inapplicable [{:id "accesskeys"
                   :impact nil
                   :tags ["cat.keyboard" "best-practice"]
                   :description "Ensures every accesskey attribute value is unique"
                   :help "accesskey attribute value should be unique"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/accesskeys?application=axeAPI"
                   :nodes []}
                  {:id "area-alt"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag244" "wcag412" "section508" "section508.22.a" "ACT" "TTv5" "TT6.a"]
                   :description "Ensures <area> elements of image maps have alternate text"
                   :help "Active <area> elements must have alternate text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/area-alt?application=axeAPI"
                   :nodes []}
                  {:id "aria-allowed-attr"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag412"]
                   :description "Ensures ARIA attributes are allowed for an element's role"
                   :help "Elements must only use allowed ARIA attributes"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-allowed-attr?application=axeAPI"
                   :nodes []}
                  {:id "aria-command-name"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag412" "ACT" "TTv5" "TT6.a"]
                   :description "Ensures every ARIA button
 link and menuitem has an accessible name"
                   :help "ARIA commands must have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-command-name?application=axeAPI"
                   :nodes []}
                  {:id "aria-dialog-name"
                   :impact nil
                   :tags ["cat.aria" "best-practice"]
                   :description "Ensures every ARIA dialog and alertdialog node has an accessible name"
                   :help "ARIA dialog and alertdialog nodes should have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-dialog-name?application=axeAPI"
                   :nodes []}
                  {:id "aria-hidden-body"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag412"]
                   :description "Ensures aria-hidden='true' is not present on the document body."
                   :help "aria-hidden='true' must not be present on the document body"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-hidden-body?application=axeAPI"
                   :nodes []}
                  {:id "aria-hidden-focus"
                   :impact nil
                   :tags ["cat.name-role-value" "wcag2a" "wcag412"]
                   :description "Ensures aria-hidden elements are not focusable nor contain focusable elements"
                   :help "ARIA hidden element must not be focusable or contain focusable elements"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-hidden-focus?application=axeAPI"
                   :nodes []}
                  {:id "aria-input-field-name"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag412" "ACT" "TTv5" "TT5.c"]
                   :description "Ensures every ARIA input field has an accessible name"
                   :help "ARIA input fields must have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-input-field-name?application=axeAPI"
                   :nodes []}
                  {:id "aria-meter-name"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag111"]
                   :description "Ensures every ARIA meter node has an accessible name"
                   :help "ARIA meter nodes must have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-meter-name?application=axeAPI"
                   :nodes []}
                  {:id "aria-progressbar-name"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag111"]
                   :description "Ensures every ARIA progressbar node has an accessible name"
                   :help "ARIA progressbar nodes must have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-progressbar-name?application=axeAPI"
                   :nodes []}
                  {:id "aria-required-children"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag131"]
                   :description "Ensures elements with an ARIA role that require child roles contain them"
                   :help "Certain ARIA roles must contain particular children"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-required-children?application=axeAPI"
                   :nodes []}
                  {:id "aria-required-parent"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag131"]
                   :description "Ensures elements with an ARIA role that require parent roles are contained by them"
                   :help "Certain ARIA roles must be contained by particular parents"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-required-parent?application=axeAPI"
                   :nodes []}
                  {:id "aria-text"
                   :impact nil
                   :tags ["cat.aria" "best-practice"]
                   :description "Ensures \"role=text\" is used on elements with no focusable descendants"
                   :help "\"role=text\" should have no focusable descendants"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-text?application=axeAPI"
                   :nodes []}
                  {:id "aria-toggle-field-name"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag412" "ACT" "TTv5" "TT5.c"]
                   :description "Ensures every ARIA toggle field has an accessible name"
                   :help "ARIA toggle fields must have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-toggle-field-name?application=axeAPI"
                   :nodes []}
                  {:id "aria-tooltip-name"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag412"]
                   :description "Ensures every ARIA tooltip node has an accessible name"
                   :help "ARIA tooltip nodes must have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-tooltip-name?application=axeAPI"
                   :nodes []}
                  {:id "aria-treeitem-name"
                   :impact nil
                   :tags ["cat.aria" "best-practice"]
                   :description "Ensures every ARIA treeitem node has an accessible name"
                   :help "ARIA treeitem nodes should have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-treeitem-name?application=axeAPI"
                   :nodes []}
                  {:id "aria-valid-attr-value"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag412"]
                   :description "Ensures all ARIA attributes have valid values"
                   :help "ARIA attributes must conform to valid values"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-valid-attr-value?application=axeAPI"
                   :nodes []}
                  {:id "aria-valid-attr"
                   :impact nil
                   :tags ["cat.aria" "wcag2a" "wcag412"]
                   :description "Ensures attributes that begin with aria- are valid ARIA attributes"
                   :help "ARIA attributes must conform to valid names"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/aria-valid-attr?application=axeAPI"
                   :nodes []}
                  {:id "autocomplete-valid"
                   :impact nil
                   :tags ["cat.forms" "wcag21aa" "wcag135" "ACT"]
                   :description "Ensure the autocomplete attribute is correct and suitable for the form field"
                   :help "autocomplete attribute must be used correctly"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/autocomplete-valid?application=axeAPI"
                   :nodes []}
                  {:id "avoid-inline-spacing"
                   :impact nil
                   :tags ["cat.structure" "wcag21aa" "wcag1412" "ACT"]
                   :description "Ensure that text spacing set through style attributes can be adjusted with custom stylesheets"
                   :help "Inline text spacing must be adjustable with custom stylesheets"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/avoid-inline-spacing?application=axeAPI"
                   :nodes []}
                  {:id "blink"
                   :impact nil
                   :tags ["cat.time-and-media" "wcag2a" "wcag222" "section508" "section508.22.j" "TTv5" "TT2.b"]
                   :description "Ensures <blink> elements are not used"
                   :help "<blink> elements are deprecated and must not be used"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/blink?application=axeAPI"
                   :nodes []}
                  {:id "definition-list"
                   :impact nil
                   :tags ["cat.structure" "wcag2a" "wcag131"]
                   :description "Ensures <dl> elements are structured correctly"
                   :help "<dl> elements must only directly contain properly-ordered <dt> and <dd> groups
 <script>
 <template> or <div> elements"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/definition-list?application=axeAPI"
                   :nodes []}
                  {:id "dlitem"
                   :impact nil
                   :tags ["cat.structure" "wcag2a" "wcag131"]
                   :description "Ensures <dt> and <dd> elements are contained by a <dl>"
                   :help "<dt> and <dd> elements must be contained by a <dl>"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/dlitem?application=axeAPI"
                   :nodes []}
                  {:id "document-title"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag242" "ACT" "TTv5" "TT12.a"]
                   :description "Ensures each HTML document contains a non-empty <title> element"
                   :help "Documents must have <title> element to aid in navigation"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/document-title?application=axeAPI"
                   :nodes []}
                  {:id "duplicate-id-active"
                   :impact nil
                   :tags ["cat.parsing" "wcag2a" "wcag411"]
                   :description "Ensures every id attribute value of active elements is unique"
                   :help "IDs of active elements must be unique"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/duplicate-id-active?application=axeAPI"
                   :nodes []}
                  {:id "duplicate-id-aria"
                   :impact nil
                   :tags ["cat.parsing" "wcag2a" "wcag411"]
                   :description "Ensures every id attribute value used in ARIA and in labels is unique"
                   :help "IDs used in ARIA and labels must be unique"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/duplicate-id-aria?application=axeAPI"
                   :nodes []}
                  {:id "empty-heading"
                   :impact nil
                   :tags ["cat.name-role-value" "best-practice"]
                   :description "Ensures headings have discernible text"
                   :help "Headings should not be empty"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/empty-heading?application=axeAPI"
                   :nodes []}
                  {:id "empty-table-header"
                   :impact nil
                   :tags ["cat.name-role-value" "best-practice"]
                   :description "Ensures table headers have discernible text"
                   :help "Table header text should not be empty"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/empty-table-header?application=axeAPI"
                   :nodes []}
                  {:id "form-field-multiple-labels"
                   :impact nil
                   :tags ["cat.forms" "wcag2a" "wcag332" "TTv5" "TT5.c"]
                   :description "Ensures form field does not have multiple label elements"
                   :help "Form field must not have multiple label elements"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/form-field-multiple-labels?application=axeAPI"
                   :nodes []}
                  {:id "frame-focusable-content"
                   :impact nil
                   :tags ["cat.keyboard" "wcag2a" "wcag211" "TTv5" "TT4.a"]
                   :description "Ensures <frame> and <iframe> elements with focusable content do not have tabindex=-1"
                   :help "Frames with focusable content must not have tabindex=-1"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/frame-focusable-content?application=axeAPI"
                   :nodes []}
                  {:id "frame-tested"
                   :impact nil
                   :tags ["cat.structure" "review-item" "best-practice"]
                   :description "Ensures <iframe> and <frame> elements contain the axe-core script"
                   :help "Frames should be tested with axe-core"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/frame-tested?application=axeAPI"
                   :nodes []}
                  {:id "frame-title-unique"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag412" "wcag2a" "TTv5" "TT12.c"]
                   :description "Ensures <iframe> and <frame> elements contain a unique title attribute"
                   :help "Frames must have a unique title attribute"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/frame-title-unique?application=axeAPI"
                   :nodes []}
                  {:id "frame-title"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag412" "section508" "section508.22.i" "TTv5" "TT12.c"]
                   :description "Ensures <iframe> and <frame> elements have an accessible name"
                   :help "Frames must have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/frame-title?application=axeAPI"
                   :nodes []}
                  {:id "heading-order"
                   :impact nil
                   :tags ["cat.semantics" "best-practice"]
                   :description "Ensures the order of headings is semantically correct"
                   :help "Heading levels should only increase by one"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/heading-order?application=axeAPI"
                   :nodes []}
                  {:id "html-has-lang"
                   :impact nil
                   :tags ["cat.language" "wcag2a" "wcag311" "ACT" "TTv5" "TT11.a"]
                   :description "Ensures every HTML document has a lang attribute"
                   :help "<html> element must have a lang attribute"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/html-has-lang?application=axeAPI"
                   :nodes []}
                  {:id "html-lang-valid"
                   :impact nil
                   :tags ["cat.language" "wcag2a" "wcag311" "ACT" "TTv5" "TT11.a"]
                   :description "Ensures the lang attribute of the <html> element has a valid value"
                   :help "<html> element must have a valid value for the lang attribute"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/html-lang-valid?application=axeAPI"
                   :nodes []}
                  {:id "html-xml-lang-mismatch"
                   :impact nil
                   :tags ["cat.language" "wcag2a" "wcag311" "ACT"]
                   :description "Ensure that HTML elements with both valid lang and xml:lang attributes agree on the base language of the page"
                   :help "HTML elements with lang and xml:lang must have the same base language"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/html-xml-lang-mismatch?application=axeAPI"
                   :nodes []}
                  {:id "image-alt"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag111" "section508" "section508.22.a" "ACT" "TTv5" "TT7.a" "TT7.b"]
                   :description "Ensures <img> elements have alternate text or a role of none or presentation"
                   :help "Images must have alternate text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/image-alt?application=axeAPI"
                   :nodes []}
                  {:id "image-redundant-alt"
                   :impact nil
                   :tags ["cat.text-alternatives" "best-practice"]
                   :description "Ensure image alternative is not repeated as text"
                   :help "Alternative text of images should not be repeated as text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/image-redundant-alt?application=axeAPI"
                   :nodes []}
                  {:id "input-button-name"
                   :impact nil
                   :tags ["cat.name-role-value" "wcag2a" "wcag412" "section508" "section508.22.a" "ACT" "TTv5" "TT5.c"]
                   :description "Ensures input buttons have discernible text"
                   :help "Input buttons must have discernible text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/input-button-name?application=axeAPI"
                   :nodes []}
                  {:id "input-image-alt"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag111" "wcag412" "section508" "section508.22.a" "ACT" "TTv5" "TT7.a"]
                   :description "Ensures <input type=\"image\"> elements have alternate text"
                   :help "Image buttons must have alternate text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/input-image-alt?application=axeAPI"
                   :nodes []}
                  {:id "label-title-only"
                   :impact nil
                   :tags ["cat.forms" "best-practice"]
                   :description "Ensures that every form element has a visible label and is not solely labeled using hidden labels
 or the title or aria-describedby attributes"
                   :help "Form elements should have a visible label"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/label-title-only?application=axeAPI"
                   :nodes []}
                  {:id "label"
                   :impact nil
                   :tags ["cat.forms" "wcag2a" "wcag412" "section508" "section508.22.n" "ACT" "TTv5" "TT5.c"]
                   :description "Ensures every form element has a label"
                   :help "Form elements must have labels"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/label?application=axeAPI"
                   :nodes []}
                  {:id "landmark-banner-is-top-level"
                   :impact nil
                   :tags ["cat.semantics" "best-practice"]
                   :description "Ensures the banner landmark is at top level"
                   :help "Banner landmark should not be contained in another landmark"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-banner-is-top-level?application=axeAPI"
                   :nodes []}
                  {:id "landmark-complementary-is-top-level"
                   :impact nil
                   :tags ["cat.semantics" "best-practice"]
                   :description "Ensures the complementary landmark or aside is at top level"
                   :help "Aside should not be contained in another landmark"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-complementary-is-top-level?application=axeAPI"
                   :nodes []}
                  {:id "landmark-contentinfo-is-top-level"
                   :impact nil
                   :tags ["cat.semantics" "best-practice"]
                   :description "Ensures the contentinfo landmark is at top level"
                   :help "Contentinfo landmark should not be contained in another landmark"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-contentinfo-is-top-level?application=axeAPI"
                   :nodes []}
                  {:id "landmark-no-duplicate-banner"
                   :impact nil
                   :tags ["cat.semantics" "best-practice"]
                   :description "Ensures the document has at most one banner landmark"
                   :help "Document should not have more than one banner landmark"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-no-duplicate-banner?application=axeAPI"
                   :nodes []}
                  {:id "landmark-no-duplicate-contentinfo"
                   :impact nil
                   :tags ["cat.semantics" "best-practice"]
                   :description "Ensures the document has at most one contentinfo landmark"
                   :help "Document should not have more than one contentinfo landmark"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-no-duplicate-contentinfo?application=axeAPI"
                   :nodes []}
                  {:id "landmark-one-main"
                   :impact nil
                   :tags ["cat.semantics" "best-practice"]
                   :description "Ensures the document has a main landmark"
                   :help "Document should have one main landmark"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/landmark-one-main?application=axeAPI"
                   :nodes []}
                  {:id "link-in-text-block"
                   :impact nil
                   :tags ["cat.color" "wcag2a" "wcag141" "TTv5" "TT13.a"]
                   :description "Ensure links are distinguished from surrounding text in a way that does not rely on color"
                   :help "Links must be distinguishable without relying on color"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/link-in-text-block?application=axeAPI"
                   :nodes []}
                  {:id "link-name"
                   :impact nil
                   :tags ["cat.name-role-value" "wcag2a" "wcag412" "wcag244" "section508" "section508.22.a" "ACT" "TTv5" "TT6.a"]
                   :description "Ensures links have discernible text"
                   :help "Links must have discernible text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/link-name?application=axeAPI"
                   :nodes []}
                  {:id "list"
                   :impact nil
                   :tags ["cat.structure" "wcag2a" "wcag131"]
                   :description "Ensures that lists are structured correctly"
                   :help "<ul> and <ol> must only directly contain <li>
 <script> or <template> elements"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/list?application=axeAPI"
                   :nodes []}
                  {:id "listitem"
                   :impact nil
                   :tags ["cat.structure" "wcag2a" "wcag131"]
                   :description "Ensures <li> elements are used semantically"
                   :help "<li> elements must be contained in a <ul> or <ol>"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/listitem?application=axeAPI"
                   :nodes []}
                  {:id "marquee"
                   :impact nil
                   :tags ["cat.parsing" "wcag2a" "wcag222" "TTv5" "TT2.b"]
                   :description "Ensures <marquee> elements are not used"
                   :help "<marquee> elements are deprecated and must not be used"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/marquee?application=axeAPI"
                   :nodes []}
                  {:id "meta-refresh"
                   :impact nil
                   :tags ["cat.time-and-media" "wcag2a" "wcag221" "TTv5" "TT2.c"]
                   :description "Ensures <meta http-equiv=\"refresh\"> is not used for delayed refresh"
                   :help "Delayed refresh under 20 hours must not be used"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/meta-refresh?application=axeAPI"
                   :nodes []}
                  {:id "meta-viewport-large"
                   :impact nil
                   :tags ["cat.sensory-and-visual-cues" "best-practice"]
                   :description "Ensures <meta name=\"viewport\"> can scale a significant amount"
                   :help "Users should be able to zoom and scale the text up to 500%"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/meta-viewport-large?application=axeAPI"
                   :nodes []}
                  {:id "meta-viewport"
                   :impact nil
                   :tags ["cat.sensory-and-visual-cues" "wcag2aa" "wcag144" "ACT"]
                   :description "Ensures <meta name=\"viewport\"> does not disable text scaling and zooming"
                   :help "Zooming and scaling must not be disabled"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/meta-viewport?application=axeAPI"
                   :nodes []}
                  {:id "object-alt"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag111" "section508" "section508.22.a"]
                   :description "Ensures <object> elements have alternate text"
                   :help "<object> elements must have alternate text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/object-alt?application=axeAPI"
                   :nodes []}
                  {:id "page-has-heading-one"
                   :impact nil
                   :tags ["cat.semantics" "best-practice"]
                   :description "Ensure that the page
 or at least one of its frames contains a level-one heading"
                   :help "Page should contain a level-one heading"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/page-has-heading-one?application=axeAPI"
                   :nodes []}
                  {:id "presentation-role-conflict"
                   :impact nil
                   :tags ["cat.aria" "best-practice" "ACT"]
                   :description "Elements marked as presentational should not have global ARIA or tabindex to ensure all screen readers ignore them"
                   :help "Ensure elements marked as presentational are consistently ignored"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/presentation-role-conflict?application=axeAPI"
                   :nodes []}
                  {:id "region"
                   :impact nil
                   :tags ["cat.keyboard" "best-practice"]
                   :description "Ensures all page content is contained by landmarks"
                   :help "All page content should be contained by landmarks"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/region?application=axeAPI"
                   :nodes []}
                  {:id "role-img-alt"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag111" "section508" "section508.22.a" "ACT" "TTv5" "TT7.a"]
                   :description "Ensures [role='img'] elements have alternate text"
                   :help "[role='img'] elements must have an alternative text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/role-img-alt?application=axeAPI"
                   :nodes []}
                  {:id "scope-attr-valid"
                   :impact nil
                   :tags ["cat.tables" "best-practice"]
                   :description "Ensures the scope attribute is used correctly on tables"
                   :help "scope attribute should be used correctly"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/scope-attr-valid?application=axeAPI"
                   :nodes []}
                  {:id "scrollable-region-focusable"
                   :impact nil
                   :tags ["cat.keyboard" "wcag2a" "wcag211"]
                   :description "Ensure elements that have scrollable content are accessible by keyboard"
                   :help "Scrollable region must have keyboard access"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/scrollable-region-focusable?application=axeAPI"
                   :nodes []}
                  {:id "select-name"
                   :impact nil
                   :tags ["cat.forms" "wcag2a" "wcag412" "section508" "section508.22.n" "ACT" "TTv5" "TT5.c"]
                   :description "Ensures select element has an accessible name"
                   :help "Select element must have an accessible name"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/select-name?application=axeAPI"
                   :nodes []}
                  {:id "server-side-image-map"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag211" "section508" "section508.22.f"]
                   :description "Ensures that server-side image maps are not used"
                   :help "Server-side image maps must not be used"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/server-side-image-map?application=axeAPI"
                   :nodes []}
                  {:id "skip-link"
                   :impact nil
                   :tags ["cat.keyboard" "best-practice"]
                   :description "Ensure all skip links have a focusable target"
                   :help "The skip-link target should exist and be focusable"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/skip-link?application=axeAPI"
                   :nodes []}
                  {:id "svg-img-alt"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag111" "section508" "section508.22.a" "ACT" "TTv5" "TT7.a"]
                   :description "Ensures <svg> elements with an img
 graphics-document or graphics-symbol role have an accessible text"
                   :help "<svg> elements with an img role must have an alternative text"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/svg-img-alt?application=axeAPI"
                   :nodes []}
                  {:id "tabindex"
                   :impact nil
                   :tags ["cat.keyboard" "best-practice"]
                   :description "Ensures tabindex attribute values are not greater than 0"
                   :help "Elements should not have tabindex greater than zero"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/tabindex?application=axeAPI"
                   :nodes []}
                  {:id "table-duplicate-name"
                   :impact nil
                   :tags ["cat.tables" "best-practice"]
                   :description "Ensure the <caption> element does not contain the same text as the summary attribute"
                   :help "tables should not have the same summary and caption"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/table-duplicate-name?application=axeAPI"
                   :nodes []}
                  {:id "td-headers-attr"
                   :impact nil
                   :tags ["cat.tables" "wcag2a" "wcag131" "section508" "section508.22.g"]
                   :description "Ensure that each cell in a table that uses the headers attribute refers only to other cells in that table"
                   :help "Table cells that use the headers attribute must only refer to cells in the same table"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/td-headers-attr?application=axeAPI"
                   :nodes []}
                  {:id "th-has-data-cells"
                   :impact nil
                   :tags ["cat.tables" "wcag2a" "wcag131" "section508" "section508.22.g" "TTv5" "14.b"]
                   :description "Ensure that <th> elements and elements with role=columnheader/rowheader have data cells they describe"
                   :help "Table headers in a data table must refer to data cells"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/th-has-data-cells?application=axeAPI"
                   :nodes []}
                  {:id "valid-lang"
                   :impact nil
                   :tags ["cat.language" "wcag2aa" "wcag312" "ACT" "TTv5" "TT11.b"]
                   :description "Ensures lang attributes have valid values"
                   :help "lang attribute must have a valid value"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/valid-lang?application=axeAPI"
                   :nodes []}
                  {:id "video-caption"
                   :impact nil
                   :tags ["cat.text-alternatives" "wcag2a" "wcag122" "section508" "section508.22.a" "TTv5" "TT17.a"]
                   :description "Ensures <video> elements have captions"
                   :help "<video> elements must have captions"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/video-caption?application=axeAPI"
                   :nodes []}
                  {:id "no-autoplay-audio"
                   :impact nil
                   :tags ["cat.time-and-media" "wcag2a" "wcag142" "ACT" "TTv5" "TT2.a"]
                   :description "Ensures <video> or <audio> elements do not autoplay audio for more than 3 seconds without a control mechanism to stop or mute the audio"
                   :help "<video> or <audio> elements must not play automatically"
                   :helpUrl "https://dequeuniversity.com/rules/axe/4.7/no-autoplay-audio?application=axeAPI"
                   :nodes []}]
   :timestamp "2023-05-09T21:41:15.184Z"
   :violations []})
