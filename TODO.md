# TODO

## Backlog

* Gel actions
    * ActionManager: rotationPhi currently does not work correctly, because player's rotationPhi is just camera...

* Bugs
    * Log: "Unloading all 3450 of non-shared programmes..." BrickVolumeRendererImpl should be created by BrickVolume,
      not by BrickSubvolume!
    * Umlauts in German start menu suddenly work, why?!
    * Railing 2 has strange lighting issue across instances (see level)
    * Sometimes keyboard strokes get lost (when frame takes too long, or stroke is hit too quickly)

* Materials
    * Ideally, MeshMaterial and BrickMaterial should be merged, and both should use the same shaders
    * Most materials should have a shader of their own, to optimise computation for that particular material
    * Metallic material should shrink the range of diffuse light such that the point of 100% diffuse light
      becomes an area, and the range where diffuse light is between 0% and 100% is pressed together. This
      increases the overall contrast and reduces the range of soft transition.
    * Shiny material (metal, wet stone) can be made look dirty by multiplying the shininess by a texel from
      a greyscale texture. This technique may also be used overlay a grid of tiles on a shiny surface, giving
      the illusion that the border where tiles are connected is a different material that is not shiny.

* Editor
    * Editor menus should all have
        * a Back item that opens parent; and this should appear at the top
        * a Done item that closes menu without opening the parent; and this should be the ESC item
    * Make editor menu a page in game menu, because it's confusing to have two menus. Allow entering edit mode from that
      menu.
    * Implement undo for spawn pt actions (serialize spawn pt, restore)
    * Editor should delete neighouring bricks that get enclosed while drawing... or shouldn't it?

* Physics
    * Sometimes a ball just stops on the floor with no rebound at all. It does not seem to be caused by hopping
      prevention, so what's the cause?
    * Implement angular momentum for spheres

* Height maps
    * Implement height maps as gels
    * Requires a new editor mode that allows for
        * changing the height of each grid point
        * smoothing the height map around a grid point
        * smoothing the entire height map uniformly
        * adding a wave function
    * The editor should ensure that the height of the lowest grid point should always stay zero, and automatically
      moving the spawn point accordingly if height values are moved accordingly. The reason for this is that the
      height map could otherwise drift from its spawn point. Also, the height map's RigidBody will need a bounding
      box, which can be computed slightly faster if it is known that the lowest point must have a height of 0.
    * The spawn pt should have properties for rotating in all direction, scaling in x, y, z, and material
    * The grid size (XY) of a height map must be specified when creating it; the editor may implement resizing later by
      either cropping or resampling the height map data
    * The renderer of a height map should ideally be the same as MeshRenderer, using the same shaders
    * Height maps need their own kind of RigidBody; physics needs new CollisionStrategies
    * The underside of a height map is always culled, therefore the position should stay fixed. To ensure this, the
      mass of a height map should always be LARGE_MASS (mass not even available in constructor of HeightMapBody). This
      also means that height maps can never collide with bricks.
    * Height maps should skip vertices when viewed from a distance (implies that height maps should never be used to
      cover a very large area, because skipping vertices only in a portion would make the algorithm complicated to get
      a seam without any holes)

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

* Game/town
    * Design a male NPC
    * Design a female NPC

* Dialogues and fonts
    * The story time should be paused while the gameMenu or a dialog is active
    * Dialogue text needs a way to emphasise certain words (bold, red) for highlighting important keywords
    * Maybe also a way to make the text appear smaller and less bright (if an NPC is whispering)
    * Umlaute are missing with certain fonts. Maybe use different fonts anyway...
    * When an item is highlighted whose step != smallStep, should show a hint that ALT/ZR can be used

* Conversations
    * A conversation is a series of dialogues that are shown as an interaction with an NPC
    * Conversations are started via a gel action or as part of a cutscene
    * Conversations block certain interaction such as the game menu. Dialogues do that, too; the dialogues that are
      opened within the conversation must not reset that
    * Conversations can decide to wait for certain events such as:
        * just a delay of a specified time
        * a rigged gel finishes its transition to a certain pose
        * a gel walking on a path reaches the end of that path
        * background music reaches or is already past a certain cue
    * Conversations can change the mode of the camera and must change it back when the conversation ends

* Background music
    * AGL may not be a good choice; maybe switch to SDL for all sound?
    * BGM needs to be streamed from disk, not loaded into RAM in full
    * BGM does not need to be music. It can also be a looped ambient sound that is too long to play without streaming.
    * In order not to put too much load on the CPU, BGM probably needs to be in a raw format (WAV, AIFF)
    * BGM can be pinned to a certain world point. Stereo panning and volume needs to change while the BGM is playing.
    * If there are two or more world points that play a BGM, there are two modes:
        * either the BGM ignores this, causing the music to arbitrarily mix (e.g. chaotic sounds of an amusement park)
        * or the SoundManager decides which one is nearer, and smoothly and fully switch to the nearer one. If the two
          BGMs have the same length and are started at the same time (!!), the two songs are always in sync even if
          only one can actually be heard. This can be used for smoothly transforming the mood as the player moves from
          one area to another.
    * The smooth transition from one song to another should be available without world points, too, e.g. to change the
      mood during a conversation or cutscene

* Cutscenes
    * App.cutscene.start(MyCutscene())
    * Solange App.cutscene.active != null, ist Game im Cutscene-Modus (mit Filmbalken)
    * App.cutscene.animate() wird immer aufgerufen
    * cutscene.nextStep(proceed: (delaySeconds: Float) -> Unit, done: () -> Unit)
    * nextStep wird initial einmal aufgerufen
    * proceed darf pro Step nur einmal aufgerufen werden
    * CutsceneManager wartet delay ab und ruft erst dann nextStep erneut auf
    * Solange proceed nicht aufgerufen wird, bleibt Cutscene hängen, z.B. in einer Conversation

* Spawn points
    * Automatic spawning should be deactivated when a gel crashes during spawning, to prevent from flooding the log

* Deferred gel initialisation
    * Gels need to load textures, meshes and shaders when they are spawned. Doing this from the game loop creates a
      frame drop, so this should always be done from a coroutine.
    * Spawn pts should not immediately return the gel; instead, they should take a lambda onCreated(gel)
    * Gels must override an abstract function initConcurrently(), which is called within a coroutine
    * The spawn pt should not add the gel to the layer until it has finished loading
    * Textures, meshes and renderer can be declared as lateinit, since the gel should not be known to anyone until it
      finished loading and is added to the layer
    * The lambda onCreated is always called from main thread, i.e. needs a place somewhere within the game loop
    * This is similar to handleLoadingScene. Can this concept be generalised?

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

* Performance
    * Don't use DYNAMIC_DRAW everywhere. Use STATIC_DRAW unless model changes often.
    * Don't reconfigure textures every render. Configure them once when creating them. Problem: Textures are shared...
    * Rendering bricks more efficiently
        * BrickVolume should cover all tesselated data; subvolumes should only pick a part from it
        * BrickModelHolder should keep only the tesselated model data of each BrickModel
            * Tesselator would only called by BrickModelHolder (lazy)
            * BrickModelHolder could automatically flip and rotate models, no need to reimplement them
        * Use an alternative (simpler) tesselation when brick subvolume is far (e.g. thin walls become flat, etc.)
        * Positions, texCoords and normals (!!) should be de-duplicated using indices
        * NO sub-sub-volumes! Instead, BrickVolume should be a gel! No global brick volume! Can even rotate them...
    * Runs at 24fps on hp laptop... Is there anything to do about it?

* Walk meshes
    * Enemies that cover an area need to do pathfinding to find a way to the player while avoiding any obstacles
    * Another example is an NPC idly walking around between points of interest inside a non-trivial area
    * Pathfinding on the entire world including bricks and nearby gels is too costly and complicated. Instead, the
      editor should allow defining WalkMeshes, which are an invisible mesh that define the area that is walkable.
    * The editor should make it easy to lay the WalkMesh neatly on top of bricks and static gels
    * The gel of the enemy or NPC that needs to do pathfinding must first find its WalkMesh by id and can then use
      it for finding a path to an arbitrary point withing the area of the WalkMesh (the target does not need to be
      on the vertex of the mesh).
    * All pathfinding happens within the mesh only; non-static gels such as other NPCs or the player are not taken into
      account
    * When an enemy or NPC found a path, it does not need to use the path strictly. When pushed by another body, or
      if it comes near a non-static gel, it should do local collision avoidance. If doing so leads too far away from
      the point it was heading to, it should recompute its path.
    * An enemy or NPC may occasionally walk outside its WalkMesh, e.g. when pushed by a large object. This is OK, but
      for pathfinding the enemy needs to walk back to the closest point on the WalkMesh, and it must do so in a straight
      line since pathfinding can only happen within the WalkMesh.
    * Pathfinding is expensive, so all pathfinding must happen in a coroutine. The gel must wait until the path becomes
      available. An NPC might scratch its head in the meantime; an enemy might look in the direction of the player and
      scream.
    * The A*/funnel algorithm I implemented for retro_adventure can probably be reused.

* Keyboard and gamepad
    * Keyboard layout should make more use of SHIFT, ALT, CMD and CTRL. This is important, because many keyboards fail
      to produce proper key events for combinations with more than two keys that do not involve these modifier keys.
    * Inside the game, mouse motion should control the camera. This is more intuitive than using the keyboard.
    * Inside a dialogue or menu, the cursor should appear, and mouse motion should no longer control the camera.
      Instead, the items of the dialogue or menu should be clickable. The cursor should disappear as soon as the
      dialogue or menu is dismissed.
    * Should we periodically try to update the gamepad database over the internet? glfwUpdateGamepadMappings

* Monitors
    * Both 16:9 and 16:10 should be fullscreen without any margin. 16:9 should be our FIXED_ASPECT_RATIO. On a 16:10
      monitor, the left and right side should be cropped. UI needs to get a margin.

* Shaders
    * Can I make all programs shared? Or should none be shared? Making all shared should probably be fine, because
      that's how bricks worked until recently...
    * Implement bump maps for textures/materials

* Textures
    * Textures are freed, but only the GL structure. Texture itself stays loaded in TextureManager. Free on level load?
    * Java ImageBuffer is bloated and complicated; my own routines are slow. Is there a good alternative? (Actually
      do some performance measurement first to check what part is slow. Mostly noticable when opening a dialogue with
      many items.)

* Mesh gels
    * Mesh files should be kept in a pool, because some meshes are reused by several gels, or several instances of a
      gel. But is freeing them only at scene loading time enough?

* Bricks
    * Add new brick shapes
        * Arcs, e.g. under a bridge
        * Double-bevel bricks (horiz bevel at 0.5m height, but no additional vertical division) for walls
    * Pull A corners further back when they're next to each other (to join ramps)
    * Make alt-ramp half its height when joining a ramp that has its downside on the alt-ramp's downside
    * Optimise Tesselator by implementing quads with GL triangle strips
    * Optimise Tesselator by implementing fans with GL fans
    * Optimise more of brick face culling, e.g. bevel checking against bevel

* Camera
    * Improve camera by moving away from a near wall (camera can sometimes look into a wall)

* Player behaviour
    * When jumping, the player gel should first check if there is nothing blocking the way. If there isn't, it should
      directly move the position a bit upwards. The up speed then does not need to be very high. Zelda BoTW seems to do
      this, and it improves the responsiveness of the action.

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
