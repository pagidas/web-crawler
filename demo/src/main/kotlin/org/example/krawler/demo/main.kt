package org.example.krawler.demo

import org.example.krawler.domain.ScrappingConfig
import org.example.krawler.domain.WebCrawler
import java.net.URI

fun main() {
    val config = ScrappingConfig(
        baseUri = URI("https://monzo.com/"),
        validSchemes = listOf("http", "https"),
        ignoredExtensions = listOf(".pdf", ".mp3", "mp4", ".m4a", ".jpg", ".png", ".gif"),
        maxConcurrentRequests = 2,
    )
    val webCrawler = WebCrawler(config)
    webCrawler()
}

