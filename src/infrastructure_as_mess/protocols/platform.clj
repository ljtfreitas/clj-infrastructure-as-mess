(ns infrastructure-as-mess.protocols.platform)

(defprotocol Platform
  (deploy-all! [_ resources]))
