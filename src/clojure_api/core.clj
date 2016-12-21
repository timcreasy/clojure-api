(ns clojure-api.core
  (:require [datomic.api :as d]
            [compojure.core :refer [GET POST defroutes]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]))

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

(defn print-users [users]
  (str "<p><a href=\"user\\add\">Add new user</a></p>"
       "<ul>"
       (apply str (map (fn [user]
                         (str "<li>" (nth user 0) " - " (nth user 1) "</li>"))
                       (get-all-users (get-database))))
       "</ul>"))

(defn add-user-to-db-from-request [name email]
  (add-user name email)
  (str "<p>User added:" name " - " email "</p>" "<p><a href=\"http://localhost:8080\\users\">View all users</a></p>"))

(defroutes app-routes
  (GET "/" [] "User database")
  (GET "/users" [] (print-users
                     (get-all-users (get-database))))
  (POST "/user/add" [name email] (add-user-to-db-from-request name email))
  (GET  "/user/add" [] "<form method='post' action='/user/add'> Name: <input type='text' name='name' /><br> Email:<input type='email' name='email' /><br><input type='submit' /></form>"))

(defn -main []
  (run-jetty (wrap-params app-routes) {:port 8080}))