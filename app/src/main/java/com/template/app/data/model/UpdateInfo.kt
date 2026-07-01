package com.template.app.data.model

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val isForce: Boolean,
    val apkSize: Long,
    val updateLog: String
)
