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
    var releaseInformationText: MutableLiveData<String> = MutableLiveData()

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

    fun getIndividualGameData(): LiveData<IndividualGameData?>
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
    //TODO: IS THE LET STATEMENT CHECKING WHETHER ALL THINGS OF THE OBJECT "IT" ARE NOT NULL? OR JUST THE LAST ONE?
    var originalReleaseDate: LiveData<String> = getIndividualGameData().map { gameData ->
        gameData?.get(0)?.first_release_date?.let {
            formatDate(it)
        } ?: run {
            NO_RELEASE_DATE_FOUND_TEXT
        }
    }

    var originalPlatforms: LiveData<String> = getIndividualGameData().map { gameData ->
        val originalReleaseDate = gameData?.get(0)?.first_release_date
        val individualReleasesList = gameData?.get(0)?.release_dates

        originalReleaseDate?.let { originalDate ->
            individualReleasesList?.let { releasesList ->
                findOriginalPlatforms(originalDate, releasesList)
            }
        } ?: run {
            NO_PLATFORMS_FOUND_TEXT
        }
    }

    var developers: LiveData<String> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.involved_companies?.let { involvedCompanies ->
            getDevelopers(involvedCompanies)
        } ?: run {
            NO_DEVELOPERS_FOUND_TEXT
        }
    }

    var publishers: LiveData<String> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.involved_companies?.let { involvedCompanies ->
            getPublishers(involvedCompanies)
        } ?: run {
            NO_PUBLISHERS_FOUND_TEXT
        }
    }

    var summaryText: LiveData<String?> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.summary ?: run {
           NO_GAME_SUMMARY
       }

    }

    //TODO: HOW TO CHECK WHETHER GAMEDATA.RELEASEDATES EXISTS AT ALL?
    var regionsList: LiveData<MutableList<String>> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.release_dates?.let { releaseDatesList ->
            getRegionsReleased(releaseDatesList)
        } ?: run {
            mutableListOf(NO_REGIONS_FOUND_TEXT)
        }

    }

    fun createImageURLForGlide(): LiveData<String> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.cover?.url?.let {
            val baseUrl = URI(gameData[0].cover?.url)
            val segments = baseUrl.path.split("/")
            val lastSegment = segments[segments.size - 1]
            val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
            //TODO: ASK KEVIN TO WALK ME THROUGH HOW MOVING THIS TO A STATIC CONSTANT WOULD WORK. COMPILER NOT HAPPY WITH VARIABLE IN THERE
            "https://images.igdb.com/igdb/image/upload/t_1080p/${imageHash}.jpg"
        } ?: run {
            ""
        }
    }

    private fun formatDate(firstReleaseDate: Int): String {

        val dateString = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(firstReleaseDate.toLong()))
        val substring = dateString.substring(0, 10)
        val localDate = LocalDate.parse(substring)
        val formatter = DateTimeFormatter.ofPattern(RELEASE_DATE_FORMAT)

        return "Original Release Date: ${formatter.format(localDate)}"

    }

    //TODO: REVIEW THIS TO SEE IF I CAN SIMPLIFY
    private fun findOriginalPlatforms(originalReleaseDate: Int, individualReleasesList: List<ReleaseDate?>): String {

        var originalPlatforms = ORIGINAL_PLATFORMS_START_TEXT
        val releasesList = mutableListOf<String>()

        for (individualRelease in individualReleasesList.indices) {
            if (originalReleaseDate == individualReleasesList[individualRelease]?.date) {
                individualReleasesList[individualRelease]?.platform?.name?.let { platformName ->
                    releasesList.add(platformName)
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

    //TODO: TALK TO KEVIN ABOUT CONDENSING THE TWO FUNCTIONS BELOW INTO ONE. CAN WE CONCATENATE "developer" IN LINE 174. IF NOT, CHAT ABOUT ALTERNATIVE METHODS (PASSING IN AN ARBITRARY VALUE)
    private fun getDevelopers(involvedCompaniesList: List<InvolvedCompany?>): String {
        var developersList = DEVELOPERS_LIST_START_TEXT
        var developersCount = 0

        for (company in involvedCompaniesList.indices) {
            if (involvedCompaniesList[company]?.developer == true) {
                developersList += "${involvedCompaniesList[company]?.company?.name}, "
                developersCount++
            }
        }
        developersList = developersList.substring(0, developersList.length - 2)

        if (developersCount > 0) {
            return developersList
        } else {
            return NO_DEVELOPERS_FOUND_TEXT
        }
    }

    private fun getPublishers(involvedCompaniesList: List<InvolvedCompany?>): String {
        var publishersList = PUBLISHERS_LIST_START_TEXT
        var publishersCount = 0

        for (company in involvedCompaniesList.indices) {
            if (involvedCompaniesList[company]?.publisher == true) {
                publishersList += "${involvedCompaniesList[company]?.company?.name}, "
                publishersCount++
            }
        }
        publishersList = publishersList.substring(0, publishersList.length - 2)

        if (publishersCount > 0) {
            return publishersList
        } else {
            return NO_PUBLISHERS_FOUND_TEXT
        }
    }

    fun changeCardViewVisibility(currentVisibility: Int): Int {
        if (currentVisibility == GONE) {
            return VISIBLE
        } else  {
            return GONE
        }
    }

    fun determineArrowButtonStatus(viewVisibility: Int): Int {
        if (viewVisibility == GONE) {
            return R.drawable.drop_down_arrow_down
        } else {
            return R.drawable.drop_down_arrow_up
        }
    }

    private fun getRegionsReleased(individualReleasesList: List<ReleaseDate?>): MutableList<String> {

        val regionInts = getRegionIntsList(individualReleasesList)
        return getRegionNameListFromInts(regionInts)
    }

    private fun getRegionIntsList(individualReleasesList: List<ReleaseDate?>): List<Int> {
        val regionInts = mutableListOf<Int>()

        for (release in individualReleasesList.indices) {
            if (regionInts.contains(individualReleasesList[release]?.region)) {
                continue
            } else {
                individualReleasesList[release]?.region?.let {
                    regionInts.add(it)
                }
            }
        }
        return regionInts.sorted()
    }

    private fun getRegionNameListFromInts(regionInts: List<Int>): MutableList<String> {
        val regionsList = mutableListOf(REGION_SPINNER_PROMPT)

        for (regionInt in regionInts) {
            regionsList.add(RegionReleases.values()[(regionInt-1)].regionName)
        }
        return regionsList
    }

    fun getReleaseInformationText(releaseDates: List<ReleaseDate?>, selectedRegion: String): String {
        var releaseInformationText = ""

        val selectedRegionInt = getSelectedRegionInt(selectedRegion)

        if (releaseDates.isNullOrEmpty()) {
            return NO_RELEASE_INFORMATION_FOUND_TEXT
        } else {
            for (release in releaseDates.indices) {
                if (selectedRegionInt == releaseDates[release]?.region) {
                    releaseInformationText += "$INDIVIDUAL_RELEASE_START_TEXT ${releaseDates[release]?.platform?.name}, ${releaseDates[release]?.human}\n"
                }
            }
            return releaseInformationText.trim()
        }
    }

    private fun getSelectedRegionInt(selectedRegion: String): Int {
        var regionInt = 0
        val enumList = RegionReleases.values()
        for (i in enumList) {
            if (i.regionName == selectedRegion) {
                regionInt = i.regionValue
            }
        }
        return regionInt
    }

    companion object{
        const val NO_GAME_SUMMARY = "No game description provided."
        const val NO_RELEASE_DATE_FOUND_TEXT = "No release date found"
        const val RELEASE_DATE_FORMAT = "MMMM dd, yyyy"
        const val ORIGINAL_PLATFORMS_START_TEXT = "Originally released on: "
        const val NO_PLATFORMS_FOUND_TEXT = "No release platforms data found"
        const val DEVELOPERS_LIST_START_TEXT = "Developed by: "
        const val NO_DEVELOPERS_FOUND_TEXT = "No developer data found"
        const val PUBLISHERS_LIST_START_TEXT = "Published by: "
        const val NO_PUBLISHERS_FOUND_TEXT = "No publisher data found"
        const val INDIVIDUAL_RELEASE_START_TEXT = "Released on"
        const val NO_REGIONS_FOUND_TEXT = "No regions found"
        const val REGION_SPINNER_PROMPT = "Select a region"
        const val NO_RELEASE_INFORMATION_FOUND_TEXT = "No release information found"
    }

}

enum class RegionReleases(val regionValue: Int, val regionName: String) {
    EUROPE(1, "Europe"),
    NORTH_AMERICA(2, "North America"),
    AUSTRALIA(3, "Australia"),
    NEW_ZEALAND(4, "New Zealand"),
    JAPAN(5, "Japan"),
    CHINA(6, "China"),
    ASIA(7, "Asia"),
    WORLDWIDE(8, "Worldwide"),
    KOREA(9, "Korea"),
    BRAZIL(10, "Brazil")
}