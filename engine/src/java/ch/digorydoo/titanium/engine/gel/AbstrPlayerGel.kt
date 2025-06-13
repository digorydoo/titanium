package ch.digorydoo.titanium.engine.gel

import ch.digorydoo.kutils.point.Point3f

/**
 * If Kotlin had intersection types, we could define IPlayerGelExtras as an interface and define AbstrPlayerGel as a
 * typealias AbstrPlayerGel = GraphicElement & IPlayerGelExtras. Unfortunately, Kotlin does not have this feature. The
 * Java way of doing this would require that GraphicElement derive from an interface IGraphicElement that repeats all
 * of its members, and then define IPlayerGel as an interface IPlayerGel: IGraphicElement, IPlayerGelExtras. I don't
 * like this, because repeating the members is verbose, and having both a GraphicElement and an IGraphicElement would
 * constantly cause confusion about which one to use. Adding another inheritance level is the best choice here even
 * though I generally consider multiple levels of inheritance as bad style that can lead to a C++ kind of mess.
 */
abstract class AbstrPlayerGel(initialPos: Point3f): GraphicElement(initialPos) {
    abstract val rotationPhi: Float
    abstract val allowActions: Boolean
}
