# titanium

## Overview

License: GNU General Public License v3.0

Project start: 24/Oct/2021

Github: https://github.com/digorydoo/titanium

Titanium is a game engine for an open world game like Zelda BoTW. The project name is a codename that should be
considered temporary. It refers to the sheer amount of work that needs to be done.

Titanium is developed under macOS, but it should also compile and run under Windows. The windows build is not frequently
tested, so there may be compilation or other issues.

Titanium is completely written in Kotlin/JVM, with no lines of code written in C++. Titanium currently uses lwjgl
(OpenGL) as the rendering pipeline. Titanium also uses GLFW for the windowing system, and OpenAL for the sound engine
(but the latter may change in the future). Other than those, Titanium uses a minimum amount of libraries. In particular,
the physics engine is fully implemented in Kotlin, and no other library is involved.

The project is divided into the following parts:

   * engine: The core of the engine
      * There is NO dependency to OpenGL or other third-party libraries here.
      * There is NO dependency to game-specific code either.
      * GL and game-specific stuff is abstracted away with proper interfaces.
   * game: The code of the concrete game
      * All scenes, NPCs, enemies, etc. that are specific to the game will go here.
      * There is NO dependency to OpenGL or other third-party libraries here.
   * main: The main entry point
      * This glues together the engine and the game parts.
      * This is the only part of the project that has any dependency to OpenGL or other third-party libraries (apart
        from import_asset).
   * import_asset: A command line tool for importing assets into the engine
      * Import Collada files and store them as Titanium binary mesh files
      * Import PNG texture files and put them together into a single PNG that can be used as a FrameCollection
   * kutils: Various utility functions
      * These are generic Kotlin utilities that deal with math, strings, and other aspects.
      * They are a separate project, because these functions are useful in other projects, too.

## Game assets

NOTE: The game assets are not part of this github project! The game will not start without these assets! I will provide
sample assets in the future. At the moment, there is no remedy!

The assets folder is divided into the following subdirectories:

    fonts/*.ttf              fonts used by the game menus and dialogues (TrueType; other formats may be supported)
    gellists/*.gls           spawn points and their properties per scene (currently text files; may change)
    generated/               assets below this subdirectory are generated through import_asset
        mesh/*.msh           mesh data imported via import_asset (custom binary format)
        textures/*.png       brick textures combined into a single PNG file through import_asset
    playfields/*.pf          brick playfield data per scene (custom binary format)
    private/                 assets below this subdirectory will NOT be copied into the final product
        bundle/              assets used by make-bundle.sh
            macos/           macOS-specific assets
            windows/         Windows-specific assets
        collada/             Collada files that serve as the input for import_asset (cannot be loaded directly)
        textures-tiles-town/ individual PNG textures that are combined into a single texture through import_asset
    shaders/*.(fsh|vsh)      GLSL shaders (text files)
    sounds/*.wav             sound data
    textures/*.png           texture data that are not brick textures

At the moment, GLSL shaders will be copied directly into the final product. TODO: Move them to private/, and make
import_asset minify and lightly encrypt them. This would involve resolving the ifdefs beforehand, i.e. there would be
multiple output files based on all combination of ShaderFlags!

## Getting started

After having obtained the assets (see previous section), you now need to set up kutils. kutils is a bunch of Kotlin
utility functions that is available on Github:

    https://github.com/digorydoo/kutils

Pull the repository and put it in the same folder as you put titanium. On macOS, you can use a symbolic link to link
titanium to its kutils sources:

    $ cd titanium/kutils
    $ ln -s ../../kutils/main/src/

On Windows, symbolic links are poorly supported, so you have to copy the sources of kutils into titanium:

    $ cd titanium/kutils
    $ cp -R ../../kutils/main/src src

On Windows, a Cygwin installation is necessary. Apart from make-proper.sh (see below), there is also a bash-based
post-build step (called post-build.sh) that will be called from gradle on every build.

If everything looks good, you can now run the script make-proper that does the rest of the setup for you.

    $ ./make-proper.sh

This does the following:

   * Create the assets/generated folder
   * Import Collada mesh files
   * Import brick textures

After that, you should be able to build and run the game with:

    $ ./gradlew main:run

## Creating a bundle

Use the script ./make-bundle.sh to create an executable bundle. This uses jpackage to bundle the java classes together
with a JRE. Make sure a JRE is available in your PATH before running the script.

## Game keybindings

Key bindings are in KeyMappingInGame.kt. The most important ones are:

   * ALT+RETURN: Toggle between fullscreen mode and window mode
   * Arrow keys, ASDW: Move player
   * SHIFT + Arrow keys, SHIFT + ASDW: Move player with double speed
   * HJKU: Rotate camera
   * Space, V: Jump

While the game is intended for use with a Gamepad, the final version should also be playable with keyboard. Currently,
the keyboard bindings are not ideal. The main concerns are:

   * Keys that can be combined should be bound to modifier keys, because some keyboards don't recognise combinations
     of regular keys
   * Camera control should be done through mouse, because using keys is somewhat tedious

## Editor

The engine does not have a sophisticated editor. The idea is that Kotlin can be short and concise, and there is no
editor needed for things that can be done just as easily from code. However, it's obvious that for a proper level
design, some kind of editor is needed. For this, Titanium has an editor mode. You basically change things from within
the game.

Enter editor mode with CTRL+E. Key bindings are in KeyMappingInEditor.kt.

### General
   * CTRL+E: Enter editor mode; leave editor
   * CTRL+S: Save
   * ESC: Open editor main menu (to change Lighting, etc.)
   * Return: Set player position (only temporarily)
   * C: Switch camera mode

### Moving, selecting
   * Arrow keys: Move caret
   * SHIFT+Arrow: Extend selection
   * ALT+Arrow: Move selection (doesn't move the enclosed bricks)
   * HOME, END: Move caret pagewise, horizontally
   * PgUp, PgDown: Move caret pagewise, vertically
   * Y: Switch selection extension point (and thus also camera target)
   * SHIFT+Y: Switch selection extension point in reverse order

### Clipboard/Undo
   * CTRL+Z: Undo
   * CTRL+SHIFT+Z: Redo
   * CTRL+C: Copy bricks to clipboard
   * CTRL+X: Cut bricks to clipboard
   * CTRL+V: Paste from clipboard

### Bricks
   * Backspace, Delete: Delete brick at caret or bricks in selection
   * Q: Fill selection with brick(s) of current shape and material
   * W: Automatically find a brick shape and material for the current location
   * M: Choose brick material
   * N: Choose brick shape
   * P: Pick up shape and material from brick at cursor
   * CTRL+I: Get brick info
   * CTRL+M: Apply brick material
   * CTRL+N: Apply brick shape
   * CTRL+R: Rotate the selected bricks

### Spawn points
   * T: Open spawn point menu
   * CTRL+T: Jump to next spawn point (guaranteed to visit all spawn points once; order not defined)
   * CTRL+SHIFT+T: Jump to previous spawn point
   * SHIFT+Q: Add a new spawn point of the same type as a recently added one

That's all, folks!
