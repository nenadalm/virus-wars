(ns app.views
  (:require
   [re-frame.core :as re-frame]
   [app.events :as events]
   [app.subs :as subs]))

(defn- player-symbol [player]
  (if (= player :p1)
    [:svg
     {:viewBox "0 0 50 50"}
     [:line.player-p1
      {:x1 0 :x2 50 :y1 0 :y2 50 :stroke-width "3px"}]
     [:line.player-p1
      {:x1 0 :x2 50 :y1 50 :y2 0 :stroke-width "3px"}]]
    [:svg
     {:viewBox "0 0 50 50"}
     [:circle.player-p2
      {:cx 25 :cy 25 :r 22 :stroke-width "3px"}]]))

(defn- cell [data x y]
  [:div.cell
   [:div.cell-content
    [:button.cell-btn
     {:on-click #(re-frame/dispatch [::events/play x y])
      :class (when data
               [(str "player-" (name (:player data)))
                (if (:live data) "live" "dead")])}
     (when data
       [player-symbol (:player data)])]]])

(defn- game-row [row row-index]
  [:div.row
   (for [col-index (range (count row))]
     ^{:key col-index} [cell (get row col-index) row-index col-index])])

(defn- status-bar []
  (let [{:keys [active-player moves]} @(re-frame/subscribe [::subs/status-bar])
        winner @(re-frame/subscribe [::subs/winner])
        app-info @(re-frame/subscribe [::subs/app-info])]
    [:div.status-bar
     (if winner
       [:<>
        "Winner: " [player-symbol winner]
        [:br]
        [:button
         {:on-click #(re-frame/dispatch [::events/reset])}
         "Reset"]]
       [:<>
        [:div
         "Active player: " [player-symbol active-player]]
        [:div
         "Moves: " moves]])
     [:div.rules-link
      [:a
       {:href "http://www.papg.com/show?5SB0"}
       "Rules"]]
     [:div.app-version
      (str "Version: " (:version app-info))]]))

(defn- game []
  (let [board @(re-frame/subscribe [::subs/board])]
    [:div.game
     [:div.board-container
      [:div.board
       (for [row-index (range (count board))]
         ^{:key row-index} [game-row (get board row-index) row-index])]]
     [status-bar]]))

(defn app []
  [game])
