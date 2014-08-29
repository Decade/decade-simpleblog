(defproject site "0.0.1"
  :description "A small website"
  :url "https://theodr.net."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [postgresql "9.1-901.jdbc4"]
                 [compojure "1.1.8"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-devel "1.3.1"]
                 [http-kit "2.1.11"]]
  :main ^:skip-aot site.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all} :dev {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]]}})
