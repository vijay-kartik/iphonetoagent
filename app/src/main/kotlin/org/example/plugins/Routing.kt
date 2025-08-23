package org.example.plugins

import io.ktor.server.application.*
import org.example.routes.ApiRoutes

fun Application.configureRouting() {
    val apiRoutes = ApiRoutes()
    apiRoutes.configureRoutes(this)
}
