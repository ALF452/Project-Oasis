package com.oasis.tracker.data

/**
 * Fixed set of platforms shown as submenus on the main menu. Not stored in
 * Room since the list is static; games and log entries reference the id.
 */
data class PlatformDef(val id: String, val displayName: String, val glyph: String)

object Platforms {
    val PC = PlatformDef("pc", "PC", "PC")
    val PS5 = PlatformDef("ps5", "PlayStation 5", "PS5")
    val PS4 = PlatformDef("ps4", "PlayStation 4", "PS4")
    val XSX = PlatformDef("xsx", "Xbox Series X|S", "XSX")
    val XONE = PlatformDef("xone", "Xbox One", "XB1")
    val SWITCH = PlatformDef("switch", "Nintendo Switch", "NSW")
    val SWITCH2 = PlatformDef("switch2", "Nintendo Switch 2", "NS2")
    val RETRO = PlatformDef("retro", "Retro / Other", "RTR")

    val ALL = listOf(PC, PS5, PS4, XSX, XONE, SWITCH, SWITCH2, RETRO)

    fun byId(id: String): PlatformDef = ALL.firstOrNull { it.id == id } ?: RETRO
}
