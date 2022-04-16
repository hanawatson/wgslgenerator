package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.expression.*
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

internal class AssignmentStatement : Statement() {
    override lateinit var stat: Stat
    private lateinit var lhs: Symbol
    private lateinit var rhs: Expression
    private lateinit var type: WGSLType
    private var declaredNewSymbol = false

    override fun generate(symbolTable: SymbolTable, stat: Stat, depth: Int): AssignmentStatement {
        this.stat = stat

        if (stat !is AssignStat) {
            throw Exception("Failure to validate AssignStat during AssignStatement generation!")
        }

        if (stat is AssignCompoundStat) {
            // should be updated to use probability method once implemented in similar Expression code!
            val exprEquivalent: BinaryExpr = when (stat) {
                is AssignArithmeticCompoundStat    -> BinaryArithmeticExpr.valueOf(stat.name)
                is AssignBitCompoundStat           -> BinaryBitExpr.valueOf(stat.name)
                is AssignLogicalCompoundStat       -> BinaryLogicalExpr.valueOf(stat.name)
                AssignIncDecCompoundStat.DECREMENT -> BinaryArithmeticExpr.MINUS
                AssignIncDecCompoundStat.INCREMENT -> BinaryArithmeticExpr.ADD
                else                               -> throw Exception(
                    "Attempt to generate AssignCompoundStat with uncategorized operator ${stat.operator}!"
                )
            }
            val exprType = ExprTypes.typeOf(exprEquivalent)
            val typeIndex = PseudoNumberGenerator.getRandomIntInRange(0, exprType.exprTypes.size)
            type = exprType.exprTypes[typeIndex]

            rhs = ExpressionGenerator.getExpressionWithReturnType(symbolTable, type, 0)
        } else {
            rhs = ExpressionGenerator.getExpressionWithoutReturnType(symbolTable, 0)
            type = rhs.returnType
        }

        // check if the selected compound statement can be used, and revert to simple assignment if not
        if (stat is AssignCompoundStat && !symbolTable.hasWriteableOf(type)) {
            this.stat = AssignEqStat.ASSIGN_SIMPLE
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
        if (stat is AssignIncDecCompoundStat) {
            return arrayListOf("$lhs${(stat as AssignStat).operator};")
        }

        val varDeclaration = if (declaredNewSymbol) "var " else ""
        val typeDeclaration = if (declaredNewSymbol && PseudoNumberGenerator.evaluateProbability(
                ConfigurationManager.probabilityOmitTypeFromDeclaration
            )) {
            ": ${type.type.wgslType}"
        } else ""

        return arrayListOf("$varDeclaration$lhs$typeDeclaration ${(stat as AssignStat).operator} $rhs;")
    }
}