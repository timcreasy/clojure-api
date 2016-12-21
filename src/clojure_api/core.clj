(ns clojure-api.core
  (:require [datomic.api :as d]))

(def conn (d/connect "datomic:dev://127.0.0.1:4334/api"))

(defn get-database []
  (d/db conn))

(defn get-user-id [db user-email]
  (ffirst (d/q '[:find ?uid
                 :in $ ?email
                 :where [?uid :user/email ?email]]
               db
               user-email)))

(defn get-all-users [db]
  (d/q '[:find ?name ?email
         :where [?user :user/name ?name]
                [?user :user/email ?email]]
       db))

(defn remove-user [user-id]
  (d/transact conn [[:db.fn/retractEntity user-id]]))

(defn add-user [user-name user-email]
  (d/transact conn [{:db/id (d/tempid :db.part/user)
                     :user/name user-name
                     :user/email user-email}]))