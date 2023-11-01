package no.danielzeller.depthoffield.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver

inline fun View.onGlobalLayout(crossinline func: () -> Unit) {
    val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            func()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }
    viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
}

fun ValueAnimator.interpolate(timeInterpolator: TimeInterpolator): ValueAnimator {
    interpolator = timeInterpolator
    return this
}

inline fun ValueAnimator.onUpdate(crossinline func: (value: Any) -> Unit): ValueAnimator {
    addUpdateListener { animation ->
        func(animation.animatedValue)
    }
    return this
}

fun ValueAnimator.delay(delay: Long): ValueAnimator {
    startDelay = delay
    return this
}

inline fun Animator.start(animations: ArrayList<Animator>):Animator {
    animations.add(this)
    this.start()
    return this
}

inline fun Animator.onEnd(crossinline func: () -> Unit): Animator {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            func()
        }
    })
    return this
}
