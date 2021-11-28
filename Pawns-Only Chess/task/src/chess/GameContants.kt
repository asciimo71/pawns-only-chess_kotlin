package chess

object GameContants {
    const val VALID_FILES = "abcdefgh"
    const val VALID_RANKS = "12345678"
    val EMPTY_COORDINATE = GameCoordinate('x', -1)
    val MR_STALEMATE = Player("stalemate", PawnColor.EMPTY)
}