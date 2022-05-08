package wgslsmith.wgslgenerator.ast

import wgslsmith.wgslgenerator.ast.expression.ExprTypes
import wgslsmith.wgslgenerator.ast.expression.Expression
import wgslsmith.wgslgenerator.ast.expression.IdentityLiteralExpression
import wgslsmith.wgslgenerator.ast.expression.compoundAssignableExprs
import wgslsmith.wgslgenerator.utils.CNFG
import java.lang.Integer.decode

enum class Type {
    // ANY type used only as a stand-in for generation purposes!
    ANY,

    BOOL,
    FLOAT,
    INT,
    UNINT,

    ARRAY,
    MAT,
    VEC;
}

internal interface WGSLType {
    val type: Type

    // return true if abstractType is a type using e.g. ANY such that the (class property)
    // type is included in it (i.e. with abstractType as an abstracted/generalised definition)
    fun isRepresentedBy(abstractType: WGSLType): Boolean
}

internal class WGSLScalarType(override val type: Type) : WGSLType {
    override fun isRepresentedBy(abstractType: WGSLType): Boolean {
        if (abstractType is WGSLScalarType) {
            return abstractType.type == Type.ANY || abstractType.type == type
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is WGSLScalarType) {
            return type == other.type
        }
        return false
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun toString(): String {
        return when (type) {
            Type.ANY   -> "ANY"
            Type.BOOL  -> "bool"
            Type.FLOAT -> "f32"
            Type.INT   -> "i32"
            Type.UNINT -> "u32"
            else       -> throw Exception(
                "Attempt to generate string representation of WGSLScalarType of unknown type $type!"
            )
        }
    }
}

internal class WGSLVectorType(val componentType: WGSLScalarType, val length: Int) : WGSLType {
    override val type = Type.VEC

    override fun isRepresentedBy(abstractType: WGSLType): Boolean {
        if (abstractType is WGSLVectorType) {
            val matchesComponentType = abstractType.componentType == abstractWGSLScalarType
                    || abstractType.componentType == componentType
            val matchesLength = abstractType.length == 0 || abstractType.length == length

            return matchesComponentType && matchesLength
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is WGSLVectorType) {
            return componentType == other.componentType && length == other.length
        }
        return false
    }

    override fun hashCode(): Int {
        var result = componentType.hashCode()
        result = 31 * result + length
        return result
    }

    override fun toString(): String {
        return "vec$length<$componentType>"
    }
}

internal class WGSLMatrixType(val componentType: WGSLScalarType, val width: Int, val length: Int) : WGSLType {
    override val type = Type.MAT

    override fun isRepresentedBy(abstractType: WGSLType): Boolean {
        if (abstractType is WGSLMatrixType) {
            val matchesComponentType = abstractType.componentType == abstractWGSLScalarType
                    || abstractType.componentType == componentType
            val matchesWidth = abstractType.width == 0 || abstractType.width == width
            val matchesLength = abstractType.length == 0 || abstractType.length == length

            return matchesComponentType && matchesWidth && matchesLength
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is WGSLMatrixType) {
            return componentType == other.componentType && width == other.width && length == other.length
        }
        return false
    }

    override fun hashCode(): Int {
        var result = componentType.hashCode()
        result = 31 * result + width
        result = 31 * result + length
        return result
    }

    override fun toString(): String {
        return "mat${width}x$length<$componentType>"
    }
}

internal class WGSLArrayType(val elementType: WGSLType, val elementCount: Expression, val nestedDepth: Int) : WGSLType {
    override val type = Type.ARRAY

    init {
        if (nestedDepth >= CNFG.maxArrayNestDepth) {
            throw Exception("Attempt to initialize WGSLArrayType of invalid nestedDepth $nestedDepth!")
        }
    }

    val elementCountValue: Int = when (elementCount) {
        is IdentityLiteralExpression -> {
            if (elementCount.returnType == scalarIntType || elementCount.returnType == scalarUnIntType) {
                decode(elementCount.literalValue)
            } else {
                throw Exception(
                    "Attempt to construct WGSLArrayType with non-integral literal elementCount $elementCount!"
                )
            }
        }
        // is ConstExpression will go here once implemented (or something similar)
        else                         -> {
            throw Exception("Attempt to construct WGSLArrayType with unknown elementCount $elementCount!")
        }
    }

    override fun isRepresentedBy(abstractType: WGSLType): Boolean {
        if (abstractType is WGSLArrayType) {
            val matchesElementType = abstractType.elementType == abstractWGSLScalarType
                    || abstractType.elementType == elementType
            val matchesElementCountValue = abstractType.elementCountValue == 0
                    || abstractType.elementCountValue == elementCountValue
            val canBeUsedAtDepth = abstractType.nestedDepth <= nestedDepth

            return matchesElementType && matchesElementCountValue && canBeUsedAtDepth
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is WGSLArrayType) {
            return elementType == other.elementType && elementCountValue == other.elementCountValue
        }
        return false
    }

    override fun hashCode(): Int {
        var result = elementType.hashCode()
        result = 31 * result + elementCountValue
        return result
    }

    override fun toString(): String {
        return "array<$elementType,$elementCount>"
    }
}

internal val scalarBoolType = WGSLScalarType(Type.BOOL)
internal val scalarFloatType = WGSLScalarType(Type.FLOAT)
internal val scalarIntType = WGSLScalarType(Type.INT)
internal val scalarUnIntType = WGSLScalarType(Type.UNINT)

internal val vectorBoolType = WGSLVectorType(scalarBoolType, 0)
internal val vectorFloatType = WGSLVectorType(scalarFloatType, 0)
internal val vectorIntType = WGSLVectorType(scalarIntType, 0)
internal val vectorUnIntType = WGSLVectorType(scalarUnIntType, 0)

internal val vector2FloatType = WGSLVectorType(scalarFloatType, 2)
internal val vector3FloatType = WGSLVectorType(scalarFloatType, 3)
internal val vector4FloatType = WGSLVectorType(scalarFloatType, 4)
internal val vector3UnIntType = WGSLVectorType(scalarUnIntType, 3)

internal val abstractWGSLScalarType = WGSLScalarType(Type.ANY)
internal val abstractWGSLVectorType = WGSLVectorType(abstractWGSLScalarType, 0)
internal val abstractWGSLMatrixType = WGSLMatrixType(abstractWGSLScalarType, 0, 0)
internal val abstractWGSLArrayType = WGSLArrayType(
    abstractWGSLScalarType, IdentityLiteralExpression(0), 0
)

internal val allTypes: ArrayList<WGSLType> = arrayListOf(
    abstractWGSLScalarType,
    abstractWGSLVectorType,
    abstractWGSLMatrixType,
    abstractWGSLArrayType
)
internal val scalarTypes: ArrayList<WGSLType> = arrayListOf(
    scalarBoolType,
    scalarFloatType,
    scalarIntType,
    scalarUnIntType
)
internal val numericScalarTypes: ArrayList<WGSLType> = arrayListOf(
    scalarFloatType,
    scalarIntType,
    scalarUnIntType
)
internal val numericVectorTypes: ArrayList<WGSLType> = arrayListOf(
    vectorFloatType,
    vectorIntType,
    vectorUnIntType
)
internal val numericTypes: ArrayList<WGSLType> = ArrayList(numericScalarTypes + numericVectorTypes)
internal val compositeTypes: ArrayList<WGSLType> = arrayListOf(
    abstractWGSLVectorType,
    abstractWGSLMatrixType,
    abstractWGSLArrayType
)
internal val matrixComponentTypes: ArrayList<WGSLType> = arrayListOf(
    scalarFloatType
)
internal val arrayElementTypes: ArrayList<WGSLType> = ArrayList(
    scalarTypes + arrayListOf(abstractWGSLVectorType, abstractWGSLMatrixType, abstractWGSLArrayType)
)

internal val compoundAssignableTypes: ArrayList<WGSLType> by lazy {
    val types: ArrayList<WGSLType> = compoundAssignableExprs.fold(ArrayList()) { acc, expr ->
        ArrayList(acc + ExprTypes.exprTypeOf(expr).types)
    }
    ArrayList(types.distinct())
}
internal val compoundAssignableConcreteTypes: ArrayList<WGSLType> by lazy {
    compoundAssignableTypes.fold(ArrayList()) { acc, type ->
        ArrayList(acc + getConcreteTypes(type))
    }
}

private val concreteTypesMap = HashMap<WGSLType, ArrayList<WGSLType>>()
internal fun getConcreteTypes(givenAbstractType: WGSLType): ArrayList<WGSLType> {
    val hashedConcreteTypes = concreteTypesMap[givenAbstractType]
    if (hashedConcreteTypes != null) {
        if (givenAbstractType is WGSLArrayType && givenAbstractType.nestedDepth + 1 >= CNFG.maxArrayNestDepth) {
            hashedConcreteTypes.removeAll { type -> type is WGSLArrayType }
        }
        return hashedConcreteTypes
    }

    val concreteTypes = when (givenAbstractType) {
        abstractWGSLScalarType -> scalarTypes
        is WGSLVectorType      -> {
            val concreteTypes = ArrayList<WGSLType>()
            val abstractTypes = arrayListOf(givenAbstractType)
            if (givenAbstractType.componentType == abstractWGSLScalarType) {
                for (abstractType in abstractTypes) {
                    for (type in scalarTypes) {
                        concreteTypes.add(WGSLVectorType(type as WGSLScalarType, abstractType.length))
                    }
                }
                abstractTypes.clear()
                abstractTypes.addAll(concreteTypes.map { concreteType -> concreteType as WGSLVectorType })
                concreteTypes.clear()
            }
            if (givenAbstractType.length == 0) {
                for (abstractType in abstractTypes) {
                    for (i in 2..4) {
                        concreteTypes.add(WGSLVectorType(abstractType.componentType, i))
                    }
                }
            }
            concreteTypes
        }
        is WGSLMatrixType      -> {
            val concreteTypes = ArrayList<WGSLType>()
            val abstractTypes = arrayListOf(givenAbstractType)
            if (givenAbstractType.componentType == abstractWGSLScalarType) {
                for (abstractType in abstractTypes) {
                    for (type in matrixComponentTypes) {
                        concreteTypes.add(
                            WGSLMatrixType(
                                type as WGSLScalarType, abstractType.width, abstractType.length
                            )
                        )
                    }
                }
                abstractTypes.clear()
                abstractTypes.addAll(concreteTypes.map { concreteType -> concreteType as WGSLMatrixType })
                concreteTypes.clear()
            }
            if (givenAbstractType.width == 0) {
                for (abstractType in abstractTypes) {
                    for (i in 2..4) {
                        concreteTypes.add(
                            WGSLMatrixType(
                                abstractType.componentType, i, abstractType.length
                            )
                        )
                    }
                }
                abstractTypes.clear()
                abstractTypes.addAll(concreteTypes.map { concreteType -> concreteType as WGSLMatrixType })
                concreteTypes.clear()
            }
            if (givenAbstractType.length == 0) {
                for (abstractType in abstractTypes) {
                    for (i in 2..4) {
                        concreteTypes.add(
                            WGSLMatrixType(
                                abstractType.componentType, abstractType.width, i
                            )
                        )
                    }
                }
            }
            concreteTypes
        }
        is WGSLArrayType       -> {
            val concreteTypes = ArrayList<WGSLType>()
            val abstractTypes = arrayListOf(givenAbstractType)
            if (givenAbstractType.elementType == abstractWGSLScalarType) {
                for (abstractType in abstractTypes) {
                    for (type in arrayElementTypes) {
                        val nestedDepth = givenAbstractType.nestedDepth + 1
                        if (nestedDepth in 1 until CNFG.maxArrayNestDepth) {
                            val nestedDepthType = if (type is WGSLArrayType) {
                                WGSLArrayType(type.elementType, type.elementCount, nestedDepth)
                            } else {
                                type
                            }
                            for (concreteNestedDepthType in getConcreteTypes(nestedDepthType)) {
                                concreteTypes.add(
                                    WGSLArrayType(
                                        concreteNestedDepthType, abstractType.elementCount, nestedDepth - 1
                                    )
                                )
                            }
                        }
                    }
                }
                abstractTypes.clear()
                abstractTypes.addAll(concreteTypes.map { concreteType -> concreteType as WGSLArrayType })
                concreteTypes.clear()
            }
            if (givenAbstractType.elementCountValue == 0) {
                for (abstractType in abstractTypes) {
                    for (i in 2..4) {
                        concreteTypes.add(
                            WGSLArrayType(
                                abstractType.elementType, IdentityLiteralExpression(i), givenAbstractType.nestedDepth
                            )
                        )
                    }
                }
            }
            concreteTypes
        }
        else                   -> arrayListOf(givenAbstractType)
    }

    concreteTypesMap[givenAbstractType] = concreteTypes
    return concreteTypes
}

internal val allConcreteTypes = allTypes.fold(ArrayList<WGSLType>()) { acc, type ->
    ArrayList(acc + getConcreteTypes(type))
}