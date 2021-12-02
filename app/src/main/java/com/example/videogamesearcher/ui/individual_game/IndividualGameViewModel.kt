package com.example.videogamesearcher.ui.individual_game

import androidx.lifecycle.*
import com.example.videogamesearcher.models.TwitchAuthorization
import com.example.videogamesearcher.models.individual_game.IndividualGameData
import com.example.videogamesearcher.repository.IndividualGameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

class IndividualGameViewModel : ViewModel() {

    private var twitchAuthorization: MutableLiveData<TwitchAuthorization> = MutableLiveData()
    var gameId: MutableLiveData<Int> = MutableLiveData()

    private val individualGameRepo = IndividualGameRepository()

    fun getAccessToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = individualGameRepo.getAccessToken()
            if (response.isSuccessful) {
                twitchAuthorization.postValue(response.body())
            } else {
                //Change how this is handled in the future.
                println("Twitch auth token response not successful.")
            }
        }
    }


    private fun getIndividualGameData(): LiveData<IndividualGameData?>
        = twitchAuthorization.switchMap { twitchAuth ->
            gameId.switchMap { gameId ->
                val individualGameSearchBody: RequestBody = "where id = $gameId;\nfields cover.url, first_release_date, name, genres.name, platforms.name, franchise.name, involved_companies.developer, involved_companies.porting, involved_companies.publisher, involved_companies.supporting, involved_companies.company.name, game_modes.name, multiplayer_modes.*, player_perspectives.name, release_dates.date, release_dates.game, release_dates.human, release_dates.platform.name, release_dates.region, similar_games.name, summary;\nlimit 100;".toRequestBody("text/plain".toMediaTypeOrNull())
                liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                    val response = individualGameRepo.getIndividualGameData(twitchAuth.access_token, individualGameSearchBody)
                    if (response.isSuccessful){
                        emit(response.body())
                    } else{
                        println("getIndividualGameData Response not successful")
                        emit(IndividualGameData())
                    }
                }
            }
    }

    fun createImageURLForGlide(): LiveData<String> = getIndividualGameData().map { gameData ->
        if (gameData?.get(0)?.cover?.url == null) {
            ""
        } else {
            val baseUrl = URI(gameData[0].cover?.url)
            val segments = baseUrl.path.split("/")
            val lastSegment = segments[segments.size - 1]
            val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
            //TODO: ASK KEVIN TO WALK ME THROUGH HOW MOVING THIS TO A STATIC CONSTANT WOULD WORK. COMPILER NOT HAPPY WITH VARIABLE IN THERE
            "https://images.igdb.com/igdb/image/upload/t_1080p/${imageHash}.jpg"
        }
    }
}