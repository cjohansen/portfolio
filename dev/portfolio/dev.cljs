(ns ^:figwheel-hooks portfolio.dev
  (:require [dumdom.component]
            [gadget.inspector :as inspector]
            [portfolio.dumdom :as portfolio]
            [portfolio.ui :as ui]
            [portfolio.data :as data]
            [portfolio.scenes :as scenes]
            [axe :as axe]
            [react :as react]))

::scenes/keep

(comment

  (tap> [:h1 "Holy smokes!"])

  (tap> {:title "Heading"
         :component [:h1 "Yo!"]})

  (tap> "<div><h1>Yowsa!</h1><p>Pretty cool</p>")

  (tap> {:title "HTML-string"
         :component "<h1>Yowsa!</h1>"})

  (tap> (let [el (js/document.createElement "h1")]
          (set! (.-innerHTML el) "I am DOM")
          el))

  (require '["react" :as react])

  (react/isValidElement (helix.dom/p "Hello"))
  (react/isValidElement (fn []))



  (portfolio/render-scene [:h1 "Holy smokes!"])

  (.portfolioOnRender (.-contentWindow (js/document.querySelector "iframe")))

  (-> (axe.run (js/document.querySelector "iframe"))
      (.then (fn [results]
               (js/console.log results)
               ;;(prn (js->clj results :keywordize-keys true))
               )))


  ;; axe
  ;; .run()
  ;; .then(results => {
  ;;                   if (results.violations.length) {
  ;;                                                   throw new Error('Accessibility issues found');
  ;;                                                   }
  ;;                   })
  ;; .catch(err => {
  ;;                console.error('Something bad happened:', err.message);
  ;;                });

  (tap> {:title "h1 test #3"
         :component [:h1 "Hello!?"]})

  (tap> [:button "OK"])

  (portfolio.data/register-collection!
   :portfolio.repl
   {:kind :folder
    :collection nil
    :title "REPL"})

  (portfolio.ui.collection/get-default-organization
   (vals @portfolio.data/scenes)
   (vals @portfolio.data/collections))

  (portfolio.data/register-scene!
   {:id :portfolio.repl/scene-1682312756260
    :idx (portfolio.data/get-next-idx "portfolio.repl")
    :component [:h1 "I am from ze REPL"]
    :collection :portfolio.repl})

  (portfolio.data/get-next-idx "portfolio.repl")

  (keys @portfolio.data/scenes)
  (keys @portfolio.data/collections)

  (tap> {:text "Ooh, mama"
         :number 42
         :boolean true})
  (tap> {:text "Hey there!"})
  (tap> {:text "Oh, boy"
         :items [{:text "Lol"}]})

  (tap> {:text "Oh boi"
         :request-log
         [{:method :post
           :url "https://security.test/api/Token"}
          {:status 200
           :body {:access_token "lol"
                  :expires_in 60000}}

          {:method :get
           :url "https://crm.test/api/DeliverySite/707057500066666666"}
          {:status 200
           :body {:Address
                  {:StreetName "Kreklingen"
                   :Number "5"
                   :Letter ""
                   :City "Sofiemyr"
                   :Zip "1412"
                   :CountryCode "NO"}}}

          {:method :get
           :url "https://google-maps/maps/api/geocode/json"
           :query-params {:address "Kreklingen 5 1412 Sofiemyr Norway"
                          :key "google-maps"}
           :as :json
           :throw-exceptions false}
          {:status 200
           :body {:status "OK"
                  :results
                  [{:geometry
                    {:location {:lat 59.787733 :lng 10.812987}}
                    :types ["premise"]}]}}

          {:method :post
           :url "https://enode.api/charging-locations"
           :oauth-token "enode.system.token"
           :form-params {:name "At mah house, yo"
                         :longitude 10.812987
                         :latitude 59.787733}}
          {:status 200
           :body {:id "634424b2-ebe3-4910-826f-e996496ecac9"
                  :name "At mah house, yo"
                  :longitude 10.812987
                  :latitude 59.787733}}]})

  )

(set! dumdom.component/*render-eagerly?* true)

(inspector/inspect "Application data" ui/app)

(comment

  (data/register-collection!
   {:id :elements
    :title "Elements"})

  (inspector/inspect "Application data" ui/app)

  (ui/start!)



  )
