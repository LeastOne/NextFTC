package org.firstinspires.ftc.threedrd.nextftc.bindings

import dev.nextftc.bindings.BindingManager
import dev.nextftc.core.components.Component

object BindingsComponent : Component {
    override fun preInit() = BindingManager.reset()

    override fun preWaitForStart() = BindingManager.update()

    override fun preUpdate() = BindingManager.update()

    override fun postStop() = BindingManager.reset()
}
