(ns app.events
  (:require
   [re-frame.core :as re-frame]
   [nenadalm.clojure-utils.cljs :as cljs-utils]
   [app.db :as db]))

(re-frame/reg-cofx
 :app-version
 (fn [coeffects _]
   (assoc coeffects :app-version (cljs-utils/app-version))))

(defn- reset-game [db]
  (assoc db :game (db/init)))

(re-frame/reg-event-fx
 ::init
 [(re-frame/inject-cofx :app-version)]
 (fn [{:keys [app-version db]} _]
   (let [data {:app-info {:version app-version}}]
     {:db (if (seq db)
            (merge db data)
            (reset-game data))})))

(re-frame/reg-event-fx
 ::update-available
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:app-info :update-available] true)}))

(re-frame/reg-event-db
 ::play
 (fn [db [_ x y]]
   (update db :game db/play x y)))

(re-frame/reg-event-db
 ::reset
 (fn [db _]
   (reset-game db)))
