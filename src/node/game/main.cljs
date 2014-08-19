(ns game.main
  (:require-macros [game.macros :refer [ability]])
  (:require [cljs.nodejs :as node]
            [game.core :refer [game-states do! system-msg pay gain draw play move-card] :as core]))

(aset js/exports "main" game.main)
(enable-console-print!)
(defn noop [])
(set! *main-cli-fn* noop)

(def commands
  {"say" core/say
   "mulligan" core/mulligan
   "keep" core/keep-hand
   "draw" (do! (ability  {:cost [:click 1]
                          :effect [(draw) (system-msg "draw 1 card.")]}))
   "credit" (do! (ability {:cost [:click 1]
                           :effect [(gain :credit 1) (system-msg "take 1 credit.")]}))
   "purge" (do! (ability {:cost [:click 3]
                          :effect [(core/purge) (system-msg "purges viruses.")]}))
   "remove-tag" (do! (ability {:cost [:click 1 :credit 2 :tag 1]
                               :effect [(system-msg "removes 1 tag.")]}))
   "play" (fn [state side {:keys [card]}]
            (when (pay state side :click 1 :credit (:cost card))
              (play state side card)
              (system-msg state side (str "plays " (:title card) "."))))})

(defn exec [action args]
  (let [params (js->clj args :keywordize-keys true)
        gameid (:gameid params)
        state (@game-states (:gameid params))]
    (case action
      "init" (core/init-game params)
      "do" ((commands (:command params)) state (keyword (:side params)) (:args params)))
    (clj->js @(@game-states gameid))))