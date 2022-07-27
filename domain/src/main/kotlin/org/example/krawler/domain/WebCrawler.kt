package org.example.krawler.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import mu.KotlinLogging
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.jsoup.Jsoup
import java.net.URI
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


@OptIn(
    ExperimentalCoroutinesApi::class,
    DelicateCoroutinesApi::class,
    ExperimentalTime::class
)
class WebCrawler(config: ScrappingConfig) {

    private val logger = KotlinLogging.logger {}

    private val mapper = UriMapper(config.baseUri, config.validSchemes, config.ignoredExtensions)
    private val http = JettyClient()
    private val baseUri = config.baseUri

    private val visitedLinks = mutableSetOf<URI>()
    private val channel = Channel<URI>()

    private val crawlContext = Dispatchers.IO.limitedParallelism(config.maxConcurrentRequests)
    private val logicContext = newSingleThreadContext("LogicContext")
    private val crawlingJobs = mutableListOf<Deferred<Unit>>()

    operator fun invoke() = runBlocking() {
        val time = measureTime { crawl() }
        logger.info { "Time taken: $time" }
    }

    private suspend fun crawl() = coroutineScope {
        launch(logicContext) { channel.send(baseUri) }
        launch(logicContext) { crawlAsync() }
    }

    private suspend fun crawlAsync() = coroutineScope {
        channel.consumeEach { link ->
            if (!visitedLinks.contains(link)) {
                visitedLinks.add(link)
                crawlingJobs.add(
                    async(crawlContext) { crawlNext(link) }
                )
            } else launch(logicContext) {
                closeChannelIfFinished()
            }
        }
        logger.info { "Finished!" }
        visitedLinks.sorted().forEach { logger.info { it } }
        logger.info { "Visited ${visitedLinks.size} links -- above are listed all the links visited" }
    }

    private suspend fun crawlNext(next: URI) = coroutineScope {
        logger.info { "Visiting $next" }
        val response = http(Request(Method.GET, next.toString()))
        if (response.status.successful) {
            val newLinks = Jsoup.parse(response.body.stream, "UTF-8", next.toString())
                .getElementsByTag("a")
                .mapNotNull { a -> mapper.mapToAbsolute(next, a.attr("href").trim()) }
                .toSet()

            if (newLinks.isEmpty()) launch(logicContext) {
                closeChannelIfFinished()
            } else {
                logger.info { "Found links: $newLinks" }
                newLinks.forEach { channel.send(it) }
            }
        }
    }

    private suspend fun closeChannelIfFinished() = coroutineScope() {
        if (!crawlingJobs.any(Job::isActive)) channel.close()
    }
}

data class ScrappingConfig(
    val baseUri: URI,
    val validSchemes: List<String>,
    val ignoredExtensions: List<String>,
    val maxConcurrentRequests: Int
)

