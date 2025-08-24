package org.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.event.Level
import org.slf4j.LoggerFactory

fun Application.configureLogging() {
    install(CallLogging) {
        logger = LoggerFactory.getLogger("ktor.application")
        level = Level.INFO
    }
}
