package wgslsmith.wgslgenerator.ast.statement

import wgslsmith.wgslgenerator.ast.Symbol
import wgslsmith.wgslgenerator.ast.Type
import wgslsmith.wgslgenerator.ast.WGSLType
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.ExpressionGenerator
import wgslsmith.wgslgenerator.ast.statement.AssignmentGenerator.assignmentArithmeticForms
import wgslsmith.wgslgenerator.ast.statement.AssignmentGenerator.assignmentBitForms
import wgslsmith.wgslgenerator.ast.statement.AssignmentGenerator.assignmentBitShiftForms
import wgslsmith.wgslgenerator.ast.statement.AssignmentGenerator.assignmentIncDecForms
import wgslsmith.wgslgenerator.ast.statement.AssignmentGenerator.assignmentLogicalForms
import wgslsmith.wgslgenerator.tables.SymbolTable
import wgslsmith.wgslgenerator.utils.ConfigurationManager
import wgslsmith.wgslgenerator.utils.PseudoNumberGenerator

interface AssignmentForms {
    val op: String
}

// likely need a safe math wrapper here!
internal enum class AssignmentArithmeticForms(override val op: String) : AssignmentForms {
    ADD("+="),
    DIV("/="),
    MINUS("-="),
    MOD("%="),
    MULT("*=");
}

internal enum class AssignmentIncDecForms(override val op: String) : AssignmentForms {
    DECREMENT("--"),
    INCREMENT("++");
}

internal enum class AssignmentBitForms(override val op: String) : AssignmentForms {
    BIT_OR("|="),
    BIT_AND("&="),
    BIT_EXCLUSIVE_OR("^=");
}

internal enum class AssignmentBitShiftForms(override val op: String) : AssignmentForms {
    SHIFT_LEFT(">>="),
    SHIFT_RIGHT("<<=");
}

internal enum class AssignmentLogicalForms(override val op: String) : AssignmentForms {
    OR("|="),
    AND("&=");
}

internal enum class AssignmentStandardForms(override val op: String) : AssignmentForms {
    ASSIGN("=");
}

internal object AssignmentGenerator {
    val assignmentArithmeticForms = ArrayList<AssignmentForms>(AssignmentArithmeticForms.values().asList())
    val assignmentBitForms = ArrayList<AssignmentForms>(AssignmentBitForms.values().asList())
    val assignmentBitShiftForms = ArrayList<AssignmentForms>(AssignmentBitShiftForms.values().asList())
    val assignmentIncDecForms = ArrayList<AssignmentForms>(AssignmentIncDecForms.values().asList())
    val assignmentLogicalForms = ArrayList<AssignmentForms>(AssignmentLogicalForms.values().asList())
}

internal class AssignmentStatement : Statement() {
    private lateinit var lhs: Symbol
    private lateinit var rhs: Expression
    private lateinit var assignmentForm: AssignmentForms
    private lateinit var type: WGSLType
    private var declaredNewSymbol = false

    override fun generate(symbolTable: SymbolTable, depth: Int): AssignmentStatement {
        rhs = ExpressionGenerator.getExpressionWithoutReturnType(symbolTable, 0)
        type = rhs.returnType

        val assignmentForms: ArrayList<AssignmentForms> = when (type.type) {
            Type.BOOL  -> assignmentLogicalForms
            Type.FLOAT -> assignmentArithmeticForms
            Type.INT   -> ArrayList(
                assignmentArithmeticForms + assignmentBitForms + assignmentIncDecForms
            )
            Type.UNINT -> ArrayList(
                assignmentArithmeticForms + assignmentBitForms + assignmentBitShiftForms + assignmentIncDecForms
            )
        }

        if (assignmentForms.size > 1 && symbolTable.hasWriteableOf(type) && PseudoNumberGenerator.evaluateProbability(
                ConfigurationManager.probabilityGenerateCompoundAssignment
            )) {
            // generate compound assignment
            lhs = symbolTable.getRandomWriteableExistingSymbol(type)

            val assignmentFormIndex = PseudoNumberGenerator.getRandomIntInRange(
                0, assignmentForms.size
            )
            assignmentForm = assignmentForms[assignmentFormIndex]
        } else {
            // generate standard "=" assignment
            if (PseudoNumberGenerator.evaluateProbability(ConfigurationManager.probabilityAssignToAnonymous)) {
                lhs = Symbol("_", type)
            } else {
                lhs = symbolTable.getRandomWriteableSymbol(type)
                if (lhs.getName() == "") {
                    lhs = symbolTable.declareNewSymbol(type)
                    declaredNewSymbol = true
                }
            }
            assignmentForm = AssignmentStandardForms.ASSIGN
        }

        return this
    }

    override fun getTabbedLines(): ArrayList<String> {
        if (assignmentForm is AssignmentIncDecForms) {
            return arrayListOf("$lhs${assignmentForm.op};")
        }

        val varDeclaration = if (declaredNewSymbol) "var " else ""
        val typeDeclaration = if (declaredNewSymbol && PseudoNumberGenerator.evaluateProbability(
                ConfigurationManager.probabilityOmitTypeFromDeclaration
            )) {
            ": ${type.type.wgslType}"
        } else ""

        return arrayListOf("$varDeclaration$lhs$typeDeclaration ${assignmentForm.op} $rhs;")
    }
}