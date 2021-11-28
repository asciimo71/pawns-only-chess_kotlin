package chess

enum class PawnColor(
    val symbol: Char,
    val direction: Int,
    val winningRank: Int,
    val longName: String
) {
    EMPTY(' ', 0, 0, ""),
    WHITE('W', 1, 8, "White"),
    BLACK('B', -1, 1, "Black")
}

class Pawn(val color: PawnColor) {
    var hasMoved: Boolean = false
    override fun toString(): String {
        return color.symbol.toString()
    }
}