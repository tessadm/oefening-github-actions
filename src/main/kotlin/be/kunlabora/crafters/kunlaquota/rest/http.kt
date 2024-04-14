package be.kunlabora.crafters.kunlaquota.rest

import org.springframework.web.servlet.function.RouterFunctionDsl
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.body
import java.net.URI
import java.util.*

val apiRoutes: RouterFunctionDsl.() -> Unit = {
    "/quote".nest {
        GET { ServerResponse.ok().body(quotes.findAll()) }
        POST { request ->
            val addQuote = request.body<AddQuote>()
            val quote: Quote = quotes.execute(addQuote)
            ServerResponse.created(quote.id.asUri(request)).build()
        }
    }
}

private fun <E> EntityId<E>.asUri(request: ServerRequest): URI =
    request.uriBuilder().path("/{id}").build(this.value)

data class AddQuote(val quote: String)

val quotes = Quotes()

typealias QuoteId = EntityId<Quote>
data class Quote(val id: QuoteId, val quotedString: String)

class Quotes {
    private val backingList: MutableList<Quote> = mutableListOf()

    fun execute(addQuote: AddQuote): Quote =
        Quote(QuoteId.new(), addQuote.quote)
            .also { store(it) }
    private fun store(quote: Quote) = backingList.add(quote)

    fun findAll() = backingList.toList()
}

@Suppress("unused")
class EntityId<E> private constructor(val value: String) {
    companion object {
        fun <E> new(): EntityId<E> = EntityId(UUID.randomUUID().toString())
    }
}