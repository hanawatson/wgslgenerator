package wgslsmith.wgslgenerator

import wgslsmith.wgslgenerator.ast.Shader
import wgslsmith.wgslgenerator.utils.ConfigParser
import wgslsmith.wgslgenerator.utils.PRNG
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 3) {
        throw Exception("Invalid arguments provided! Expecting 3, received ${args.size}.")
    }

    val seed = args[2].toLongOrNull()
    if (seed == null && args[2] != "NULL") {
        throw Exception("Invalid seed value provided! The provided seed must be a valid Long.")
    }

    val outputText: String = try {
        if (seed != null) {
            PRNG.initializeWithSeed(seed)
        } else {
            PRNG.initializeWithoutSeed()
        }
        ConfigParser()

        val shader = Shader().generate()
        "// Random seed: ${PRNG.seed}\n" +
                shader.toString().replaceFirst("COMPUTE_STAGE", "stage(compute)")
    } catch (e: Exception) {
        println("Warning: internal error during shader creation!")
        "Error generating WGSL shader: ${e.message}"
    }


    if (args[0] != "NULL" && !args[0].endsWith(".wgsl")) {
        throw Exception("Invalid output file path provided! The provided path must end with the .wgsl extension.")
    }
    if (args[0] != "NULL" && File(args[0].substringBeforeLast("/")).isDirectory) {
        throw Exception("Invalid output file path provided! The provided path must be in an existing directory.")
    }
    var outputFilePath = if (args[0] == "NULL") "" else args[0]
    if (args[1] == "1") {
        outputFilePath = outputFilePath.replace(".wgsl", "")
        outputFilePath += "${PRNG.seed}.wgsl"
    }
    val outputFile = if (outputFilePath != "") {
        File(outputFilePath)
    } else {
        null
    }
    val outputFileCreated = outputFile?.createNewFile() ?: true
    if (!outputFileCreated) {
        throw Exception("Invalid output file path provided! A file must not already exist at the provided path.")
    }

    if (outputFile == null) {
        println(outputText)
    } else {
        outputFile.writeText(outputText)
    }
}