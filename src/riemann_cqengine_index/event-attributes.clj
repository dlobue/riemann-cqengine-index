(ns riemann-cqengine-index.event-attributes
  (import com.googlecode.cqengine.attribute.MultiValueAttribute
          com.googlecode.cqengine.attribute.SimpleAttribute))


(def SERVICE
  (proxy [SimpleAttribute] [riemann.codec.Event String]
    (getValue ^String [event] (:service event))))

(def HOST
  (proxy [SimpleAttribute] [riemann.codec.Event String]
    (getValue ^String [event] (:host event))))

(def STATE
  (proxy [SimpleAttribute] [riemann.codec.Event String]
    (getValue ^String [event] (:state event))))

(def DESCRIPTION
  (proxy [SimpleAttribute] [riemann.codec.Event String]
    (getValue ^String [event] (:description event))))

(def ID
  (proxy [SimpleAttribute] [riemann.codec.Event List]
    (getValue ^List [event] [(:host event) (:service event)])))

(def TIME
  (proxy [SimpleAttribute] [riemann.codec.Event Long]
    (getValue ^Long [event] (:time event))))

(def TTL
  (proxy [SimpleAttribute] [riemann.codec.Event Float]
    (getValue ^Float [event] (:ttl event))))

(def EXPIRES_AT
  (proxy [SimpleAttribute] [riemann.codec.Event Float]
    (getValue ^Float [event] (+ (:ttl event) (:time event))))

(def TAGS
  (proxy [MultiValueAttribute] [riemann.codec.Event String]
    (getValue ^String [event] (:tags event))))

