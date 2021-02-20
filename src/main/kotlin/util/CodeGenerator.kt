package util

class CodeGenerator {
    private val start = 1
    private val end = 9
    private var code = ""

    private fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    fun generate(): String{
        for (i in 1..6) code += rand(start, end).toString()

        return code
    }
}