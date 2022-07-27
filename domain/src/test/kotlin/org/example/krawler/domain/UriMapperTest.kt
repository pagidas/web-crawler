package org.example.krawler.domain

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UriMapperTest {

    private val mapper = UriMapper(URI("https://test.com/"), listOf("http", "https"), listOf(".pdf"))

    @ParameterizedTest
    @MethodSource("provideTestData")
    fun `transforms relative to absolute correctly`(givenVisitingLink: URI, givenRawLink: String, expectedUri: URI?) {
        val result = mapper.mapToAbsolute(givenVisitingLink, givenRawLink)
        Assertions.assertEquals(expectedUri, result)
    }

    private fun provideTestData(): Stream<Arguments> =
        Stream.of(
            Arguments.of(URI("https://test.com"), "/path-zero", URI("https://test.com/path-zero/")),
            Arguments.of(URI("https://test.com"), "/path-a-dir/", URI("https://test.com/path-a-dir/")),
            Arguments.of(URI("https://test.com/path-a-dir/"), "apple", URI("https://test.com/path-a-dir/apple/")),
            Arguments.of(URI("https://test.com/path-a-dir/"), "orange", URI("https://test.com/path-a-dir/orange/")),
            Arguments.of(URI("https://test.com/path-a-dir/"), "/path-b-dir/", URI("https://test.com/path-b-dir/")),
            Arguments.of(URI("https://test.com/path-b-dir/"), "banana", URI("https://test.com/path-b-dir/banana/")),
            Arguments.of(URI("https://test.com/path-b-dir/"), "peach", URI("https://test.com/path-b-dir/peach/")),
            Arguments.of(URI("https://test.com/path-b-dir/"), "../path-a-dir/", URI("https://test.com/path-a-dir/")),
            Arguments.of(URI("https://github.com/profile/"), "any", null),
            Arguments.of(URI("https://test.com/"), "demo.pdf", null),
            Arguments.of(URI("https://test.com/about/"), "../demo.png", URI("https://test.com/demo.png")),
            Arguments.of(URI("https://test.com/"), "#", null),
            Arguments.of(URI("https://test.com/#sectionA"), "#sectionA", URI("https://test.com/#sectionA")),
        )

}