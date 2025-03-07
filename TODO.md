# TODO

## Backlog

* Physics
    * orig pos
        * wenn xyOverlap, aber kein zOverlap, dann kann nur Fläche sein
        * wenn zOverlap, aber kein xyOverlap, dan kann nur Seite sein
        * Sonst Ecke;
            * wenn |speedXY| > |speedZ|, dann Seite
            * sonst Fläche
    * Gels gemäss Anteil gewichtet schieben (d.h. der erste weight1, der zweite DEN REST)

* Bugs
    * Railing 2 has strange lighting issue across instances (see level)
    * Sometimes keyboard strokes get lost (when frame takes too long, or stroke is hit too quickly)
    * Gels that are frozen in editor and/or dialogue must not fall through floor by gravity

* Performance
    * Don't use DYNAMIC_DRAW everywhere. Use STATIC_DRAW unless model changes often.
    * Don't reconfigure textures every render. Configure them once when creating them. Problem: Textures are shared...

* Editor menus should all have
    * a Back item that opens parent; and this should appear at the top
    * a Done item that closes menu without opening the parent; and this should be the ESC item

* Can I make all programs shared? Or should none be shared? All should be fine, because that's how bricks worked until
  recently...

* Shadows
    * Optimise shadow implementation
        * MacBook Pro (which has strong CPU and weak GPU) has similar FPS, slightly faster than Mac Mini
        * So, both CPU and GPU should probably be optimised

    * Make the shadow map more dense outside; or use multiple shadow maps
        * sampler2DArray to hold multiple layers
        * texture(depthMap, vec3(TexCoords, currentLayer))
        * glBindTexture(GL_TEXTURE_2D_ARRAY, lightDepthMaps)
        * glTexParameteri(GL_TEXTURE_2D_ARRAY, ...)
        * glTexImage3D
        * Use geometry shader like in https://learnopengl.com/Guest-Articles/2021/CSM

    * Improve shadows by blurring it; but not all materials (requires four shadow texel lookups)

* Rendering bricks more efficiently
    * BrickVolume should cover all tesselated data; subvolumes should only pick a part from it
    * BrickModelHolder should keep only the tesselated model data of each BrickModel
        * Tesselator would only called by BrickModelHolder (lazy)
        * BrickModelHolder could automatically flip and rotate models, no need to reimplement them
    * Use an alternative (simpler) tesselation when brick subvolume is far (e.g. thin walls become flat, etc.)
    * Positions, texCoords and normals (!!) should be de-duplicated using indices
    * NO sub-sub-volumes! Instead, BrickVolume should be a gel! No global brick volume! Can even rotate them...

* New brick shapes
    * Arcs, e.g. under a bridge
    * Double-bevel bricks (horiz bevel at 0.5m height, but no additional vertical division) for walls

* Pause the story time while the gameMenu or a dialog is active

* Fix incorrect normals in mesh.vsh: Normal = transpose(inverse(mat3(model))) * aNormal;

* Runs at 24fps on hp laptop... Is there anything to do about it?

* Both 16:9 and 16:10 should be fullscreen without any margin. 16:9 should be our FIXED_ASPECT_RATIO. On a 16:10
  monitor, the left and right side should be cropped. UI needs to get a margin.

* Deferred gel initialisation
    * Spawn pts do not return Gel; instead have a lambda onCreated
    * Gels have a val initConcurrently: (() -> Unit)? = null
    * If initConcurrently is null, spawn pt can call onCreated directly, otherwise call initConcurrently from coroutine
    * Renderers should always be loaded concurrently, so maybe just make it a non-null fun?
    * renderer should be lateinit; spawn pt should not add the gel to the layer until it has finished loading
    * onCreated needs to be called from main thread! Similar to handleLoadingScene. Generalise?

* Cutscenes
    * App.cutscene.start(MyCutscene())
    * Solange App.cutscene.active != null, ist Game im Cutscene-Modus (mit Filmbalken)
    * App.cutscene.animate() wird immer aufgerufen
    * cutscene.nextStep(proceed: (delaySeconds: Float) -> Unit, done: () -> Unit)
    * nextStep wird initial einmal aufgerufen
    * proceed darf pro Step nur einmal aufgerufen werden
    * CutsceneManager wartet delay ab und ruft erst dann nextStep erneut auf
    * Solange proceed nicht aufgerufen wird, bleibt Cutscene hängen, z.B. in einer Conversation

* Apply bone transformations
    * Whole object is rendered at once; no matrix stack needed
    * Each bone = 1 transformation
    * Poses are defined in game (NOT loaded from Blender Collada)
        * For each bone, define angles phi, rho, theta (and maybe relative length)
        * Interpolation between poses is done on those values, NOT on the transforms!
    * On CPU
        * Iterate through all bones and define the relative transforms
            * The relative transforms only change if the angles phi, rho, theta change (and maybe relative length)
            * So keep the old values and compare (avoids recomputing the cos, sin of the rotation)
        * Recursively iterate through bones and compute the cumulated transforms
            * We need to do this if ANY of the bones changed
            * The cumulated transform has all the parent transforms applied as well
            * The shader will only "see" the cumulated transforms
            * Ordering of transformations must match how the shader looks up transforms and weights
    * On GPU
        * Each node can potentially take part in ALL transformations, i.e. all bones
        * Vertex shader needs to look up the position from index (what's the best way to do this?)
        * For each transformation (bone) do
            * Look up the weight according to transformation_index + transformation_count * node_index
            * Apply the weighted transformation
            * Weights should sum up to 1, so simply multiply weight * transform_matrix and add up

* ZR should always be a synonym for ALT
* Dialogue: When an item is highlighted whose step != smallStep, should show hint that ALT/ZR can be used
* Mesh files should be kept in a pool. But when to free them?
* Optimise Tesselator by implementing quads with GL triangle strips
* Optimise Tesselator by implementing fans with GL fans
* Fix problem that collision detection sets gel pos to NaN when two gels are set to the exact same spot
* Implement undo for spawn pt actions (serialize spawn pt, restore)
* Pull A corners further back when they're next to each other (to join ramps)
* Make alt-ramp half its height when joining a ramp that has its downside on the alt-ramp's downside
* Improve camera by moving away from a near wall (camera can sometimes look into a wall)
* Place a height map object anywhere. Useful for natural slopes, hills. Needed for town?
* Implement heightAt of ThinPillarFrame*Models
* Implement heightAt of Ramp*AltModels
* Implement heightAt of Stairs*Models
* Implement heightAt of UprightTubeModel
* Implement heightAt of Bevel*Models
* Implement bump maps for textures/materials
* Textures are freed, but only the GL structure. Texture itself stays loaded in TextureManager. Free on level load?
* Crash when switching from full-screen back to window mode! Can't reproduce...
* Optimise more of brick face culling, e.g. bevel checking against bevel
* Editor should delete neighouring bricks that get enclosed while drawing
* Improve collision handling, e.g. move sprite away from near wall
* Make jump z accel lower, but implement climbing low walls (max 1.5m)
* Persisting values
    * App.flags.set(PersistedBool.DOOR42_OPEN, true)
    * App.flags.get(PersistedBool.DOOR42_OPEN).value = true // can keep instance of get for efficiency
    * App.flags.getList(PersistedList.DYNAMIC_ENEMIES, len = 42).get(index = 2).get(PersistedBool.MOUTH_OPEN).value

## Optimisation

Some parts of the engine need a lot of optimisation. Here are a few ideas:

    * From https://docs.nvidia.com/jetson/archives/r36.2/DeveloperGuide/SD/Graphics/GraphicsProgramming/OpenglEsProgrammingTips.html
    * Setting GL state redundantly or resetting state to some default value might lead to performance hit!
    * Batching shaders together is beneficial
    * A very common mistake is setting the tex parameters for filtering and wrapping every time a texture object is
      bound
    * Another common mistake is updating uniforms that haven't changed since the last time the program was used
    * Per-vertex colours are accurately stored with 3 x BYTEs with a flag to normalize in VertexAttributePointer
    * If some attribute for a primitive or a number of primitives is constant for the same draw call, then disable the
      particular vertex attribute index and set the constant value with VertexAttrib*() instead of replicating the data.
    * The number of vertex attributes is a limited resource. If each vertex comes with two sets of texture coordinates
      for multi-texturing, these can often be combined these into one attribute with four components instead of two
      attributes with two components.
    * A structure-of-arrays layout is less efficient than an array-of-structures in most cases.
    * Buffer objects should always be used to store both geometry and indices. Check that no code is calling
      glDrawArrays(), and that no code is calling glDrawElements() without a buffer bind.
    * A common mistake is to have too many small buffers, leading to too many draw calls and thus high CPU load
    * USE VERTEX INDICES AND MAKE UNIQUE IDENTICAL POSITIONS AND NORMALS!
    * Do not write large or generalized shaders
    * Be careful with assuming that conditionals skip computations and reduce the workload
    * Many application-defined uniforms, colours, normals and texture samples can usually be represented using lowp
    * Use mipmaps when appropriate

## Random ideas for the final game

    * Find a slingshot. Maybe somebody builds it for you. There are small pebbles around at hundreds of locations.

    * There is a robot with a head that seems detached. Throw a pebble with the slingshot at it. Its head falls to the
      floor, and the body moves helplessly in search of the head. If it walks into the head's field of view, it turns,
      walks towards it, and puts the head back on. If you grab the head, the body will follow you. If the head is too
      far from the body, the body turns off.

    * A huge city
        * districts
            * the city is divided into districts (XY)
            * each district is completely isolated (big walls)
            * walking between districts always means going through a gate
        * layers
            * the city has three layers: one atop, one ground-level, one below ground
            * changing the layer always means going through a lift
        * map
            * there is one map per district + layer, e.g. District 9 Ground, District 5 Sewers
            * you need to find these maps separately, e.g. buy
            * you can mark things on the map, but you can't fast-travel with the map
        * navigation and fast-travel
            * gates
                * gates are used to travel between districts
                * player knows that loading will occur when passing through such a gate
                * gates are usually in mid-layer
            * lifts
                * to travel between layers, you use a lift
                * sometimes you can also use a lift in the same layer, but they look differently
                * player knows that loading will occur when using a big lift, and none when using a small one
            * trains, subways
                * to travel between districts, or within same district
                * costs money
                * loading will always occur when using a train or subway that leaves the district
            * telephone boxes (maybe too weird?)
                * most district+layer areas have one box, never more than one
                * you need to find and active them (like Weather Wanes in ZBTW)
                * once activated, you can use the box to travel between any of the activated boxes
                * when using a box, you're presented the available places as a menu
                    * not a map, because boxes allow you to travel between layers
                    * not presenting it as a map also helps avoid confusion with the normal map function
                * the box disappears with the player in the ground... no beaming!
                * loading will always occur when using a box
            * ventilation grates
                * you can climb into ventilation grates to travel horizontally
                * they will always stay in the same layer and district, no loading
                * they always connect two places, not more
                * sometimes they connect two rather unlikely places
                * they usually are just a shortcut for a way you could walk otherwise

    * A lightning strikes a tree, which is felled and blocks the way
    * Area is constrained to Home and surrounding woods
    * Pick up firewood in woods
    * Open cupboard, get matches
    * Make a fire in the fireplace
    * Pick up bucket near house
    * Fill bucket with water from brook in woods
    * Fill water into steam engine, start engine
    * Pick up wire from pile near house
    * Put wire on working area near steam engine
    * Tinker up a wire donkey
    * Find scissors in another cupboard
    * Make scissor hands for the wire donkey
    * Tell the donkey to cut up the tree blocking the way
    * Make two more donkeys and go to the market to sell them
    * They sell electric cars in the market
