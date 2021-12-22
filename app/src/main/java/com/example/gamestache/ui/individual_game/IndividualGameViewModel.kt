package com.example.gamestache.ui.individual_game

import android.content.res.Resources
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.example.gamestache.R
import com.example.gamestache.models.TwitchAuthorization
import com.example.gamestache.models.individual_game.*
import com.example.gamestache.repository.GameStacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.NullPointerException
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class IndividualGameViewModel(private val repository: GameStacheRepository, private val resources: Resources) : ViewModel() {

    private var twitchAuthorization: MutableLiveData<TwitchAuthorization> = MutableLiveData()
    var gameId: MutableLiveData<Int> = MutableLiveData()
    var releaseInformationText: MutableLiveData<String> = MutableLiveData()

    fun getAccessToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getAccessToken()
            if (response.isSuccessful) {
                twitchAuthorization.postValue(response.body())
            } else {
                //Change how this is handled in the future.
                println("Twitch auth token response not successful.")
            }
        }
    }

    private fun checkIfGameIsInRoom(): LiveData<Int> = gameId.switchMap { gameID ->
        repository.checkIfGameIsInRoom(gameID)
    }

    private fun getIndividualGameData(): LiveData<out List<IndividualGameDataItem?>>
        = twitchAuthorization.switchMap { twitchAuth ->
            gameId.switchMap { gameID ->
                checkIfGameIsInRoom().switchMap { gameStatusInRoom ->
                    if (gameStatusInRoom == GAME_NOT_IN_ROOM) {
                        val individualGameSearchBody: RequestBody = createIndividualGameSearchBodyRequestBody(gameID)

                        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                            val response = repository.getIndividualGameData(twitchAuth.access_token, individualGameSearchBody)
                            if (response.isSuccessful){
                                response.body()?.let {
                                    storeIndividualGameToDb(it)
                                    emit(it)
                                } ?: throw NullPointerException()

                            } else{
                                //TODO: LOOK INTO HOW TO BETTER HANDLE A NULL RESPONSE
                                println("getIndividualGameData Response not successful")
                                throw NullPointerException()
                            }
                        }
                    } else {
                        repository.getIndividualGameDataFromRoom(gameID)
                    }
                }
            }
    }

    fun getReleaseDatesList(): LiveData<List<ReleaseDate?>?> =
        getIndividualGameData().map { gameData ->
            gameData[0]?.release_dates
        }

    private suspend fun storeIndividualGameToDb(individualGameData: List<IndividualGameDataItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.storeIndividualGameToDb(individualGameData)
        }
    }

    //TODO: IS THERE ANY REASON TO PULL A SUBSET OF GAMEDATA FROM THE DB WHEN GETINDIVIDUALGAMEDATA() IS ALREADY GETTING THE MOST UP TO DATE DATA?
    var originalReleaseDate: LiveData<String> = getIndividualGameData().map { gameData ->
        gameData[0]?.first_release_date?.let {
            formatDate(it)
        } ?: run {
            resources.getString(R.string.no_release_date_found_text)
        }
    }

    var originalPlatforms: LiveData<String> = getIndividualGameData().map { gameData ->
        val originalReleaseDate = gameData[0]?.first_release_date
        val individualReleasesList = gameData[0]?.release_dates

        originalReleaseDate?.let { originalDate ->
            individualReleasesList?.let { releasesList ->
                findOriginalPlatforms(originalDate, releasesList)
            }
        } ?: run {
            resources.getString(R.string.no_platforms_found_text)
        }
    }

    var developers: LiveData<String> = getIndividualGameData().map { gameData ->

        gameData[0]?.involved_companies?.let { involvedCompanies ->
            getDevelopers(involvedCompanies)
        } ?: run {
            resources.getString(R.string.no_developers_found_text)
        }
    }

    var publishers: LiveData<String> = getIndividualGameData().map { gameData ->

        gameData[0]?.involved_companies?.let { involvedCompanies ->
            getPublishers(involvedCompanies)
        } ?: run {
            resources.getString(R.string.no_publishers_found_text)
        }
    }

    var summaryText: LiveData<String?> = getIndividualGameData().map { gameData ->

        gameData[0]?.summary ?: run {
           resources.getString(R.string.no_game_summary)
       }

    }

    var regionsList: LiveData<MutableList<String>> = getIndividualGameData().map { gameData ->

        gameData[0]?.release_dates?.let { releaseDatesList ->
            getRegionsReleased(releaseDatesList)
        } ?: run {
            mutableListOf(resources.getString(R.string.no_regions_found_text))
        }

    }

    var playerPerspectivesText: LiveData<String> = getIndividualGameData().map { gameData ->
        gameData[0]?.player_perspectives?.let { playerPerspectives ->
            getPlayerPerspectivesText(playerPerspectives)
        } ?: run {
            resources.getString(R.string.no_player_perspectives)
        }
    }

    var genresText: LiveData<String> = getIndividualGameData().map { gameData ->
        gameData[0]?.genres?.let { genres ->
            getGenreText(genres)
        } ?: run {
            resources.getString(R.string.no_genres_found_text)
        }
    }

    fun createImageURLForGlide(): LiveData<String> = getIndividualGameData().map { gameData ->

        gameData[0]?.cover?.url?.let {
            val baseUrl = URI(gameData[0]?.cover?.url)
            val segments = baseUrl.path.split("/")
            val lastSegment = segments[segments.size - 1]
            val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
            addImageHashToGlideURL(imageHash)
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

    private fun findOriginalPlatforms(originalReleaseDate: Int, individualReleasesList: List<ReleaseDate?>): String {

        val releasesList = getOriginalReleasePlatformsList(originalReleaseDate, individualReleasesList)

        return releasesList.joinToString(prefix = resources.getString(R.string.original_platforms_start_text), separator = ", ")

    }

    private fun getOriginalReleasePlatformsList(originalReleaseDate: Int, individualReleasesList: List<ReleaseDate?>): MutableList<String> {
        val releasesList = mutableListOf<String>()

        for (individualRelease in individualReleasesList.indices) {
            if (originalReleaseDate == individualReleasesList[individualRelease]?.date) {
                individualReleasesList[individualRelease]?.platform?.name?.let { platformName ->
                    releasesList.add(platformName)
                }
            }
        }
        return releasesList
    }

    private fun getDevelopers(involvedCompaniesList: List<InvolvedCompany?>): String {
        val developersList = createDevelopersAndPublishersList(involvedCompaniesList)["developers"]

        if (!developersList.isNullOrEmpty()) {
            return developersList.joinToString(prefix = resources.getString(R.string.developers_list_start_text), separator = ", ")
        } else {
            return resources.getString(R.string.no_developers_found_text)
        }

    }

    private fun getPublishers(involvedCompaniesList: List<InvolvedCompany?>): String {
        val publishersList = createDevelopersAndPublishersList(involvedCompaniesList)["publishers"]

        if (!publishersList.isNullOrEmpty()) {
            return publishersList.joinToString(prefix = resources.getString(R.string.publishers_list_start_text), separator = ", ")
        } else {
            return resources.getString(R.string.no_publishers_found_text)
        }

    }

    private fun createDevelopersAndPublishersList(involvedCompaniesList: List<InvolvedCompany?>): Map<String, MutableList<String>> {
        val developerList = mutableListOf<String>()
        val publisherList = mutableListOf<String>()

        for (company in involvedCompaniesList.indices) {
            if (involvedCompaniesList[company]?.publisher == true) {
                involvedCompaniesList[company]?.company?.name?.let { publisherList.add(it) }
            }
            if (involvedCompaniesList[company]?.developer == true) {
                involvedCompaniesList[company]?.company?.name?.let { developerList.add(it) }
            }
        }
        return mapOf("publishers" to publisherList, "developers" to developerList)
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
        val regionsList = mutableListOf(resources.getString(R.string.region_spinner_prompt))

        for (regionInt in regionInts) {
            regionsList.add(RegionReleases.values()[(regionInt-1)].regionName)
        }
        return regionsList
    }

    fun getReleaseInformationText(releaseDates: List<ReleaseDate?>, selectedRegion: String): String {
        var releaseInformationText = ""

        val selectedRegionInt = getSelectedRegionInt(selectedRegion)

        if (releaseDates.isNullOrEmpty()) {
            return resources.getString(R.string.no_release_information_found_text)
        } else {
            for (release in releaseDates.indices) {
                if (selectedRegionInt == releaseDates[release]?.region) {
                    releaseInformationText += "${resources.getString(R.string.individual_release_start_text)} ${releaseDates[release]?.platform?.name}, ${releaseDates[release]?.human}\n"
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

    private fun getPlayerPerspectivesText(playerPerspectives: List<PlayerPerspective?>): String {
        val playerPerspectivesList = mutableListOf<String>()

        for (perspective in playerPerspectives.indices) {
            playerPerspectives[perspective]?.name?.let { playerPerspectivesList.add(it) }
        }

        return playerPerspectivesList.joinToString(prefix = resources.getString(R.string.player_perspectives_prefix), separator = ", ")
    }

    private fun getGenreText(genres: List<Genre?>): String {
        val genresList = mutableListOf<String>()

        for (perspective in genres.indices) {
            genres[perspective]?.name?.let { genresList.add(it) }
        }

        return genresList.joinToString(prefix = resources.getString(R.string.genres_prefix), separator = ", ")
    }

    companion object{

        const val RELEASE_DATE_FORMAT = "MMMM dd, yyyy"
        const val GAME_NOT_IN_ROOM= 0
        fun addImageHashToGlideURL(imageHash: String): String = "https://images.igdb.com/igdb/image/upload/t_1080p/${imageHash}.jpg"
        fun createIndividualGameSearchBodyRequestBody(gameID: Int): RequestBody = "where id = $gameID;\nfields cover.url, first_release_date, name, genres.name, platforms.name, franchise.name, involved_companies.developer, involved_companies.porting, involved_companies.publisher, involved_companies.supporting, involved_companies.company.name, game_modes.name, multiplayer_modes.*, player_perspectives.name, release_dates.date, release_dates.game, release_dates.human, release_dates.platform.name, release_dates.region, similar_games.name, summary;\nlimit 100;".toRequestBody("text/plain".toMediaTypeOrNull())

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