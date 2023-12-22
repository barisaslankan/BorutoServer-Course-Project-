package com.barisaslankan

import com.barisaslankan.models.ApiResponse
import com.barisaslankan.repository.HeroRepositoryImpl
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlinx.serialization.json.Json
import com.barisaslankan.repository.NEXT_PAGE_KEY
import com.barisaslankan.repository.PREVIOUS_PAGE_KEY
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest

class ApplicationTest {

    @AfterTest
    fun tearDown(){
        stopKoin()
    }

    @Test
    fun `access root endpoint, assert correct information`() =
        testApplication {
            val response = client.get("/")
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status
                )
                assertEquals(
                    expected = "Welcome to Boruto API",
                    actual = response.bodyAsText()
                )
        }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query all pages, assert correct information`(){
        testApplication {
            val heroRepository = HeroRepositoryImpl()
            val pages = 1..5
            val heroes = listOf(
                heroRepository.page1,
                heroRepository.page2,
                heroRepository.page3,
                heroRepository.page4,
                heroRepository.page5
            )
            pages.forEach { page ->
                val response = client.get("boruto/heroes?page=$page")

                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status
                )
                val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
                val expected = ApiResponse(
                    success = true,
                    message = "ok",
                    prevPage = calculatePage(page = page)[PREVIOUS_PAGE_KEY],
                    nextPage = calculatePage(page = page)[NEXT_PAGE_KEY],
                    heroes = heroes[page - 1],
                    lastUpdated = actual.lastUpdated
                )

                assertEquals(
                    actual = actual,
                    expected = expected
                )
            }
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query invalid page number, assert error`() =
        testApplication {
            val response = client.get("/boruto/heroes?page=invalid")
            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = response.status
            )
            val expected = ApiResponse(
                success = false,
                message = "Only Numbers Allowed."
            )
            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
            assertEquals(
                expected = expected,
                actual = actual
            )
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query hero name, assert single hero result`() =
        testApplication {
            val response = client.get("/boruto/heroes/search?name=sas")
            assertEquals(expected = HttpStatusCode.OK, actual = response.status)
            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
                .heroes.size
            assertEquals(expected = 1, actual = actual)
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query hero name, assert multiple heroes result`() =
        testApplication {
            val response = client.get("/boruto/heroes/search?name=sa")
            assertEquals(expected = HttpStatusCode.OK, actual = response.status)
            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
                .heroes.size
            assertEquals(expected = 3, actual = actual)
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query an empty text, assert empty list as a result`() =
        testApplication {
            val response = client.get("/boruto/heroes/search?name=")
            assertEquals(expected = HttpStatusCode.OK, actual = response.status)
            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
                .heroes
            assertEquals(expected = emptyList(), actual = actual)
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query non existing hero, assert empty list as a result`() =
        testApplication {
            val response = client.get("/boruto/heroes/search?name=unknown")
            assertEquals(expected = HttpStatusCode.OK, actual = response.status)
            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
                .heroes
            assertEquals(expected = emptyList(), actual = actual)
        }

    private fun calculatePage(page: Int): Map<String, Int?> {
        var prevPage: Int? = page
        var nextPage: Int? = page
        if (page in 1..4) {
            nextPage = nextPage?.plus(1)
        }
        if (page in 2..5) {
            prevPage = prevPage?.minus(1)
        }
        if (page == 1) {
            prevPage = null
        }
        if (page == 5) {
            nextPage = null
        }
        return mapOf(PREVIOUS_PAGE_KEY to prevPage, NEXT_PAGE_KEY to nextPage)
    }
}