import java.lang.System.`in`
import java.util.*

fun main(args: Array<String>) {
    val handler = Krypto()
    val sc = Scanner(`in`)
    var command: String
    while (true) {
        command = sc.nextLine()
        handler.handle(command)
    }
}
