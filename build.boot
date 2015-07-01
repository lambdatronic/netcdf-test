(task-options!
 pom  {:project     'netcdf-test
       :version     "1.0.0"
       :description "Experimenting with the Java NetCDF library"}
 repl {:eval        '(set! *warn-on-reflection* true)
       :init-ns     'netcdf-test.core}
 aot  {:namespace   '#{netcdf-test.core}}
 jar  {:main        'netcdf-test.core})

(set-env!
 :source-paths   #{"src"}
 :resource-paths #{"resources"}
 :dependencies   '[[org.clojure/clojure "1.7.0"]
                   [edu.ucar/netcdf4    "4.5.5"]])

(deftask build
  "Build my project."
  []
  (comp (aot) (pom) (uber) (jar)))
