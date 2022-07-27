package org.example.krawler.domain

import java.net.URI

class UriMapper(
    private val baseUri: URI,
    private val validSchemes: List<String>,
    private val ignoredExtensions: List<String>
) {

    fun mapToAbsolute(visitingLink: URI, rawLink: String): URI? {
        return if (visitingLink.authority != baseUri.authority) null
        else if (isValid(rawLink)) {
            val uri = URI(rawLink).let {
                val hasFragment = it.fragment?.isNotBlank() == true
                val endsWithSlash = it.path.endsWith("/")
                val hasExt = it.path.split("/").last().split(".").size == 2
                val hasQuery = it.query?.isNotBlank() == true
                if (!hasFragment && !endsWithSlash && !hasExt && !hasQuery) URI("$rawLink/") else it
            }
            if (!uri.isAbsolute) visitingLink.resolve(uri) else uri
        } else null
    }

    private fun isValid(rawUri: String): Boolean =
        try {
            URI(rawUri)
        } catch (e: Throwable) {
            null
        }?.let { uri ->
            if (uri.isAbsolute) isValidAbsolute(uri) else isValidRelative(uri)
        } ?: false

    private fun isValidAbsolute(uri: URI): Boolean =
        uri.authority == baseUri.authority
                && validSchemes.contains(uri.scheme)
                && !uri.hasIgnoredExtension(ignoredExtensions)
                && uri.hasNonEmptyFragment()

    private fun isValidRelative(uri: URI): Boolean =
        !uri.hasIgnoredExtension(ignoredExtensions) && uri.hasNonEmptyFragment()

    private fun URI.hasIgnoredExtension(ignoredExtensions: List<String>): Boolean =
        ignoredExtensions.any { path.endsWith(it) }

    private fun URI.hasNonEmptyFragment(): Boolean = fragment?.isNotBlank() ?: true
}

