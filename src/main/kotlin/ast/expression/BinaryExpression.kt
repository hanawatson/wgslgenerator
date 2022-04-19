package wgslsmith.wgslgenerator.ast.expression

import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.WGSLVectorType
import wgslsmith.wgslgenerator.ast.scalarIntType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class BinaryExpression : Expression() {
    private lateinit var lhs: Expression
    private lateinit var rhs: Expression
    private lateinit var lhsType: WGSLType
    private lateinit var rhsType: WGSLType

    override lateinit var returnType: WGSLType
    override lateinit var expr: Expr
    override var numberOfParentheses = PRNG.getNumberOfParentheses()

    override fun generate(symbolTable: SymbolTable, returnType: WGSLType, expr: Expr, depth: Int): BinaryExpression {
        this.returnType = returnType
        this.expr = expr

        lhsType = returnType
        rhsType = returnType
        if (returnType is WGSLVectorType && expr is BinaryArithmeticExpr
            && PRNG.evaluateProbability(CNFG.probabilityReplaceVectorBinaryOperandWithScalar)) {
            if (PRNG.getRandomBool()) {
                lhsType = returnType.componentType
            } else {
                rhsType = returnType.componentType
            }
        }

        lhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, lhsType, depth + 1)
        rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, rhsType, depth + 1)

        return this
    }

    fun generateModWithIntExpressions(symbolTable: SymbolTable, lhs: Expression, rhs: Expression): BinaryExpression {
        this.generate(symbolTable, scalarIntType, BinaryArithmeticExpr.MOD, CNFG.maxExpressionRecursion)
        this.lhs = lhs
        this.rhs = rhs

        return this
    }

    fun getRHS(): Expression {
        return rhs
    }

    override fun toString(): String {
        val lhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(lhs)
        val rhsString = ExpressionGenerator.getUsefulParenthesizedExpressionString(rhs)

        var binaryExpressionString = "$lhsString ${expr.operator} $rhsString"

        if (CNFG.useExcessParentheses) {
            for (i in 1..numberOfParentheses) {
                binaryExpressionString = "($binaryExpressionString)"
            }
        }

        return binaryExpressionString
    }
}