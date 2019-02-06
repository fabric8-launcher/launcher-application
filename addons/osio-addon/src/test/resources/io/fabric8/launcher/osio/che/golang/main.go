package main

import (
	"fmt"
	"log"
	"net/http"
)

var isOnline = true

func main() {
	http.HandleFunc("/api/greeting", greeting)
	http.HandleFunc("/api/stop", stop)
	http.HandleFunc("/api/health", health)
	fmt.Println("Web server running on port 8080")

	log.Fatal(http.ListenAndServe(":8080", nil))
}

func greeting(w http.ResponseWriter, r *http.Request) {
	if isOnline {
		message := "World"
		if m := r.FormValue("name"); m != "" {
			message = m
		}
		fmt.Fprintf(w, "Hello %s!", message)
		return
	}
	w.WriteHeader(503)
	w.Write([]byte("Not Online"))
}

func stop(w http.ResponseWriter, r *http.Request) {
	isOnline = false
	w.Write([]byte("Stopping HTTP Server"))
}

func health(w http.ResponseWriter, r *http.Request) {
	if isOnline {
		w.WriteHeader(200)
		return
	}
	w.WriteHeader(500)
}
