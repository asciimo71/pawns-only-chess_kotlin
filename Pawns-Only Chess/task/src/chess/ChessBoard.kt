package chess

import kotlin.math.abs

class ChessBoard {
    private val board: Array<MutableMap<Char, Pawn>> = Array(8) { rank ->
        val files = mutableMapOf<Char, Pawn>()
        GameContants.VALID_FILES.forEach { file ->
            files[file] = Pawn(
                when (rank) {
                    1 -> PawnColor.WHITE
                    6 -> PawnColor.BLACK
                    else -> PawnColor.EMPTY
                }
            )
        }

        return@Array files
    }

    private val emptyField : Pawn = Pawn(PawnColor.EMPTY)

    private val separator: String = "   ${"+---".repeat(8)}+"
    private val filesLegend: String = GameContants.VALID_FILES.map { "  $it " }
        .joinToString("", "   ")

    private fun rankStr(rank: Int): String {
        var files = ""
        board[rank].keys.sorted().forEach { file ->
            files += "| ${board[rank][file]!!} "
        }
        return " ${rank + 1} ${files}|"
    }

    override fun toString(): String {
        val boardStr = StringBuilder()
            .appendLine(separator)

        for (rank in board.size - 1 downTo 0) {
            boardStr.appendLine(rankStr(rank))
                .appendLine(separator)
        }

        boardStr.appendLine(filesLegend)

        return boardStr.toString()
    }

    /**
     * return the pawn at position it the color matches, null otherwise
     */
    fun pawnWithColorAtCoordinates(color: PawnColor, position: GameCoordinate): Pawn? {
        val pawn = pawnAtCoordinates(position)
        return if (pawn?.color == color) pawn else null
    }

    fun pawnAtCoordinates(position: GameCoordinate): Pawn? {
        val pawn = getPawnAtCoordinates(position)
        return if (pawn.color == PawnColor.EMPTY) null else pawn
    }

    fun movePawn(move: Move) {
        val pawn = removePawnAtCoordinates(move.from)
        setPawnAtCoordinates(move.to, pawn)
    }

    private fun setPawnAtCoordinates(position: GameCoordinate, pawn: Pawn): Pawn? {
        val tmpPawn = pawnAtCoordinates(position)
        board[position.rank - 1][position.file] = pawn
        return tmpPawn
    }

    private fun removePawnAtCoordinates(position: GameCoordinate): Pawn {
        val tmpPawn = getPawnAtCoordinates(position)
        setPawnAtCoordinates(position, emptyField)
        return tmpPawn
    }

    private fun getPawnAtCoordinates(position: GameCoordinate) =
        board[position.rank - 1][position.file]!!

    fun noPawnBlockingRank(move: Move): Boolean {
        val rankDistance = move.rankDistance()
        return if (rankDistance == 0 || abs(rankDistance) == 1) {
            true
        } else if (rankDistance > 1) {
            pawnAtCoordinates(GameCoordinate(move.from.file, move.from.rank + 1)) == null
        } else {
            pawnAtCoordinates(GameCoordinate(move.from.file, move.from.rank - 1)) == null
        }
    }

    fun clearPawnAt(position: GameCoordinate) : Pawn? {
        return setPawnAtCoordinates(position, emptyField)
    }

    fun rank(rank: Int) = board[rank-1]
}