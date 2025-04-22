package com.zrgenesiscloud.visioncue.ui.editor

import kotlinx.serialization.Serializable

// Data class to track formatting information
@Serializable
data class FormattingInfo(
    val start: Int,
    val end: Int,
    val formatType: TextFormatType
)