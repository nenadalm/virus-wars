(ns app.subs
  (:require
   [re-frame.core :as re-frame]
   [app.db :as db]))

(re-frame/reg-sub
 ::app-info
 (fn [db _]
   (:app-info db)))

(re-frame/reg-sub
 ::board
 (fn [db _]
   (get-in db [:game :board])))

(re-frame/reg-sub
 ::status-bar
 (fn [db _]
   (let [game (:game db)]
     {:active-player (:active-player game)
      :moves (:moves game)})))

(re-frame/reg-sub
 ::winner
 (fn [db _]
   (db/winner (:game db))))
