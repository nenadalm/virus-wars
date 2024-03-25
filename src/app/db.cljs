(ns app.db
  (:refer-clojure :exclude [take])
  (:require
   [clojure.set :as set]))

(defn- valid-point? [game x y]
  (let [max-index (dec (:size game))]
    (and (<= 0 x max-index)
         (<= 0 y max-index))))

(defn- all-points [game]
  (let [size (:size game)]
    (vec (for [x (range size)
               y (range size)]
           [x y]))))

(defn- opponent [player]
  ({:p1 :p2 :p2 :p1} player))

(defn- neighbour-points [game x y]
  (set
   (filterv
    (fn [[x y]] (valid-point? game x y))
    [[x (inc y)]
     [x (dec y)]
     [(inc x) y]
     [(dec x) y]
     [(inc x) (inc y)]
     [(inc x) (dec y)]
     [(dec x) (dec y)]
     [(dec x) (inc y)]])))

(defn- living? [game x y]
  (let [active-player (:active-player game)
        cell (get-in game [:board x y])]
    (and (:live cell)
         (= active-player (:player cell)))))

(defn- killed? [game x y]
  (let [active-player (:active-player game)
        cell (get-in game [:board x y])]
    (and cell
         (not (:live cell))
         (not= active-player (:player cell)))))

(defn- self-adjacent? [game x y]
  (loop [neighbours (neighbour-points game x y)
         visited #{}]
    (cond
      (empty? neighbours) false
      (some (fn [[x y]] (living? game x y)) neighbours) true
      :else (recur
             (set/difference
              (into
               #{}
               (comp
                (filter (fn [[x y]] (killed? game x y)))
                (mapcat (fn [[x y]]
                          (neighbour-points game x y))))
               neighbours)
              visited)
             (set/union visited neighbours)))))

(defn- interaction-type [game x y]
  (let [active-player (:active-player game)
        cell (get-in game [:board x y])]
    (when (self-adjacent? game x y)
      (if cell
        (when (and (:live cell)
                   (not= (:player cell) active-player))
          :kill)
        :take))))

(defn- update-moves [game]
  (let [moves (:moves game)]
    (if (== moves 1)
      (assoc game
             :moves 3
             :active-player (opponent (:active-player game)))
      (update game :moves dec))))

(defn- kill [game x y]
  (-> game
      (assoc-in [:board x y :live] false)
      update-moves))

(defn- take [game x y]
  (-> game
      (assoc-in [:board x y] {:player (:active-player game)
                              :live true})
      update-moves))

(defn play [game x y]
  (case (interaction-type game x y)
    :take (take game x y)
    :kill (kill game x y)
    game))

(defn winner [game]
  (when-not (boolean
             (some
              (fn [[x y]]
                (interaction-type game x y))
              (all-points game)))
    (opponent (:active-player game))))

(defn init []
  (let [size 8]
    {:board (-> (vec (repeat size (vec (repeat size nil))))
                (assoc-in [0 0] {:player :p1 :live true})
                (assoc-in [(dec size) (dec size)] {:player :p2 :live true}))
     :size 8
     :active-player (first (shuffle [:p1 :p2]))
     :moves 1}))
