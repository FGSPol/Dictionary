package fr.isep.dictionary.util

import fr.isep.dictionary.models.Definition
import fr.isep.dictionary.models.theWord
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {

    @GET("en/{word}")
    suspend fun getMeaning(@Path("word") word: String): Response<List<theWord>>

    @GET("en/{word}")
    suspend fun getDefinitions(@Path("word") word: String): Response<List<Definition>>
}
