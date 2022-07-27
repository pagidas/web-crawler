package org.example.krawler.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import org.example.krawler.domain.ScrappingConfig
import org.example.krawler.domain.WebCrawler
import java.net.URI
import java.net.URL

class Krawler: CliktCommand(help = """
    Given BASEURI, this script crawls into the website and visits each 
    url it finds on the same domain, printing them.
    
    BASEURI must be a valid absolute url.
""".trimIndent()) {

    init {
        versionOption("0.0.1")
    }

    private val baseUri: String by argument(help = "The base uri to crawl to.")
        .validate { rawUri ->
            try { URL(rawUri) } catch (e: Exception) {
                fail("Base uri must be a valid absolute url -- error: '${e.message}'")
            }
        }

    private val ignoredExtensions: List<String> by option(help = "List of ignored extensions to crawl to.")
        .split(",")
        .default(listOf(".pdf", ".mp3", ".mp4", ".m4a", ".jpg", ".png", ".gif"))
        .validate {
            require(it.all { ext -> ext.contains(".") }) {
                "Extension should be of this form: e.g. '.pdf'"
            }
        }

    private val validSchemes: List<String> by option(help = "List of valid schemes in uri protocol for crawling.")
        .split(",")
        .default(listOf("http", "https"))

    private val maxConcurrentRequests: Int by option(help = "Max concurrent requests for crawler.")
        .int()
        .default(4)

    override fun run() {
        val config = ScrappingConfig(URI(baseUri), validSchemes, ignoredExtensions, maxConcurrentRequests)
        println("== Initiating crawler with configuration ==\n${config.toStdOutFormat()}")
        val crawler = WebCrawler(config)
        crawler()
    }
}

private fun ScrappingConfig.toStdOutFormat(): String =
    """
        base_uri: $baseUri
        valid_schemes: $validSchemes
        ignored_extensions: $ignoredExtensions
        max_concurent_requests: $maxConcurrentRequests
    """.trimIndent()

fun main(args: Array<String>) = Krawler().main(args)

