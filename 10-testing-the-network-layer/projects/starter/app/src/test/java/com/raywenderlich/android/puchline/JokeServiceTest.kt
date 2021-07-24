package com.raywenderlich.android.puchline

import com.github.javafaker.Faker
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.raywenderlich.android.punchline.Joke
import com.raywenderlich.android.punchline.JokeService
import com.raywenderlich.android.punchline.RepositoryImpl
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val id = "6"
private const val joke =
    "How does a train eat? It goes chew, chew"


class JokeServiceTestUsingMockWebServer {

    private val testJson = """{ "id": $id, "joke": "$joke" }"""

    @get:Rule
    val mockWebServer = MockWebServer()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val jokeService by lazy {
        retrofit.create(JokeService::class.java)
    }

    @Test
    fun getRandomJokeEmitsJoke() {
        mockWebServer.enqueue(
            MockResponse()
                .setBody(testJson)
                .setResponseCode(200))
        val testObserver = jokeService.getRandomJoke().test()
        testObserver.assertValue(Joke(id, joke))
    }

    @Test
    fun getRandomJokeGetsRandomJokeJson() {
        // 1
        mockWebServer.enqueue(
            MockResponse()
                .setBody(testJson)
                .setResponseCode(200))
        // 2
        val testObserver = jokeService.getRandomJoke().test()
        // 3
        testObserver.assertNoErrors()
        // 4
        assertEquals("/random_joke.json",
            mockWebServer.takeRequest().path)
    }
}

class JokeServiceTestMockingService {

    private val jokeService: JokeService = mock()
    private val repository = RepositoryImpl(jokeService)

    @Test
    fun getRandomJokeEmitsJoke() {
        val joke = Joke(id, joke)
        whenever(jokeService.getRandomJoke())
            .thenReturn(Single.just(joke))
        val testObserver = repository.getJoke().test()
        testObserver.assertValue(joke)
    }

}

class JokeServiceTestUsingFaker {
    var faker = Faker()
    private val jokeService: JokeService = mock()
    private val repository = RepositoryImpl(jokeService)

    @Test
    fun getRandomJokeEmitsJoke() {
        val joke = Joke(
            faker.idNumber().valid(),
            faker.lorem().sentence())

        whenever(jokeService.getRandomJoke())
            .thenReturn(Single.just(joke))
        val testObserver = repository.getJoke().test()

        testObserver.assertValue(joke)
    }

}


