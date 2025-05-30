package de.readeckapp.domain.model

sealed interface Template {
    fun getTemplate(isSystemInDarkMode: Boolean): String
    data class SimpleTemplate(val template: String): Template {
        override fun getTemplate(isSystemInDarkMode: Boolean): String {
            return template
        }
    }
    data class DynamicTemplate(val light: String, val dark: String): Template {
        override fun getTemplate(isSystemInDarkMode: Boolean): String {
            return if (isSystemInDarkMode) {
                dark
            } else {
                light
            }
        }
    }
    companion object {
        const val LIGHT_TEMPLATE_FILE = "html_template_light.html"
        const val DARK_TEMPLATE_FILE = "html_template_dark.html"
    }
}