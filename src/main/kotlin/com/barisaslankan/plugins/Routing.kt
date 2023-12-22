package com.barisaslankan.plugins

import com.barisaslankan.routes.getAllHeroes
import com.barisaslankan.routes.root
import com.barisaslankan.routes.searchHeroes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting(){
    routing {
        root()
        getAllHeroes()
        searchHeroes()

        staticResources(remotePath = "/images", basePackage = "images")
    }
}