package org.example.plugins

import io.ktor.server.application.*

fun Application.configureAuthentication() {
    // Authentication is now handled directly in the routes
    // No need to install Ktor's Authentication plugin for this simple API key validation
}
