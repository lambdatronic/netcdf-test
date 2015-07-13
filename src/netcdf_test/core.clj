;; This code is translated from the NetCDF Java Tutorial here:
;; http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/tutorial/
;;
;; The netcdf-java API is here:
;; http://www.unidata.ucar.edu/software/thredds/v4.5/netcdf-java/javadoc/index.html

(ns netcdf-test.core
  (:gen-class)
  (:import [ucar.nc2 NetcdfFile Attribute Dimension Variable]))

(defn process-attributes [attribute-list]
  (into {}
        (for [[^String name ^Attribute attribute] (Attribute/makeMap attribute-list)]
          [(keyword name) (.getStringValue attribute)])))

(defn process-dimensions [dimension-list]
  (into {}
        (for [^Dimension dim dimension-list]
          [(keyword (.makeFullName dim)) (.getLength dim)])))

(defn process-variables [variable-list]
  (doall
   (for [^Variable v variable-list]
     {:name        (.getNameAndDimensions v)
      :description (.getDescription v)
      :data-type   (str (.getDataType v))
      :dimensions  (process-dimensions (.getDimensions v))
      :attributes  (process-attributes (.getAttributes v))})))

(defn get-ncfile-info [filename]
  (with-open [^NetcdfFile ncfile (NetcdfFile/open filename)]
    {:file-type  (.getFileTypeDescription ncfile)
     :attributes (process-attributes (.getGlobalAttributes ncfile))
     :dimensions (process-dimensions (.getDimensions ncfile))
     :variables  (process-variables (.getVariables ncfile))}))

(defn read-ncfile [filename varname range]
  (with-open [^NetcdfFile ncfile (NetcdfFile/open filename)]
    (if-let [^Variable v (.findVariable ncfile varname)]
      (let [data (if (nil? range)
                   (.read v)
                   (.read v ^String range))]
        {:shape (.getShape data)
         :data  (.get1DJavaArray data (.getClassType (.getDataType v)))}))))

(defn -main [& args]
  (println args))

;;=====================================================================
;; The following functions are specific to the WRF data I am analyzing.
;;=====================================================================

(defn extract-layer-by-timestep [filename varname timestep]
  (read-ncfile filename varname (str timestep ",:,:")))

(defn extract-all-timesteps-by-location [filename varname i j]
  (read-ncfile filename varname (str ":," i "," j)))

(defn get-timestamp [filename timestep]
  (apply concat
   (for [varname ["year" "month" "day" "hour" "minute"]]
     (:data (read-ncfile filename varname (str timestep))))))

;; timestamp of raster band 0
;; (get-timestamp "/storage/WRF/varbytime/ffwig.nc" 0)

;; first hour in WRF dataset
;; (do (def ffwig-0 (extract-layer-by-timestep "/storage/WRF/varbytime/ffwig.nc" "ffwig" 0)) nil)

;; all hours at point 100,100
;; (do (def ffwig-100-100 (extract-all-timesteps-by-location "/storage/WRF/varbytime/ffwig.nc" "ffwig" 100 100)) nil)

;; top 2.5% of hourly readings at point 100,100
;; (take (int (* (count (:data ffwig-100-100)) 0.025)) (sort > (:data ffwig-100-100)))
