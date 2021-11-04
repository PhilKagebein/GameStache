package com.example.videogamesearcher.models

data class GameNameResponseItem(
    val id: Int,
    val name: String,
    val platforms: List<Platform>

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameNameResponseItem

        if (id != other.id) return false
        if (name != other.name) return false
        if (platforms != other.platforms) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + platforms.hashCode()
        return result
    }

    override fun toString(): String {
        return "GameNameResponseItem(id=$id, name='$name', platforms=$platforms)"
    }


}