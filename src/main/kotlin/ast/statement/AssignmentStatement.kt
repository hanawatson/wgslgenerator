package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.expression.*
import wgslsmith.wgslgenerator.ast.scalarIntType
import wgslsmith.wgslgenerator.ast.scalarUnIntType
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.CNFG
import wgslsmith.wgslgenerator.utils.PRNG

internal class AssignmentStatement : Statement() {
    private lateinit var lhs: Symbol
    private lateinit var rhs: Expression
    private lateinit var type: WGSLType
    private var declaredNewSymbol = false
    private var compoundBinaryOperator = ""

    override lateinit var stat: Stat

    override fun generate(symbolTable: SymbolTable, stat: Stat, depth: Int): AssignmentStatement {
        this.stat = stat

        if (stat !is AssignStat) {
            throw Exception("Failure to validate AssignStat during AssignStatement generation!")
        }

        if (stat is AssignCompoundStat) {
            val exprEquivalent = when (stat) {
                AssignCompoundStat.DECREMENT       -> BinaryArithmeticMatrixNumericExpr.MINUS
                AssignCompoundStat.INCREMENT       -> BinaryArithmeticMatrixNumericExpr.ADD
                AssignCompoundStat.BINARY_OPERATOR -> PRNG.getRandomExprFrom(compoundAssignableExprs)
            }
            type = when (stat) {
                AssignCompoundStat.DECREMENT,
                AssignCompoundStat.INCREMENT       -> PRNG.getRandomTypeFrom(
                    arrayListOf(scalarIntType, scalarUnIntType)
                )
                AssignCompoundStat.BINARY_OPERATOR -> {
                    compoundBinaryOperator = "${exprEquivalent.operator}="
                    val exprType = ExprTypes.typeOf(exprEquivalent)
                    PRNG.getRandomTypeFrom(exprType.types)
                }
            }

            val binaryExpressionEquivalent = BinaryExpression().generate(
                symbolTable, type, exprEquivalent, 0
            )

            rhs = binaryExpressionEquivalent.getRHS()

            // temporary code to handle lack of implementation in naga of mixed operands during compound assignment
            if (rhs.returnType != type) {
                rhs = IdentityZeroValExpression().generate(symbolTable, type, IdentityUniversalExpr.ZERO_VALUE, 0)
            }
        } else {
            rhs = ExpressionGenerator.getExpressionWithoutReturnType(symbolTable, 0)
            type = rhs.returnType
        }

        // check if the selected compound statement can be used, and revert to simple assignment if not
        if (stat is AssignCompoundStat && !symbolTable.hasWriteableOf(type)) {
            this.stat = AssignEqStat.ASSIGN_SIMPLE
            type = rhs.returnType
        }

        if (this.stat is AssignCompoundStat) {
            lhs = symbolTable.getRandomWriteableExistingSymbol(type)
        } else {
            /*if (stat == AssignEqStat.ASSIGN_PHONY) {
                lhs = Symbol("_", type)
            } else {*/
            lhs = symbolTable.getRandomWriteableSymbol(type)
            if (lhs.name == "") {
                lhs = symbolTable.declareNewSymbol(type)
                declaredNewSymbol = true
            }
            //}
        }

        return this
    }

    override fun getTabbedLines(): ArrayList<String> {
        if (stat != AssignCompoundStat.BINARY_OPERATOR && lhs.type != rhs.returnType) {
            print("")
        }
        val operator = if (stat == AssignCompoundStat.BINARY_OPERATOR) {
            compoundBinaryOperator
        } else {
            (stat as AssignStat).operator
        }
        if (stat == AssignCompoundStat.DECREMENT || stat == AssignCompoundStat.INCREMENT) {
            return arrayListOf("$lhs$operator;")
        }

        val varDeclaration = if (declaredNewSymbol) "var " else ""
        val typeDeclaration = if (declaredNewSymbol &&
            PRNG.evaluateProbability(CNFG.probabilityOmitTypeFromDeclaration)
        ) {
            ": $type"
        } else ""

        return arrayListOf("$varDeclaration$lhs$typeDeclaration $operator $rhs;")
    }
}