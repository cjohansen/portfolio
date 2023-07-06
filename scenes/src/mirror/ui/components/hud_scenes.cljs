(ns mirror.ui.components.hud-scenes
  (:require [portfolio.dumdom :as portfolio :refer-macros [defscene]]
            [portfolio.ui.components.hud :as hud]
            [portfolio.ui.components.error :refer [Error]]
            [phosphor.icons :as icons]))

(portfolio/configure-scenes
 {:title "Heads-up Display"})

(defscene basic-hud
  (hud/render-hud
   {:action {:icon (icons/icon :phosphor.bold/x)
             :actions []
             :title "Close"}}))

(defscene hud-with-error
  (hud/render-hud
   {:action {:icon (icons/icon :phosphor.bold/x)
             :actions []
             :title "Close"}}
   (Error
    {:title "Something went really wrong"
     :message "Your thingamajig tripped over the whatchamacallit and now everything is sad."
     :stack "Error: Oh no!
    at new cljs$core$ExceptionInfo (http://localhost:5995/portfolio/js/dev/cljs/core.js:38048:10)
    at Function.cljs$core$IFn$_invoke$arity$3 (http://localhost:5995/portfolio/js/dev/cljs/core.js:38109:9)
    at Function.cljs$core$IFn$_invoke$arity$2 (http://localhost:5995/portfolio/js/dev/cljs/core.js:38105:26)
    at cljs$core$ex_info (http://localhost:5995/portfolio/js/dev/cljs/core.js:38091:26)
    at Function.<anonymous> (http://localhost:5995/portfolio/js/dev/portfolio/components/button.js:10:25)
    at Function.cljs$core$IFn$_invoke$arity$3 (http://localhost:5995/portfolio/js/dev/cljs/core.js:13585:10)
    at Function.cljs$core$IFn$_invoke$arity$3 (http://localhost:5995/portfolio/js/dev/cljs/core.js:13873:34)
    at cljs$core$apply (http://localhost:5995/portfolio/js/dev/cljs/core.js:13815:24)
    at http://localhost:5995/portfolio/js/dev/dumdom/component.js:357:98
    at http://localhost:5995/portfolio/js/dev/dumdom/component.js:364:3"})))
