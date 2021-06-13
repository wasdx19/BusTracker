package com.example.busstopreminder.util

import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError

class SearchSuggestionUtill(
    private var searchManagerParam: SearchManager?,
    private var suggestSessionParam: SuggestSession?
) : SuggestSession.SuggestListener {

    companion object {
        const val RESULT_NUMBER_LIMIT = 5
    }

    private var suggestSession: SuggestSession? = null
    private var searchManager: SearchManager? = null

    var onSuggestionErrorAccured: ((String) -> Unit)? = null
    var onSuggestionResult: ((List<String>) -> Unit)? = null

    private var suggestResult = arrayListOf<String>()

    private var pointCenter = Point(55.75, 37.62)
    private var boxSize = 0.2

    private val boundingBox = BoundingBox(
        Point(pointCenter.latitude - boxSize, pointCenter.longitude - boxSize),
        Point(pointCenter.latitude + boxSize, pointCenter.longitude + boxSize)
    )

    private val suggestionOption = SuggestOptions().setSuggestTypes(
        SuggestType.GEO.value or SuggestType.BIZ.value or SuggestType.TRANSIT.value
    )

    init {
        searchManager = searchManagerParam
        suggestSession = suggestSessionParam
    }


    fun requestSuggest(query: String) {
        suggestSession!!.suggest(query, boundingBox, suggestionOption, this)
    }

    override fun onResponse(item: MutableList<SuggestItem>) {
        suggestResult.clear()

        for (i in 0 until RESULT_NUMBER_LIMIT.coerceAtMost(item.size)) {
            suggestResult.add(item[i].displayText.orEmpty())
        }

        onSuggestionResult?.invoke(suggestResult)
    }

    override fun onError(error: Error) {
        var errorMessage = "Ошибка подсказки"
        if (error is RemoteError) {
            errorMessage = "удаленная ошибка подсказки"
        } else if (error is NetworkError) {
            errorMessage = "сеть ошибка подсказки"
        }

        onSuggestionErrorAccured?.invoke(errorMessage)
    }

}