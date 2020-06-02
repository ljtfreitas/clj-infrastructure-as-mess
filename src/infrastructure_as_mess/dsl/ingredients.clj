(ns infrastructure-as-mess.dsl.ingredients)

(defmacro blend [f & forms]
  `(apply ~f [~@forms]))

(defmacro ingredients [& forms]
  `(flatten [~@forms]))