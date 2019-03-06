package io.fabric8.launcher.creator.core.template.transformers

import io.fabric8.launcher.creator.core.template.Transformer

/**
 * A simple no-op transformer
 */
fun id(): Transformer {
    return { lines -> lines }
}
