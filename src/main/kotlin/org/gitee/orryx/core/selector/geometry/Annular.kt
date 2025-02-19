package org.gitee.orryx.core.selector.geometry

import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector
import org.gitee.orryx.core.parser.StringParser
import org.gitee.orryx.core.selector.ISelectorGeometry
import org.gitee.orryx.core.targets.ITarget
import org.gitee.orryx.utils.getParameter
import org.gitee.orryx.utils.isInRound
import org.gitee.orryx.utils.read
import org.gitee.orryx.utils.toTarget
import taboolib.common.platform.function.adaptLocation
import taboolib.module.effect.createCircle
import taboolib.module.kether.ScriptContext
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 选中根据原点来定义的环状实体
 * ```
 * @annular 2 3 5
 * @annular min max high
 * ```
 */
object Annular : ISelectorGeometry {

    override val keys: Array<String>
        get() = arrayOf("annular")

    override fun getTargets(context: ScriptContext, parameter: StringParser.Entry): List<ITarget<*>> {
        val origin = context.getParameter().origin ?: return emptyList()
        val location = origin.location

        val min = parameter.read<Double>(0, 0.0)
        val max = parameter.read<Double>(1, 0.0)
        val high = parameter.read<Double>(2, 0.0)

        val livingEntities = origin.world.getNearbyEntities(origin.location, max, high, max)
        val list = mutableListOf<ITarget<*>>()

        livingEntities.forEach {
            if (it is LivingEntity) {
                val offset = sqrt(it.width.pow(2) * 2) / 2
                if (it.location.isInRound(
                        location,
                        (max + offset).coerceAtLeast(min)
                    ) && !it.eyeLocation.isInRound(
                        location,
                        (min - offset).coerceAtLeast(0.0).coerceAtMost(max)
                    ) && location.y in (location.y - high / 2)..(location.y + high / 2)
                ) {
                    list += it.toTarget()
                }
            }
        }
        return list
    }

    override fun aFrameShowLocations(context: ScriptContext, parameter: StringParser.Entry): List<taboolib.common.util.Location> {
        val origin = context.getParameter().origin?.location ?: return emptyList()
        val min = parameter.read<Double>(0, 0.0)
        val max = parameter.read<Double>(1, 0.0)
        val high = parameter.read<Double>(2, 0.0)

        val locations = mutableListOf<taboolib.common.util.Location>()

        fun circleMax(loc: Location) {
            locations.addAll(
                createCircle(
                    adaptLocation(loc),
                    max,
                    5.0,
                    0
                ).calculateLocations().map { it }
            )
        }
        fun circleMin(loc: Location) {
            locations.addAll(
                createCircle(
                    adaptLocation(loc),
                    min,
                    5.0,
                    0
                ).calculateLocations().map { it }
            )
        }

        circleMax(origin.clone().add(Vector(0.0, high / 2, 0.0)))
        circleMax(origin)
        circleMax(origin.clone().add(Vector(0.0, -high / 2, 0.0)))

        circleMin(origin.clone().add(Vector(0.0, high / 2, 0.0)))
        circleMin(origin)
        circleMin(origin.clone().add(Vector(0.0, -high / 2, 0.0)))

        return locations
    }


}
