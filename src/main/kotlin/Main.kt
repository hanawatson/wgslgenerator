package wgslsmith.wgslgenerator

import wgslsmith.wgslgenerator.ast.Shader
import wgslsmith.wgslgenerator.utils.ConfigParser
import wgslsmith.wgslgenerator.utils.PRNG
import java.io.File
import java.nio.charset.Charset

fun main(/*args: Array<String>*/) {
    val seed: Long? = null
    if (seed != null) {
        PRNG.initializeWithSeed(seed)
    } else {
        PRNG.initializeWithoutSeed()
    }
    ConfigParser()

    val shader = Shader().generate()
    println(shader)
    println("Random seed: ${PRNG.seed}")

    File("../test.wgsl").writeText(shader.toString().replaceFirst("COMPUTE_STAGE", "stage(compute)"))
    val processTint =
        ProcessBuilder("./tint", "../../../test.wgsl"/*, "--validate"*/).directory(File("../tint/out/Debug")).start()
    processTint.inputStream.reader(Charset.defaultCharset()).use { it.readText() }
    processTint.errorStream.reader(Charset.defaultCharset()).use {
        val errorText = it.readText()
        if (errorText.isEmpty()) {
            println("Tint: OK")
        } else {
            println(errorText)
        }
    }


    File("../test.wgsl").writeText(shader.toString().replaceFirst("COMPUTE_STAGE", "compute"))
    try {
        val processNaga = ProcessBuilder("cargo", "run", "../test.wgsl").directory(File("../naga")).start()
        processNaga.inputStream.reader(Charset.defaultCharset()).use { it.readText() }
        processNaga.errorStream.reader(Charset.defaultCharset()).use {
            val errorText = it.readText()
            if (errorText.isEmpty()) {
                println("naga: OK")
            } else {
                println(errorText)
            }
        }
    } catch (e: Error) {
        println(e)
    }
}