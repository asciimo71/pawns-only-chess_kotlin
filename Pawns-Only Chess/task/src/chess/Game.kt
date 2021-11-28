package chess

import kotlin.math.abs

class Player(val name: String, val color: PawnColor) {
    var pawns: Int = 8

    override fun toString(): String {
        return name
    }
}

class EnPassantPosition(
    val gameClock: Int,
    val enPassantTargetPosition: GameCoordinate,
    val pawnPosition: GameCoordinate
) {
    override fun toString(): String = "@[${gameClock}]: pawn at $pawnPosition can " +
            "be taken at $enPassantTargetPosition in round ${gameClock + 1}"
}

class Game(val playerA: String, val playerB: String, val board: ChessBoard) {

    private var winner: Player? = null
    private var lastEnPassantPosition: EnPassantPosition? = null
    private var players: Array<Player?> = Array(2) { null }
    private var currentPlayer: Int = -1
    private var currentOpponent: Int = -1
    private val moveParser = MoveParser()

    private var gameClock: Int = 0

    // move to gamestate?
    private var gameFinished: Boolean = false

    fun start() {
        initPlayers()

        currentPlayer = selectStartingPlayer()
        currentOpponent = selectStartingOpponent()
        gameClock = 0

        // println("Player ${players[currentPlayer]} begins")

        println(board)

        while (gameRunning()) {
            val move = readPlayersMove()
            when (move?.command) {
                Move.GameCommand.MOVE -> if (executeMove(move)) {
                    println(board)
                    nextPlayer()
                }
                Move.GameCommand.EXIT -> exitGame()
                else -> println("Invalid Input")
            }
        }
        if(winner != null ) {
            val message = if (winner == GameContants.MR_STALEMATE) "Stalemate!"
            else String.format("%s wins!", winner!!.color.longName)

            println(message)
        }

        println("Bye!")
    }

    private fun executeMove(move: Move): Boolean {
        val player = players[currentPlayer]!!
        val pawn = board.pawnWithColorAtCoordinates(player.color, move.from)

        if (pawn == null) {
            println("No ${player.color.name.lowercase()} pawn at ${move.from}")
            return false
        }

        val maxDistance = if (pawn.hasMoved) 1 else 2
        val direction = directionOf(player)
        val rankDistance = direction * move.rankDistance()
        val fileDistance = move.fileDistance()

        var moved = false

        if (fileDistance == 0 && rankDistance in 1..maxDistance) {
            val pawnAtTarget = board.pawnAtCoordinates(move.to)
            if (pawnAtTarget == null && board.noPawnBlockingRank(move)) {
                board.movePawn(move)
                pawn.hasMoved = true
                if (rankDistance == 2)
                    recordEnPassantAvailable(move.to, direction)
                else
                    clearEnPassantPosition()
                moved = true
            }
        } else if (abs(fileDistance) == 1 && rankDistance == 1) {
            val opponent = players[currentOpponent]!!
            val victim = board.pawnAtCoordinates(move.to)

            if (victim != null && victim.color == opponent.color) {
                board.movePawn(move)
                opponent.pawns--
                moved = true
            } else if (victim == null) {
                enPassantAvailableAt(move.to)?.let {
                    board.movePawn(move)
                    board.clearPawnAt(it.pawnPosition)
                    opponent.pawns--
                    moved = true
                }
            }

            if (moved) clearEnPassantPosition()
        }

        if (!moved) println("Invalid Input")
        return moved
    }

    private fun directionOf(player: Player) = player.color.direction

    private fun clearEnPassantPosition() {
        lastEnPassantPosition = null
    }

    private fun enPassantAvailableAt(position: GameCoordinate): EnPassantPosition? {
        return if (lastEnPassantPosition?.enPassantTargetPosition == position) {
            lastEnPassantPosition
        } else null
    }

    private fun recordEnPassantAvailable(pawnPosition: GameCoordinate, direction: Int) {
        val enPassantTargetPosition =
            GameCoordinate(
                pawnPosition.file,
                pawnPosition.rank - direction
            )

        val enPassantPosition = EnPassantPosition(
            gameClock,
            enPassantTargetPosition,
            pawnPosition
        )

        lastEnPassantPosition = enPassantPosition
    }

    private fun exitGame() {
        gameFinished = true
    }

    fun gameRunning(): Boolean {
        if (!gameFinished) checkEndOfGame()
        return !gameFinished
    }

    private fun checkEndOfGame() {
        val current = players[currentPlayer]!!
        val opponent = players[currentOpponent]!!

        var gameOver = false
        if (!gameOver && playerHasNoPawns(current)) {
            this.winner = opponent
            gameOver = true
        }

        if (!gameOver && playerHasDame(opponent)) {
            this.winner = opponent
            gameOver = true
        }

        if (!gameOver && !playerCanMove(current)) {
            this.winner = GameContants.MR_STALEMATE
            gameOver = true
        }

        gameFinished = gameOver
    }

    private fun playerCanMove(player: Player): Boolean {
        for (rank in 1..8) {
            val aPawnCanMove = GameContants.VALID_FILES
                .map {
                    val gameCoordinate = GameCoordinate(it, rank)
                    return@map if (board.pawnWithColorAtCoordinates(
                            player.color,
                            gameCoordinate
                        ) != null
                    ) {
                        gameCoordinate
                    } else {
                        null
                    }
                } // all coordinates of Player's pawns
                .filterNotNull()
                .filter { position ->
                    // check move with distance 1, we need no check on dist == 2 because,
                    // if I cant move 1, I cant move 2.
                    val moveToPos = GameCoordinate(
                        position.file, position.rank + player
                            .color.direction
                    )
                    if (board.pawnAtCoordinates(moveToPos) == null) return@filter true // can
                    // move

                    // check capture left/right
                    if (position.file > 'a') {
                        val capLeftPos = GameCoordinate(
                            position.file - 1, position.rank + player
                                .color.direction
                        )
                        if (board.pawnAtCoordinates(capLeftPos) == null) return@filter false
                        // noone to capture
                        if (board.pawnWithColorAtCoordinates(
                                player.color,
                                capLeftPos
                            ) == null
                        ) return@filter true // not my color at coordinates -> can capture
                    }

                    if (position.file < 'h') {
                        val capRightPos = GameCoordinate(
                            position.file + 1, position.rank + player
                                .color.direction
                        )
                        if (board.pawnAtCoordinates(capRightPos) == null) return@filter false
                        // noone to capture
                        if (board.pawnWithColorAtCoordinates(
                                player.color,
                                capRightPos
                            ) == null
                        ) return@filter true // not my color at coordinates -> can capture
                    }

                    // check enPassant Capture
                    if (lastEnPassantPosition != null) {
                        if (player.color.direction * (lastEnPassantPosition!!
                                .enPassantTargetPosition
                                .rank -
                                    position
                                        .rank) == 1 && abs(
                                lastEnPassantPosition!!
                                    .enPassantTargetPosition.file - position.file
                            ) == 1
                        ) {
                            // can reach == move to enPassantTargetPosition
                            return@filter true
                        }
                    }
                    return@filter false
                } // list of all pawns that can move
                .isNotEmpty() // true, if any pawn can move
            if( aPawnCanMove ) return true
        }

        return false
    }

    private fun playerHasDame(player: Player): Boolean {
        return board.rank(player.color.winningRank).filterValues {
            it.color == player.color
        }.isNotEmpty()
    }

    private fun playerHasNoPawns(player: Player): Boolean = player.pawns == 0

    /**
     * this rotates player-array index by modulo. Could also swap values.
     */
    private fun nextPlayer() {
        currentPlayer = (currentPlayer + 1) % 2
        currentOpponent = (currentOpponent + 1) % 2
        gameClock++
    }

    private fun readPlayersMove(): Move? {
        println("${players[currentPlayer]}'s turn:")
        print("> ")
        val moveStr = readLine()!!
        return moveParser.parse(moveStr)
    }

    private fun initPlayers() {
        players[0] = Player(playerA, PawnColor.WHITE)
        players[1] = Player(playerB, PawnColor.BLACK)
    }

    private fun selectStartingPlayer(): Int = 0 //(Math.random() * 1000).roundToInt() % 2
    private fun selectStartingOpponent(): Int = 1


}

