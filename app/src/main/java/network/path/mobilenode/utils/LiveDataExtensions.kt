package network.path.mobilenode.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, block: (T) -> Unit) {
    observe(lifecycleOwner, Observer { block(it) })
}

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, block: () -> Unit) {
    observe(lifecycleOwner, Observer { block() })
}