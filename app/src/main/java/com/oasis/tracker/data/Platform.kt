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

    val NES = PlatformDef("nes", "NES", "NES")
    val SNES = PlatformDef("snes", "SNES", "SNES")
    val N64 = PlatformDef("n64", "Nintendo 64", "N64")
    val GAMECUBE = PlatformDef("gamecube", "GameCube", "GC")
    val WII = PlatformDef("wii", "Wii", "WII")
    val WIIU = PlatformDef("wiiu", "Wii U", "WIIU")
    val GAMEBOY = PlatformDef("gameboy", "Game Boy / Color", "GB")
    val GBA = PlatformDef("gba", "Game Boy Advance", "GBA")
    val NDS = PlatformDef("nds", "Nintendo DS", "NDS")
    val N3DS = PlatformDef("n3ds", "Nintendo 3DS", "3DS")
    val PS1 = PlatformDef("ps1", "PlayStation", "PS1")
    val PS2 = PlatformDef("ps2", "PlayStation 2", "PS2")
    val PS3 = PlatformDef("ps3", "PlayStation 3", "PS3")
    val PSVITA = PlatformDef("psvita", "PlayStation Vita", "VITA")
    val PSP = PlatformDef("psp", "PSP", "PSP")
    val XBOX = PlatformDef("xbox", "Xbox", "XBOX")
    val XBOX360 = PlatformDef("xbox360", "Xbox 360", "X360")
    val GENESIS = PlatformDef("genesis", "Sega Genesis", "GEN")
    val SATURN = PlatformDef("saturn", "Sega Saturn", "SAT")
    val DREAMCAST = PlatformDef("dreamcast", "Dreamcast", "DC")
    val ATARI2600 = PlatformDef("atari2600", "Atari 2600", "ATARI")

    val MODERN_PLATFORMS = listOf(PC, PS5, PS4, XSX, XONE, SWITCH, SWITCH2)

    val RETRO_PLATFORMS = listOf(
        NES, SNES, N64, GAMECUBE, WII, WIIU,
        GAMEBOY, GBA, NDS, N3DS,
        PS1, PS2, PS3, PSVITA, PSP,
        XBOX, XBOX360,
        GENESIS, SATURN, DREAMCAST,
        ATARI2600
    )

    val ALL = MODERN_PLATFORMS + RETRO_PLATFORMS

    fun byId(id: String): PlatformDef = ALL.firstOrNull { it.id == id } ?: PC
}
