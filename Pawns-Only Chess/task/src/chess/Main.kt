package chess

fun readUserInput(prompt: String): String {
    println(prompt)
    print("> ")
    return readLine()!!
}

fun main() {
    val game: Game

    println(" Pawns-Only Chess")

    val playerA = readUserInput("First Player's name:")
    val playerB = readUserInput("Second Player's name:")

    game = Game(playerA, playerB, ChessBoard())

    game.start()
}