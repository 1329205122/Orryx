package org.gitee.orryx.core.profile

open class Flag<T: Any>(override val value: T, override val isPersistence: Boolean, override val timeout: Long) : IFlag {

    override val timestamp: Long = System.currentTimeMillis()

    override fun toString(): String {
        return "Flag(value=$value, isPersistence=$isPersistence, timeout=$timeout, timestamp=$timestamp)"
    }

}