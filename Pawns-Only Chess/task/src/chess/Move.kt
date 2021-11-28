package chess

data class Move(
    val from: GameCoordinate,
    val to: GameCoordinate,
    val command: GameCommand = GameCommand.MOVE
) {
    fun rankDistance(): Int = to.rank - from.rank
    fun fileDistance(): Int = (to.file - 'a') - (from.file - 'a')

    enum class GameCommand {
        MOVE, EXIT
    }
}

class MoveParser() {
    val coordParser = GameCoordinateParser()

    private fun gameCommandOf(str: String): Move.GameCommand {
        return when (str.lowercase()) {
            "exit" -> Move.GameCommand.EXIT
            else -> Move.GameCommand.MOVE
        }
    }

    fun parse(moveStr: String): Move? {
        val gameCommand = gameCommandOf(moveStr)

        if (gameCommand == Move.GameCommand.MOVE) {
            if (moveStr.length != 4) return null

            val fromStr = moveStr.substring(0, 2)
            val toStr = moveStr.substring(2, 4)

            val from = coordParser.parse(fromStr)
            val to = coordParser.parse(toStr)

            return if (from == null || to == null) {
                null
            } else {
                Move(from, to)
            }
        } else {
            return Move(
                GameContants.EMPTY_COORDINATE,
                GameContants.EMPTY_COORDINATE,
                gameCommand
            )
        }
    }
}