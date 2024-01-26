(ns ^:figwheel-hooks portfolio.dev
  (:require [dumdom.component]
            [gadget.inspector :as inspector]
            [portfolio.data :as data]
            [portfolio.scenes :as scenes]
            [portfolio.ui :as ui]))

::scenes/keep

(set! dumdom.component/*render-eagerly?* true)

(comment
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

(inspector/inspect "Application data" ui/app)

(comment

  (data/register-collection!
   {:id :elements
    :title "Elements"})

  (inspector/inspect "Application data" ui/app)

  (ui/start!)

)
