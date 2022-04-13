package wgslsmith.wgslgenerator.utils

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.expression.BinOpArithmeticForms
import wgslsmith.wgslgenerator.ast.expression.UnOpArithmeticForms

internal object SafeWrapperManager {
    private val usedWrappers: ArrayList<String> = ArrayList()

    // should refactor when know more!
    fun getBinOpArithmeticSafeWrapper(expressionType: WGSLType, operationType: BinOpArithmeticForms): String {
        val safeWrapper = "safe_math_wrapper_${expressionType.type.wgslType}_${operationType.longOp}"
        usedWrappers.add(safeWrapper)
        return safeWrapper
    }

    fun getUnOpArithmeticSafeWrapper(expressionType: WGSLType, operationType: UnOpArithmeticForms): String {
        val safeWrapper = "safe_math_wrapper_${expressionType.type.wgslType}_${operationType.longOp}"
        usedWrappers.add(safeWrapper)
        return safeWrapper
    }
}