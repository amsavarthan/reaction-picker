package dev.amsavarthan.reaction.picker.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a reaction item in the reaction picker.
 *
 * @param key The unique key of the reaction. Can be used to identify the reaction.
 * @param label The label of the reaction.
 * @param labelRes The string resource ID of the label of the reaction.
 * @param painterResource The drawable resource ID of the reaction.
 * @param imageVector The image vector of the reaction.
 *
 * Use from the overloads to create a reaction item.
 *
 * @author Amsavarthan Lv
 */
class Reaction private constructor(
    val key: Any? = null,
    val label: String? = null,
    @StringRes
    val labelRes: Int? = null,

    @DrawableRes
    val painterResource: Int? = null,
    val imageVector: ImageVector? = null,
) {

    constructor(
        key: Any? = null,
        label: String,
        @DrawableRes painterResource: Int,
    ) : this(
        key = key,
        label = label,
        labelRes = null,
        painterResource = painterResource,
        imageVector = null
    )

    constructor(
        key: Any? = null,
        label: String,
        imageVector: ImageVector,
    ) : this(
        key = key,
        label = label,
        labelRes = null,
        painterResource = null,
        imageVector = imageVector
    )

    constructor(
        key: Any? = null,
        @StringRes labelRes: Int,
        @DrawableRes painterResource: Int,
    ) : this(
        key = key,
        label = null,
        labelRes = labelRes,
        painterResource = painterResource,
        imageVector = null
    )

    constructor(
        key: Any? = null,
        @StringRes labelRes: Int,
        imageVector: ImageVector,
    ) : this(
        key = key,
        label = null,
        labelRes = labelRes,
        painterResource = null,
        imageVector = imageVector
    )

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + (label?.hashCode() ?: 0)
        result = 31 * result + (labelRes ?: 0)
        result = 31 * result + (painterResource ?: 0)
        result = 31 * result + (imageVector?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Reaction) return false

        if (key != other.key) return false
        if (label != other.label) return false
        if (labelRes != other.labelRes) return false
        if (painterResource != other.painterResource) return false
        if (imageVector != other.imageVector) return false

        return true
    }

    override fun toString(): String {
        return "Reaction(label=$label, labelRes=$labelRes, painterResource=$painterResource, imageVector=$imageVector)"
    }

}

fun List<Reaction>.getReactionByKey(key: Any): Reaction? {
    return firstOrNull { it.key == key }
}