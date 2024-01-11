package fr.isep.dictionary.util

import fr.isep.dictionary.models.theWord
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {

    @GET("en/{word}")
    suspend fun getMeaning(@Path("word") word : String) : Response<List<theWord>>
}