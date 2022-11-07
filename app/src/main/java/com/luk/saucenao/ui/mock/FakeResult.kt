package com.luk.saucenao.ui.mock

import com.luk.saucenao.Results

class FakeResult : Results.Result() {
    override val similarity = "48.17%"
    override val thumbnail = ""
    override var title: String? = "ibuki fuuko gallery(clannad)"
    override val extUrls = arrayListOf<String>()
    override val columns = arrayListOf(
        "Creator(s): Unknown",
        "ibuki fuuko gallery(clannad)",
    )
}
