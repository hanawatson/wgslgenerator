package wgslsmith.wgslgenerator

import wgslsmith.wgslgenerator.ast.Shader
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

fun main(args: Array<String>) {
    PseudoNumberGenerator.initializeWithoutSeed()

    val shader = Shader().generate()
    println(shader)
}