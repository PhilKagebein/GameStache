package com.example.gamestache.ui.individual_game

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.example.gamestache.R
import com.example.gamestache.getAuthToken
import com.example.gamestache.models.GameMode
import com.example.gamestache.models.Genre
import com.example.gamestache.models.TwitchAuthorization
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import com.example.gamestache.models.individual_game.*
import com.example.gamestache.models.search_results.SearchResultsResponseItem
import com.example.gamestache.repository.GameStacheRepository
import com.example.gamestache.ui.explore.GamesListAdapterFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

class IndividualGameViewModel(private val gameStacheRepo: GameStacheRepository, private val resources: Resources) : ViewModel() {

    private var twitchAuthorization: MutableLiveData<TwitchAuthorization?> = MutableLiveData()
    var gameId: MutableLiveData<Int> = MutableLiveData()
    var releaseInformationText: MutableLiveData<String> = MutableLiveData()
    var coopCapabilitiesText: MutableLiveData<String> = MutableLiveData()
    var offlineCapabilitiesText: MutableLiveData<String> = MutableLiveData()
    var onlineCapabilitiesText: MutableLiveData<String> = MutableLiveData()
    val glideURL: MutableLiveData<String> = MutableLiveData()
    val similarGamesList: MutableLiveData<List<SimilarGame?>> = MutableLiveData()
    val releaseDatesList: MutableLiveData<List<ReleaseDate?>?> = MutableLiveData()
    val originalReleaseDate: MutableLiveData<String> = MutableLiveData()
    val originalPlatforms: MutableLiveData<String> = MutableLiveData()
    val developers: MutableLiveData<String> = MutableLiveData()
    val publishers: MutableLiveData<String> = MutableLiveData()
    val summaryText: MutableLiveData<String> = MutableLiveData()
    val regionsList: MutableLiveData<MutableList<String?>?> = MutableLiveData()
    val playerPerspectivesText: MutableLiveData<String> = MutableLiveData()
    val genresText: MutableLiveData<String> = MutableLiveData()
    val gameModesText: MutableLiveData<String> = MutableLiveData()
    val platformsListForMultiplayerModesSpinner: MutableLiveData<MutableList<String?>> = MutableLiveData()
    val multiplayerModesList: MutableLiveData<List<MultiplayerModesItem?>?> = MutableLiveData()

    val platformListFromDb = gameStacheRepo.getPlatformsListFromDb()

    fun getAuthToken(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val authToken = getAuthToken(context, gameStacheRepo)
            twitchAuthorization.postValue(authToken)

        }
    }

    private fun checkIfGameIsInRoom(): LiveData<Int> = gameId.switchMap { gameID ->
        gameStacheRepo.checkIfGameIsInDb(gameID)
    }

    fun getIndividualGameData(): LiveData<out List<IndividualGameDataItem?>>
        = twitchAuthorization.switchMap { twitchAuth ->
            gameId.switchMap { gameID ->
                checkIfGameIsInRoom().switchMap { gameStatusInRoom ->
                    if (gameStatusInRoom == GAME_NOT_IN_DB) {
                        val individualGameSearchBody: RequestBody = createIndividualGameSearchBodyRequestBody(gameID)

                        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                            twitchAuth?.access_token?.let { authToken ->
                                val response = gameStacheRepo.getIndividualGameDataFromApi(authToken, individualGameSearchBody)
                                if (response.isSuccessful){
                                    response.body()?.let {
                                        storeIndividualGameToDb(it)
                                        emit(it)
                                    } ?: run {
                                        Log.i(GET_INDIVIDUAL_GAME_DATA_TAG, API_RESPONSE_UNSUCCESSFUL_LOG_TEXT)
                                        emit(emptyList<IndividualGameDataItem>())
                                    }
                                } else{
                                    Log.i(GET_INDIVIDUAL_GAME_DATA_TAG, API_RESPONSE_UNSUCCESSFUL_LOG_TEXT)
                                    emit(emptyList<IndividualGameDataItem>())
                                }
                            } ?: run {
                                Log.i(GET_INDIVIDUAL_GAME_DATA_TAG, TWITCH_AUTH_NULL_TEXT)
                                emit(emptyList<IndividualGameDataItem>())
                            }
                        }

                    } else {
                       gameStacheRepo.getIndividualGameDataFromDb(gameID)
                    }
                }
            }
    }

    private suspend fun storeIndividualGameToDb(individualGameData: List<IndividualGameDataItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepo.storeIndividualGameToDb(individualGameData)
        }
    }

    val progressBarIsVisible: LiveData<Boolean>
    = getIndividualGameData().switchMap { gameData ->
        platformListFromDb.map { platformsListInDb ->
            if (!gameData.isNullOrEmpty()) {
                postImageUrl(gameData)
                postSimilarGamesList(gameData)
                postReleaseDateData(gameData)
                postOriginalReleaseDateText(gameData)
                postOriginalPlatformsText(gameData)
                postDevelopersText(gameData)
                postPublishersText(gameData)
                postSummaryText(gameData)
                postRegionsList(gameData)
                postPlayerPerspectivesText(gameData)
                postGenresText(gameData)
                postGameModesText(gameData)
                postPlatformsListForMultiplayerModesSpinner(gameData, platformsListInDb)
                postMultiplayerModesList(gameData)
            }
            false
        }
    }

    private fun postImageUrl(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.cover?.url?.let {
            val baseUrl = URI(gameData[0]?.cover?.url)
            val segments = baseUrl.path.split("/")
            val lastSegment = segments[segments.size - 1]
            val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
            glideURL.postValue( addImageHashToGlideURL(imageHash) )
        } ?: run {
            glideURL.postValue("")
        }
    }

    private fun postSimilarGamesList(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.similar_games?.let { similarGames ->
            similarGamesList.postValue(similarGames)
        } ?: run {
            similarGamesList.postValue(emptyList())
        }
    }

    private fun postReleaseDateData(gameData: List<IndividualGameDataItem?>) {
        releaseDatesList.postValue(gameData[0]?.release_dates)
    }

    private fun postOriginalReleaseDateText(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.first_release_date?.let {
            originalReleaseDate.postValue( formatDate(it) )
        } ?: run {
            originalReleaseDate.postValue( resources.getString(R.string.no_release_date_found_text) )
        }
    }

    private fun postOriginalPlatformsText(gameData: List<IndividualGameDataItem?>) {
        val originalReleaseDate = gameData[0]?.first_release_date
        val individualReleasesList = gameData[0]?.release_dates

        originalReleaseDate?.let { originalDate ->
            individualReleasesList?.let { releasesList ->
                originalPlatforms.postValue( createOriginalReleasePlatformsText(originalDate, releasesList) )
            }
        } ?: run {
            originalPlatforms.postValue( resources.getString(R.string.no_platforms_found_text) )
        }
    }

    private fun postDevelopersText(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.involved_companies?.let { involvedCompanies ->
            developers.postValue( createDevelopersText(involvedCompanies) )
        } ?: run {
            developers.postValue( resources.getString(R.string.no_developers_found_text) )
        }
    }

    private fun postPublishersText(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.involved_companies?.let { involvedCompanies ->
            publishers.postValue( createPublishersText(involvedCompanies) )
        } ?: run {
            publishers.postValue( resources.getString(R.string.no_publishers_found_text) )
        }
    }

    private fun postSummaryText(gameData: List<IndividualGameDataItem?>) {

        gameData[0]?.summary?.let {
            summaryText.postValue( it )
        } ?: run {
            summaryText.postValue( resources.getString(R.string.no_game_summary) )
        }
    }

    private fun postRegionsList(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.release_dates?.let { releaseDatesList ->
            regionsList.postValue( getRegionsReleasedList(releaseDatesList) )
        } ?: run {
            regionsList.postValue( null )
        }
    }

    private fun postPlayerPerspectivesText(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.player_perspectives?.let { playerPerspectives ->
            playerPerspectivesText.postValue( createPlayerPerspectivesText(playerPerspectives) )
        } ?: run {
            playerPerspectivesText.postValue( resources.getString(R.string.no_player_perspectives) )
        }
    }

    private fun postGenresText(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.genres?.let { genres ->
            genresText.postValue( createGenresText(genres) )
        } ?: run {
            genresText.postValue( resources.getString(R.string.no_genres_found_text) )
        }
    }

    private fun postGameModesText(gameData: List<IndividualGameDataItem?>) {
        gameData[0]?.game_modes?.let { gameModes ->
            gameModesText.postValue( createGameModesText(gameModes) )
        } ?: run {
            gameModesText.postValue( resources.getString(R.string.no_game_modes_text) )
        }
    }

    private fun postMultiplayerModesList(gameData: List<IndividualGameDataItem?>) {
        multiplayerModesList.postValue( gameData[0]?.multiplayer_modes )
    }

    private fun createGameModesText(gameModes: List<GameMode?>): String {
        val gameModesList = mutableListOf<String>()

        for (gameMode in gameModes) {
            gameMode?.name?.let { gameModesList.add(it) }
        }

        return gameModesList.joinToString(prefix = resources.getString(R.string.game_modes_list_prefix), separator = resources.getString(R.string.game_modes_list_separator))
    }

    private fun postPlatformsListForMultiplayerModesSpinner(gameData: List<IndividualGameDataItem?>, platformsListInDb: List<GenericSpinnerItem>) {
        val multiplayerModesRaw = gameData[0]?.multiplayer_modes
        val platformIntsList = mutableListOf<Int>()
        val platformNamesList: MutableList<String?> = mutableListOf(resources.getString(R.string.platform_spinner_prompt))

        multiplayerModesRaw?.let {
            for (platform in multiplayerModesRaw) {
                platform?.platform?.let { platformInt -> platformIntsList.add(platformInt) }
            }
        }
        for (int in platformIntsList) {
            for (entry in platformsListInDb) {
                if (int == entry.id) {
                    platformNamesList.add(entry.name)
                }
            }
        }
        platformsListForMultiplayerModesSpinner.postValue( platformNamesList )
    }

    fun getCoopCapabilitiesText(multiplayerModes: List<MultiplayerModesItem?>?, selectedPlatform: String, platformsListFromDb: List<GenericSpinnerItem>): String {
        val selectedPlatformInt = getSelectedPlatformInt(selectedPlatform, platformsListFromDb)
        val coopCapabilitiesList = getMultiplayerCapabilitiesMap(multiplayerModes, selectedPlatformInt)["coop"]

        if (coopCapabilitiesList != null) {
            return coopCapabilitiesList.joinToString(prefix = resources.getString(R.string.multiplayer_capabilities_prefix), separator = resources.getString(R.string.multiplayer_capabilities_separator))
        } else {
            return resources.getString(R.string.none)
        }

    }

    fun getOfflineCapabilitiesText(multiplayerModes: List<MultiplayerModesItem?>?, selectedPlatform: String, platformsListFromDb: List<GenericSpinnerItem>): String {
        val selectedPlatformInt = getSelectedPlatformInt(selectedPlatform, platformsListFromDb)
        val offlineCapabilitiesList = getMultiplayerCapabilitiesMap(multiplayerModes, selectedPlatformInt)["offline"]

        if (offlineCapabilitiesList != null) {
            return offlineCapabilitiesList.joinToString(prefix = resources.getString(R.string.multiplayer_capabilities_prefix), separator = resources.getString(R.string.multiplayer_capabilities_separator))
        } else {
            return resources.getString(R.string.none)
        }
    }

    fun getOnlineCapabilitiesText(multiplayerModes: List<MultiplayerModesItem?>?, selectedPlatform: String, platformsListFromDb: List<GenericSpinnerItem>): String {
        val selectedPlatformInt = getSelectedPlatformInt(selectedPlatform, platformsListFromDb)
        val onlineCapabilitiesList = getMultiplayerCapabilitiesMap(multiplayerModes, selectedPlatformInt)["online"]

        if (onlineCapabilitiesList != null) {
            return onlineCapabilitiesList.joinToString(prefix = resources.getString(R.string.multiplayer_capabilities_prefix), separator = resources.getString(R.string.multiplayer_capabilities_separator))
        } else {
            return resources.getString(R.string.none)
        }
    }

    private fun getMultiplayerCapabilitiesMap(multiplayerModes: List<MultiplayerModesItem?>?, selectedPlatformInt: Int): Map<String, MutableList<String>> {
        val coopCapabilitiesList = mutableListOf<String>()
        val offlineCapabilitiesList = mutableListOf<String>()
        val onlineCapabilitiesList = mutableListOf<String>()

        if (selectedPlatformInt == 0) {
            coopCapabilitiesList.add(resources.getString(R.string.none))
            offlineCapabilitiesList.add(resources.getString(R.string.none))
            onlineCapabilitiesList.add(resources.getString(R.string.none))
        } else {

            multiplayerModes?.let {
                for (platform in multiplayerModes) {
                    if (selectedPlatformInt == platform?.platform) {

                        if (platform.campaigncoop) {
                            coopCapabilitiesList.add(resources.getString(R.string.campaign_coop))
                        }
                        if (platform.lancoop) {
                            coopCapabilitiesList.add(resources.getString(R.string.lan_coop))
                        }

                        if (platform.offlinecoop) {
                            offlineCapabilitiesList.add(resources.getString(R.string.offline_coop))
                        }
                        if (platform.splitscreen) {
                            offlineCapabilitiesList.add(resources.getString(R.string.split_screen))
                        }

                        if (platform.onlinecoop) {
                            onlineCapabilitiesList.add(resources.getString(R.string.online_coop))
                        }
                        if (platform.dropin) {
                            onlineCapabilitiesList.add(resources.getString(R.string.drop_in_multi))
                        }

                        if (offlineCapabilitiesList.isNullOrEmpty()) {
                            offlineCapabilitiesList.add(resources.getString(R.string.none))
                        } else {
                            val offlineCoopMax = platform.offlinecoopmax
                            val offlineMax = platform.offlinemax
                            val maxPlayersOffline = determineMaxPlayers(offlineCoopMax, offlineMax)

                            if (maxPlayersOffline.isNotBlank()) {
                                offlineCapabilitiesList.add(maxPlayersOffline)
                            }


                        }

                        if (onlineCapabilitiesList.isNullOrEmpty()) {
                            onlineCapabilitiesList.add(resources.getString(R.string.none))
                        } else {
                            val onlineCoopMax = platform.onlinecoopmax
                            val onlineMax = platform.onlinemax
                            val maxPlayersOnline = determineMaxPlayers(onlineCoopMax, onlineMax)

                            if (maxPlayersOnline.isNotBlank()) {
                                onlineCapabilitiesList.add(maxPlayersOnline)
                            }
                        }

                        if (coopCapabilitiesList.isNullOrEmpty()) {
                            coopCapabilitiesList.add(resources.getString(R.string.none))
                        }

                    }

                }
            } ?: run {
                coopCapabilitiesList.add(resources.getString(R.string.none))
                offlineCapabilitiesList.add(resources.getString(R.string.none))
                onlineCapabilitiesList.add(resources.getString(R.string.none))
            }
        }
        return mapOf("coop" to coopCapabilitiesList, "offline" to offlineCapabilitiesList, "online" to onlineCapabilitiesList)
    }

    private fun determineMaxPlayers(coopMax: Int?, max: Int?): String {
        var localCoopMax = coopMax
        var localMax = max

        if (localCoopMax == null) localCoopMax = 0
        if (localMax == null) localMax = 0

        if ( (localCoopMax != 0) || (localMax != 0) ) {
            return resources.getString(R.string.max_players, max(localCoopMax, localMax))
        } else {
            return ""
        }

    }

    private fun getSelectedPlatformInt(selectedPlatform: String, platformList: List<GenericSpinnerItem>): Int {
        var platformInt = 0
        for (platform in platformList) {
            if (platform.name == selectedPlatform) {
                platformInt = platform.id
            }
        }
        return platformInt
    }

    private fun formatDate(firstReleaseDate: Int): String {

        val dateString = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(firstReleaseDate.toLong()))
        val substring = dateString.substring(0, 10)
        val localDate = LocalDate.parse(substring)
        val formatter = DateTimeFormatter.ofPattern(RELEASE_DATE_FORMAT)

        return resources.getString(R.string.original_release_date, formatter.format(localDate))

    }

    private fun createOriginalReleasePlatformsText(originalReleaseDate: Int, individualReleasesList: List<ReleaseDate?>): String {

        val releasesList = getOriginalReleasePlatformsList(originalReleaseDate, individualReleasesList)

        return releasesList.joinToString(prefix = resources.getString(R.string.original_platforms_start_text), separator = ", ")

    }

    private fun getOriginalReleasePlatformsList(originalReleaseDate: Int, individualReleasesList: List<ReleaseDate?>): MutableList<String> {
        val releasesList = mutableListOf<String>()

        for (individualRelease in individualReleasesList) {
            if (originalReleaseDate == individualRelease?.date) {
                individualRelease.platform?.name?.let { platformName ->
                    releasesList.add(platformName)
                }
            }
        }
        return releasesList
    }

    private fun createDevelopersText(involvedCompaniesList: List<InvolvedCompany?>): String {
        val developersList = createDevelopersAndPublishersList(involvedCompaniesList)["developers"]

        if (!developersList.isNullOrEmpty()) {
            return developersList.joinToString(prefix = resources.getString(R.string.developers_list_start_text), separator = ", ")
        } else {
            return resources.getString(R.string.no_developers_found_text)
        }

    }

    private fun createPublishersText(involvedCompaniesList: List<InvolvedCompany?>): String {
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

        for (company in involvedCompaniesList) {
            if (company?.publisher == true) {
                company.company?.name?.let { publisherList.add(it) }
            }
            if (company?.developer == true) {
                company.company?.name?.let { developerList.add(it) }
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

    fun getMultiplayerTitleAndSpinnerVisibility(currentVisibility: Int, multiplayerModes: List<MultiplayerModesItem?>?): Int {
        if (multiplayerModes.isNullOrEmpty()) return GONE
        else {
            return changeCardViewVisibility(currentVisibility)
        }
    }

    fun determineDropDownButtonOrientation(viewVisibility: Int): Int {
        if (viewVisibility == GONE) {
            return R.drawable.drop_down_arrow_up
        } else {
            return R.drawable.drop_down_arrow_down
        }
    }

    private fun updateFavoriteAndWishlistStatus(game: List<IndividualGameDataItem?>, newFavoriteStatus: Boolean, newWishlistStatus: Boolean, favoriteOrWishlistStatusInTable: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            game[0]?.let { game ->
                if (favoriteOrWishlistStatusInTable == GAME_NOT_IN_DB) {
                    gameStacheRepo.addGameToFavoriteAndWishlistTable(
                        SearchResultsResponseItem(game.id, game.cover, game.game_modes, game.genres, game.name, game.platforms, newFavoriteStatus, newWishlistStatus))
                } else {
                    game.id?.let { id ->
                        newFavoriteStatus.let { newFavoriteStatus ->
                            newWishlistStatus.let { newWishlistStatus ->
                                gameStacheRepo.updateFavoriteAndWishlistStatus(id, newFavoriteStatus, newWishlistStatus)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getRegionsReleasedList(individualReleasesList: List<ReleaseDate?>): MutableList<String?> {

        val regionInts = getRegionIntsList(individualReleasesList)
        return createRegionsReleasedList(regionInts)
    }

    private fun getRegionIntsList(individualReleasesList: List<ReleaseDate?>): List<Int> {
        val regionInts = mutableListOf<Int>()

        for (release in individualReleasesList) {
            if (regionInts.contains(release?.region)) {
                continue
            } else {
                release?.region?.let {
                    regionInts.add(it)
                }
            }
        }
        return regionInts.sorted()
    }

    private fun createRegionsReleasedList(regionInts: List<Int>): MutableList<String?> {
        val regionsList: MutableList<String?> = mutableListOf(resources.getString(R.string.region_spinner_prompt))

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
            for (release in releaseDates) {
                if (selectedRegionInt == release?.region) {
                    releaseInformationText += "${resources.getString(R.string.individual_release_start_text)} ${release.platform?.name}, ${release.human}\n"
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

    private fun createPlayerPerspectivesText(playerPerspectives: List<PlayerPerspective?>): String {
        val playerPerspectivesList = mutableListOf<String>()

        for (perspective in playerPerspectives) {
            perspective?.name?.let { playerPerspectivesList.add(it) }
        }

        return playerPerspectivesList.joinToString(prefix = resources.getString(R.string.player_perspectives_prefix), separator = ", ")
    }

    private fun createGenresText(genres: List<Genre?>): String {
        val genresList = mutableListOf<String>()

        for (perspective in genres) {
            perspective?.name?.let { genresList.add(it) }
        }

        return genresList.joinToString(prefix = resources.getString(R.string.genres_prefix), separator = ", ")
    }

    private fun changeFavoriteAndWishlistStatus(gameData: List<IndividualGameDataItem?>, buttonPressed: GamesListAdapterFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            var favoriteStatus: Boolean? = false
            var wishlistStatus: Boolean? = false

            val favoriteOrWishlistStatusInTable: Int? = gameData[0]?.id?.let { gameStacheRepo.checkIfGameIsInFavoriteAndWishlistTable(it) }

            if (favoriteOrWishlistStatusInTable != GAME_NOT_IN_DB) {
                val gameDataFromDb = gameData[0]?.id?.let { gameStacheRepo.getIndividualGameInfo(it) }
                favoriteStatus = gameDataFromDb?.favoriteStatus
                wishlistStatus = gameDataFromDb?.wishlistStatus
            }

            when (buttonPressed) {
                GamesListAdapterFragment.FAVORITES -> {favoriteStatus = favoriteStatus?.not()}
                GamesListAdapterFragment.WISHLIST -> {wishlistStatus = wishlistStatus?.not()}
                else -> {

                }
            }

            favoriteStatus?.let { currentFavoriteStatus ->
                wishlistStatus?.let { wishlistStatus ->
                    favoriteOrWishlistStatusInTable?.let { favoriteOrWishlistStatusInTable ->
                        updateFavoriteAndWishlistStatus(gameData, currentFavoriteStatus, wishlistStatus, favoriteOrWishlistStatusInTable)
                    }
                }
            }
        }
    }

    fun onFavoriteOrWishlistButtonPush(buttonText: String, gameData: List<IndividualGameDataItem?>, buttonPressed: GamesListAdapterFragment): String {
        var newButtonText = ""
        changeFavoriteAndWishlistStatus(gameData, buttonPressed)

            when (buttonText) {
                resources.getString(R.string.add_to_favorites_button_text) -> {
                    newButtonText = resources.getString(R.string.game_is_a_favorite_text)
                }
                resources.getString(R.string.game_is_a_favorite_text) -> {
                    newButtonText = resources.getString(R.string.add_to_favorites_button_text)
                }

                resources.getString(R.string.add_to_wishlist_button_text) -> {
                    newButtonText = resources.getString(R.string.game_is_in_wishlist_text)
                }

                resources.getString(R.string.game_is_in_wishlist_text) -> {
                    newButtonText = resources.getString(R.string.add_to_wishlist_button_text)
                }
            }

        return newButtonText
    }

    val currentStatusAsAFavorite: LiveData<String>
        = gameId.switchMap { gameId ->
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                if ( (gameStacheRepo.checkIfGameIsInFavoriteAndWishlistTable(gameId) == GAME_NOT_IN_DB) || gameStacheRepo.getIndividualGameInfo(gameId).favoriteStatus == NO ) {
                    emit(resources.getString(R.string.add_to_favorites_button_text))
                } else {
                    emit(resources.getString(R.string.game_is_a_favorite_text))
                }
            }
    }

    val currentStatusAsWishlistItem: LiveData<String>
            = gameId.switchMap { gameId ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            if ((gameStacheRepo.checkIfGameIsInFavoriteAndWishlistTable(gameId) == GAME_NOT_IN_DB) || gameStacheRepo.getIndividualGameInfo(gameId).wishlistStatus == NO) {
                emit(resources.getString(R.string.add_to_wishlist_button_text))
            } else {
                emit(resources.getString(R.string.game_is_in_wishlist_text))
            }
        }
    }

    companion object{

        const val RELEASE_DATE_FORMAT = "MMMM dd, yyyy"
        const val GAME_NOT_IN_DB = 0
        const val NO = false
        fun addImageHashToGlideURL(imageHash: String): String = "https://images.igdb.com/igdb/image/upload/t_1080p/${imageHash}.jpg"
        fun createIndividualGameSearchBodyRequestBody(gameID: Int): RequestBody = "where id = $gameID;\nfields cover.url, first_release_date, name, genres.name, platforms.name, franchise.name, involved_companies.developer, involved_companies.porting, involved_companies.publisher, involved_companies.supporting, involved_companies.company.name, game_modes.name, multiplayer_modes.*, player_perspectives.name, release_dates.date, release_dates.game, release_dates.human, release_dates.platform.name, release_dates.region, similar_games.name, summary;\nlimit 100;".toRequestBody("text/plain".toMediaTypeOrNull())
        const val GET_INDIVIDUAL_GAME_DATA_TAG = "getIndividualGameData - Api Response"
        const val API_RESPONSE_UNSUCCESSFUL_LOG_TEXT = "Response from Api unsuccessful"
        const val TWITCH_AUTH_NULL_TEXT = "Twitch Auth is Null"
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