package com.alex.ubercloneapp.models

class FCMResponse(
    val success: Int? = null,
    val failure: Int? = null,
    val canonical_ids: Int? = null,
    val multicas_id: Long? = null,
    val results: ArrayList<Any> = ArrayList<Any>()
) {
}