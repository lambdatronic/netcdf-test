;; This code is translated from the NetCDF Java Tutorial here:
;; http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/tutorial/
;;
;; The netcdf-java API is here:
;; http://www.unidata.ucar.edu/software/thredds/v4.5/netcdf-java/javadoc/index.html

(ns netcdf-test.core
  (:gen-class)
  (:import [ucar.nc2 NetcdfFile Variable Attribute Dimension]))

(defn process-attributes [attribute-list]
  (into {} (map (fn [[name attribute]] [(keyword name) (.getStringValue ^Attribute attribute)])) (Attribute/makeMap attribute-list)))

(defn process-dimensions [dimension-list]
  (into {} (map (fn [^Dimension dim] (vector (keyword (.makeFullName dim)) (.getLength dim)))) dimension-list))

(defn get-ncfile-info [filename]
  (with-open [^NetcdfFile ncfile (NetcdfFile/open filename)]
    {:file-type  (.getFileTypeDescription ncfile)
     :attributes (process-attributes (.getGlobalAttributes ncfile))
     :dimensions (process-dimensions (.getDimensions ncfile))
     :variables  (seq (.getVariables ncfile))}))

(defn read-ncfile [filename varname range]
  (with-open [^NetcdfFile ncfile (NetcdfFile/open filename)]
    (if-let [^Variable v (.findVariable ncfile varname)]
      {:shape (seq (.getShape v))
       :data  (if (nil? range) (.read v) (.read v ^String range))})))

;; (def ffwig-1 (read-ncfile "/storage/WRF/varbytime/ffwig.nc" "ffwig" "0,:,:")) ;; first hour in WRF dataset
;; => {:shape (87673 534 486)
;;     :data  ucar.ma2.ArrayFloat$D3}
;; (.getSize ffwi-1)
;; (.getSizeBytes ffwi-1)
;; (.reduce ffwi-1)

;; (def ffwi-point-vals (read-ncfile "/data/WRF/varbytime/ffwi.nc" "ffwi" ":,100,100")) ;; all hours at point 100,100 (0-indexed)
;; (def ffwi-point-vals-data (.getStorage ffwi-2))
;; (take (int (* (count ffwi-point-vals-data) 0.025)) (sort > ffwi-point-vals-data)) ;; top 2.5% of hourly readings

(defn -main [& args]
  (println args))
