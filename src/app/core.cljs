(ns app.core
  (:require
   [re-frame.core :as re-frame]
   [reagent.dom :as reagent-dom]
   [app.config :as config]
   [app.views :as views]
   [app.events :as events]
   [nenadalm.clojure-utils.re-frame.autosave :as autosave]
   [nenadalm.clojure-utils.cljs :as cljs-utils]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent-dom/render [views/app]
                      (.getElementById js/document "app")))

(defn register-worker []
  (some-> js/navigator
          .-serviceWorker
          (.register "worker.js")
          (.then
           (fn [registration]
             (if (and (.-waiting registration)
                      js/navigator.serviceWorker.controller)
               (re-frame/dispatch [::events/update-available])
               (.addEventListener
                registration
                "updatefound"
                (fn []
                  (when-let [installing (.-installing registration)]
                    (.addEventListener
                     installing
                     "statechange"
                     (fn []
                       (when (and (.-waiting registration)
                                  js/navigator.serviceWorker.controller)
                         (re-frame/dispatch [::events/update-available]))))))))))))

(defn- dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn- prod-setup []
  (when-not config/debug?
    (register-worker)))

(defn ^:export init []
  (dev-setup)
  (prod-setup)
  (cljs-utils/prevent-screen-lock)
  (autosave/init "nenadalm.virus-wars/autosave")
  (re-frame/dispatch-sync [::events/init])
  (mount-root))

(defn ^:dev/after-load after-load []
  (mount-root))
