package com.example.videogamesearcher.ui.individual_game

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.example.videogamesearcher.R
import com.example.videogamesearcher.models.TwitchAuthorization
import com.example.videogamesearcher.models.individual_game.IndividualGameData
import com.example.videogamesearcher.models.individual_game.InvolvedCompany
import com.example.videogamesearcher.models.individual_game.ReleaseDate
import com.example.videogamesearcher.repository.IndividualGameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    //TODO: CACHE ALL THIS LATER, OR NOT? DISCUSS WHAT'S FASTER/BEST PRACTICE
    var originalReleaseDate: LiveData<String> = getIndividualGameData().map { gameData ->
        formatDate(gameData?.get(0)?.first_release_date)
    }

    var originalPlatforms: LiveData<String> = getIndividualGameData().map { gameData ->
        val originalReleaseDate = gameData?.get(0)?.first_release_date
        val individualReleasesList = gameData?.get(0)?.release_dates

        findOriginalPlatforms(originalReleaseDate, individualReleasesList)
    }

    var developers: LiveData<String> = getIndividualGameData().map { gameData ->

        getDevelopers(gameData?.get(0)?.involved_companies)
    }

    var publishers: LiveData<String> = getIndividualGameData().map { gameData ->

        getPublishers(gameData?.get(0)?.involved_companies)
    }

    var summaryText: LiveData<String?> = getIndividualGameData().map { gameData ->
        if (gameData?.get(0)?.summary.isNullOrEmpty()) {
            NO_GAME_SUMMARY
        } else {
            gameData?.get(0)?.summary
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

    private fun formatDate(firstReleaseDate: Int?): String {
        if (firstReleaseDate == null) {
            return NO_RELEASE_DATE_FOUND_TEXT
        } else {
            val dateString = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(firstReleaseDate.toLong()))
            val substring = dateString.substring(0, 10)
            val localDate = LocalDate.parse(substring)
            val formatter = DateTimeFormatter.ofPattern(RELEASE_DATE_FORMAT)

            return "Original Release Date: ${formatter.format(localDate)}"
        }
    }

    //TODO: REVIEW THIS TO SEE IF I CAN SIMPLIFY
    private fun findOriginalPlatforms(originalReleaseDate: Int?, individualReleasesList: List<ReleaseDate?>?): String {

        var originalPlatforms = ORIGINAL_PLATFORMS_START_TEXT
        var releasesList = mutableListOf<String>()

        if (individualReleasesList.isNullOrEmpty() || originalReleaseDate == null) {
            originalPlatforms = NO_PLATFORMS_FOUND_TEXT
        } else {
            for (individualRelease in individualReleasesList.indices) {
                if (originalReleaseDate == individualReleasesList[individualRelease]?.date) {
                    individualReleasesList[individualRelease]?.platform?.name?.let { platformName -> releasesList.add(platformName) }
                }
            }
        }

        if (releasesList.size == 1) {
            originalPlatforms += releasesList[0]
        } else if (releasesList.size > 1) {
            for (release in releasesList.indices) {
                if (release == (releasesList.size - 1) ) {
                    originalPlatforms += releasesList[release]
                } else {
                    originalPlatforms += "${releasesList[release]}, "
                }
            }
        }
        return originalPlatforms
    }

    //TODO: TALK TO KEVIN ABOUT CONDENSING THE TWO FUNCTIONS BELOW INTO ONE. CAN WE CONCATENATE "developer" IN LINE 142. IF NOT, CHAT ABOUT ALTERNATIVE METHODS (PASSING IN AN ARBITRARY VALUE)
    private fun getDevelopers(involvedCompaniesList: List<InvolvedCompany?>?): String {
        var developersList = DEVELOPERS_LIST_START_TEXT

        if (involvedCompaniesList.isNullOrEmpty()) {
            developersList = NO_DEVELOPERS_FOUND_TEXT
        } else {

            for (company in involvedCompaniesList.indices) {
                if (involvedCompaniesList[company]?.developer == true) {
                    developersList += "${involvedCompaniesList[company]?.company?.name}, "
                }
            }
            developersList = developersList.substring(0, developersList.length-2)
        }

        return developersList
    }

    private fun getPublishers(involvedCompaniesList: List<InvolvedCompany?>?): String {
        var publishersList = PUBLISHERS_LIST_START_TEXT

        if (involvedCompaniesList.isNullOrEmpty()) {
            publishersList = NO_PUBLISHERS_FOUND_TEXT
        } else {

            for (company in involvedCompaniesList.indices) {
                if (involvedCompaniesList[company]?.publisher == true) {
                    publishersList += "${involvedCompaniesList[company]?.company?.name}, "
                }
            }
            publishersList = publishersList.substring(0, publishersList.length-2)
        }

        return publishersList
    }

    fun setDescriptionTextVisibility(descriptionTextVisibility: Int): Int {
        if (descriptionTextVisibility == GONE) {
            return VISIBLE
        } else  {
            return GONE
        }
    }

    fun determineArrowButtonStatus(descriptionTextVisibility: Int): Int {
        if (descriptionTextVisibility == GONE) {
            return R.drawable.drop_down_arrow_down
        } else {
            return R.drawable.drop_down_arrow_up
        }
    }

    companion object{
        const val NO_GAME_SUMMARY = "No game description provided."
        const val NO_RELEASE_DATE_FOUND_TEXT = "No release date found"
        const val RELEASE_DATE_FORMAT = "MMMM dd, yyyy"
        const val ORIGINAL_PLATFORMS_START_TEXT = "Originally released on: "
        const val NO_PLATFORMS_FOUND_TEXT = "No platforms available"
        const val DEVELOPERS_LIST_START_TEXT = "Developed by: "
        const val NO_DEVELOPERS_FOUND_TEXT = "No developers found"
        const val PUBLISHERS_LIST_START_TEXT = "Published by: "
        const val NO_PUBLISHERS_FOUND_TEXT = "No publishers found"
    }
}