package io.ztechnologies.buscacep

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform