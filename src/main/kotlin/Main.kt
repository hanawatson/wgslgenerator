package wgslsmith.wgslgenerator

import wgslsmith.wgslgenerator.ast.Shader
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

fun main(args: Array<String>) {
    PseudoNumberGenerator.initializeWithoutSeed()

    /*val symbolTable = SymbolTable()
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    symbolTable.addNonWriteableSymbol(Symbol("betterer", WGSLType.INT))
    symbolTable.addSymbol(Symbol("better", WGSLType.INT))
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomSymbol(WGSLType.INT).getName())
    println("-------------")
    println(symbolTable.getRandomWriteableSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomWriteableSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomWriteableSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomWriteableSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomWriteableSymbol(WGSLType.INT).getName())
    println(symbolTable.getRandomWriteableSymbol(WGSLType.INT).getName())*/

    val shader = Shader().generate()
    println(shader)
}