package chess

data class GameCoordinate(val file: Char, val rank: Int) {
    override fun toString(): String {
        return "$file$rank"
    }
}


class GameCoordinateParser {
    fun parse(coordStr: String): GameCoordinate? {
        val file = coordStr[0]
        val rankC = coordStr[1]
        return if (isInputInRange(file, rankC)) {
            GameCoordinate(file, rankC.digitToInt())
        } else {
            null
        }
    }

    private fun isInputInRange(file: Char, rankC: Char) =
        GameContants.VALID_FILES.contains(file, true) && GameContants.VALID_RANKS.contains(rankC, true)
}