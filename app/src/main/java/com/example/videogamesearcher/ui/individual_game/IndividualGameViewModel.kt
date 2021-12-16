package com.example.videogamesearcher.ui.individual_game

import android.content.res.Resources
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

class IndividualGameViewModel(private val resources: Resources) : ViewModel() {

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
    var originalReleaseDate: LiveData<String> = getIndividualGameData().map { gameData ->
        gameData?.get(0)?.first_release_date?.let {
            formatDate(it)
        } ?: run {
            resources.getString(R.string.no_release_date_found_text)
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
            resources.getString(R.string.no_platforms_found_text)
        }
    }

    var developers: LiveData<String> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.involved_companies?.let { involvedCompanies ->
            getDevelopers(involvedCompanies)
        } ?: run {
            resources.getString(R.string.no_developers_found_text)
        }
    }

    var publishers: LiveData<String> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.involved_companies?.let { involvedCompanies ->
            getPublishers(involvedCompanies)
        } ?: run {
            resources.getString(R.string.no_publishers_found_text)
        }
    }

    var summaryText: LiveData<String?> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.summary ?: run {
           resources.getString(R.string.no_game_summary)
       }

    }

    var regionsList: LiveData<MutableList<String>> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.release_dates?.let { releaseDatesList ->
            getRegionsReleased(releaseDatesList)
        } ?: run {
            mutableListOf(resources.getString(R.string.no_regions_found_text))
        }

    }

    fun createImageURLForGlide(): LiveData<String> = getIndividualGameData().map { gameData ->

        gameData?.get(0)?.cover?.url?.let {
            val baseUrl = URI(gameData[0].cover?.url)
            val segments = baseUrl.path.split("/")
            val lastSegment = segments[segments.size - 1]
            val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
            //TODO: LOOK INTO STATIC FUNCTION OR INTERPOLATION
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

        var originalPlatforms = resources.getString(R.string.original_platforms_start_text)
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

    //TODO: USE STRING INTERPOLATION TO GET RID OF THE START TEXT AND DEVELOPERS COUNT
    private fun getDevelopers(involvedCompaniesList: List<InvolvedCompany?>): String {
        var developersList = resources.getString(R.string.developers_list_start_text)
        var developersCount = 0

        //TODO: PULL THE FOR LOOP OUT INTO ITS OWN FUNCTION
        for (company in involvedCompaniesList.indices) {
            if (involvedCompaniesList[company]?.developer == true) {
                //TODO: LOOK INTO .JOINTOSTRING() AND ELIMINATE CONCATENATION AND MAKE THIS AN ACTUAL LIST
                developersList += "${involvedCompaniesList[company]?.company?.name}, "
                developersCount++
            }
        }
        developersList = developersList.substring(0, developersList.length - 2)

        if (developersCount > 0) {
            return developersList
        } else {
            return resources.getString(R.string.no_developers_found_text)
        }
    }

    private fun getPublishers(involvedCompaniesList: List<InvolvedCompany?>): String {
        var publishersList = resources.getString(R.string.publishers_list_start_text)
        var publishersCount = 0

        //TODO: PULL THIS OUT TOO
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
            return resources.getString(R.string.no_publishers_found_text)
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

    //TODO: MOVE TO STRINGS.XML
    companion object{

        const val RELEASE_DATE_FORMAT = "MMMM dd, yyyy"

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