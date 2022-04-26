package wgslsmith.wgslgenerator

import wgslsmith.wgslgenerator.ast.Shader
import wgslsmith.wgslgenerator.utils.ConfigParser
import wgslsmith.wgslgenerator.utils.PRNG
import java.io.File
import java.nio.charset.Charset

fun main(/*args: Array<String>*/) {
    PRNG.initializeWithoutSeed()
    ConfigParser()

    val shader = Shader().generate()
    println(shader)
    File("../tint/out/Debug/test.wgsl").writeText("$shader")

    val processTint = ProcessBuilder("./tint", "test.wgsl").directory(File("../tint/out/Debug")).start()
    processTint.inputStream.reader(Charset.defaultCharset()).use { it.readText() }
    processTint.errorStream.reader(Charset.defaultCharset()).use { println(it.readText()) }
}