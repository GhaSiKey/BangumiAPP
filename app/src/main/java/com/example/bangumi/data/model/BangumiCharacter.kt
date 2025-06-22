package com.example.bangumi.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created by gaoshiqi
 * on 2025/6/9 18:51
 * email: gaoshiqi@bilibili.com
 */
// 角色类型枚举
enum class CharacterType(val value: Int) {
    CHARACTER(1),
    MECHANIC(2),
    SHIP(3),
    ORGANIZATION(4);

    companion object {
        @JvmStatic
        fun fromValue(value: Int) = values().first { it.value == value }
    }
}

// 人物类型枚举
enum class PersonType(val value: Int) {
    INDIVIDUAL(1),
    CORPORATION(2),
    ASSOCIATION(3);

    companion object {
        @JvmStatic
        fun fromValue(value: Int) = values().first { it.value == value }
    }
}

// 职业枚举
enum class PersonCareer {
    producer, mangaka, artist, seiyu, writer, illustrator, actor
}

// 图片模型
data class PersonImages(
    val large: String?,
    val medium: String?,
    val small: String?,
    val grid: String?
)

// 人物模型
data class Person(
    val id: Int,
    val name: String,
    val type: PersonType,
    val career: List<PersonCareer>,
    val images: PersonImages?,
    @SerializedName("short_summary") val shortSummary: String,
    val locked: Boolean
)

// 关联角色模型
data class BangumiCharacter(
    val id: Int,
    val name: String,
    val type: CharacterType,
    val images: PersonImages?,
    val relation: String,
    val actors: List<Person> = emptyList()
)
