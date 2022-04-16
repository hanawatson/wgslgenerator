package wgslsmith.wgslgenerator

import wgslsmith.wgslgenerator.ast.Shader
import wgslsmith.wgslgenerator.utils.PRNG

fun main(args: Array<String>) {
    PRNG.initializeWithoutSeed()

    val shader = Shader().generate()
    println(shader)
}