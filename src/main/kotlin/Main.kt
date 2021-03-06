package wgslsmith.wgslgenerator

import com.sun.tools.javac.Main
import wgslsmith.wgslgenerator.ast.Shader
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.ConfigParser
import wgslsmith.wgslgenerator.utils.PRNG
import java.io.File

fun main(args: Array<String>) {
    var configFile: File? = null
    var output: String? = null
    var inputSeed: Long? = null

    val mandatoryArguments = 3
    for (arg in args.drop(mandatoryArguments)) {
        when {
            arg.startsWith("out:")  -> {
                output = arg.removePrefix("out:").ifBlank { null }
            }
            arg.startsWith("conf:") -> {
                configFile = if (arg.removePrefix("conf:").isBlank()) null else {
                    File(arg.removePrefix("conf:"))
                }
            }
            arg.startsWith("seed:") -> {
                inputSeed = if (arg.removePrefix("seed:").isBlank()) null else {
                    val seedAsLong = arg.removePrefix("seed:").toLongOrNull()
                    if (seedAsLong == null) {
                        System.err.println("Error: provided seed is not a valid Long.")
                        return
                    }
                    seedAsLong
                }
            }
        }
    }

    val randomizeOutputFile = args[0] == "1"
    CNFG.tintSafe = args[1] == "1"
    CNFG.nagaSafe = args[2] == "1"

    val config = try {
        if (configFile == null) {
            val defaultConfig = Main::class.java.classLoader.getResourceAsStream("defaultConfig.json")
            defaultConfig.reader().readText()
        } else {
            configFile.readText()
        }
    } catch (e: Exception) {
        System.err.println(
            "Error: failed to read config file. Internal error follows." +
                    "\n\n\t${e.message?.replace("\n", "\n\t")}"
        )
        return
    }

    try {
        PRNG.initialize(inputSeed)
        ConfigParser(config)
    } catch (e: Exception) {
        System.err.println(
            "Error: failed to initialize internal tools. Internal error follows." +
                    "\n\n\t${e.message?.replace("\n", "\n\t")}"
        )
        return
    }

    val outputText: String

    try {
        val shader = Shader()
        val seedComment = "// Random seed: ${PRNG.getSeed()}"
        outputText = "$seedComment\n\n$shader"
    } catch (e: Exception) {
        System.err.println(
            "Error: failed to generate WGSL shader. Internal error follows." +
                    "\n\n\t${e.message?.replace("\n", "\n\t")}"
        )
        return
    }

    try {
        if (output?.isNotBlank() == true || randomizeOutputFile) {
            val outputFull = if (randomizeOutputFile) {
                (output ?: "").removeSuffix(".wgsl") + PRNG.getSeed() + ".wgsl"
            } else {
                output
            }

            val outputFile = File(outputFull)
            outputFile.createNewFile()
            outputFile.writeText(outputText)
        } else {
            println(outputText)
        }
    } catch (e: Exception) {
        System.err.println(
            "Error: failed to write to output shader file. Internal error follows." +
                    "\n\n\t${e.message?.replace("\n", "\n\t")}"
        )
        return
    }
}