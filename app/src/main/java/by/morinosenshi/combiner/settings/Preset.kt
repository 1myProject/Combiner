package by.morinosenshi.combiner.settings

enum class Preset {
    ULTRAFAST, SUPERFAST, VERYFAST, FASTER, FAST, MEDIUM, SLOW, SLOWER, VERYSLOW, PLACEBO;

    override fun toString(): String {
        return this.name.lowercase()
    }

    companion object {
        fun getPreset(id: Int): Preset = entries.getOrNull(id) ?: MEDIUM
        val MAX = entries.size
        val DEFAULT_NAME = MEDIUM
        val DEFAULT = DEFAULT_NAME.ordinal
    }
}
