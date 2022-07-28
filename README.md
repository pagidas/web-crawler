# web-crawler
This exercise is about building a web crawler that given a
starting url, it will find all links in the same domain, 
visit them, and lastly print them. We need to build the 
web crawler without using any library that does url validation, 
and concurrency look up. We can only use DOM parsing library.
The web crawler is limited to one subdomain. For example,
if base url is `https://github.com/` the web crawler should not
follow external links, e.g. `facebook.com` or `community.github.com`.

## Structure
The project is structured in multiple modules.
- `domain` - has all the logic regarding web crawling.
- `cli-adapter` - adapts the api into a cli application.

## How to use
At the root of the project we should simply run:

`./gradlew clean distZip`

And at `cli-adapter/build/distributions/` there will be a
`krawler.zip` compressed file, which we need to unzip.

In the `bin/` folder of the unzipped file, there will be
the executable which we can just run:

`./krawler` without any arguments, displaying the `--help`
section of how to use.

### Requirements

Since we used `gradle wrapper` we do not need to have `gradle`
installed in our local machine. However, the wrapper depends on
the `jdk`.

## Experimentation
The current implementation includes Kotlin's [coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
to deal with concurrency. We used the [channel](https://kotlinlang.org/docs/channels.html)
like a blocking queue to process next links to follow up on.

All `http` requests for next links to follow are executed asynchronously
using the `IO` thread dispatcher since it allows more parallel tasks executed.

As for checking shared mutable state, we approached it by using a
single-threaded dispatcher to synchronise those calls. We could have
used thread-safe collections, but Kotlin's proposed way is to do
the above.

