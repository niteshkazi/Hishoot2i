package template

import androidx.annotation.Keep
import common.ext.exhaustive
import template.Template.Default
import template.Template.Version1
import template.Template.Version2
import template.Template.Version3
import template.Template.VersionHtz
import java.text.Collator

@Keep
enum class TemplateComparator : Comparator<Template> {
    NAME_ASC {
        override val impl: (Template, Template) -> Int = { lhs, rhs ->
            collator.compare(lhs.name, rhs.name)
        }
    },
    NAME_DESC {
        override val impl: (Template, Template) -> Int = { lhs, rhs ->
            collator.compare(rhs.name, lhs.name)
        }
    },
    TYPE_ASC {
        override val impl: (Template, Template) -> Int = { lhs, rhs ->
            lhs.typeSort().compareTo(rhs.typeSort()).takeIf { it != 0 }
                ?: NAME_ASC.impl(lhs, rhs)
        }
    },
    TYPE_DESC {
        override val impl: (Template, Template) -> Int = { lhs, rhs ->
            rhs.typeSort().compareTo(lhs.typeSort()).takeIf { it != 0 }
                ?: NAME_ASC.impl(lhs, rhs)
        }
    },
    DATE_ASC {
        override val impl: (Template, Template) -> Int = { lhs, rhs ->
            lhs.installedDate.compareTo(rhs.installedDate).takeIf { it != 0 }
                ?: NAME_ASC.impl(lhs, rhs)
        }
    },
    DATE_DESC {
        override val impl: (Template, Template) -> Int = { lhs, rhs ->
            rhs.installedDate.compareTo(lhs.installedDate).takeIf { it != 0 }
                ?: NAME_ASC.impl(lhs, rhs)
        }
    };

    protected abstract val impl: (Template, Template) -> Int

    protected val collator: Collator by lazy { Collator.getInstance() }

    protected fun Template.typeSort() = when (this) {
        is Default -> 0
        is Version1 -> 1
        is Version2 -> 2
        is Version3 -> 3
        is VersionHtz -> 4
    }.exhaustive

    protected fun Template.isDefault() = this is Default

    override fun compare(lhs: Template, rhs: Template): Int =
        if (lhs.isDefault() || rhs.isDefault()) 1 else impl(lhs, rhs)
}
