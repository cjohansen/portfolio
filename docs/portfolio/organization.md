# Organizing scenes

Portfolio tries to automatically organize your scenes into "packages"
(typically: all scenes for a specific component) and "folders" (e.g., a
collection of related scenes, e.g. "Components", "Layouts", etc). It uses the
namespaces of your scenes to do this, and attempts to humanize the resulting
names. You can easily provide custom names, icons, colors and sort orders for
scenes, packages and folders - or you can completely customize your scene
organization if your want.

## Customizing packages / namespaces

The default "packages" correspond to the namespaces where your scenes are
defined. If you drop a bunch of `defscene`s into `ui.components.button-scenes`,
then `ui.components.button-scenes` will be used as a "package", and its name
will be `"Button scenes"`. If you don't like this, call
`portfolio.<adapter>/configure-scenes` from the same namespace:

```clj
(ns ui.components.button-scenes
  (:require '[portfolio.reagent :as portfolio]))

(portfolio/configure-scenes
  {:title "Button"})
```

What keys can you stick in this map? Well, the following (more on icons
[below](#icons)):

- `:title` - The title of the collection.
- `:idx` - The sorting index.
- `:icon` - An icon to display instead of the default 3D box
- `:expanded-icon` - An icon to display when the package is expanded (overrides
  `:icon`, if both are set)
- `:collapsed-icon` - An icon to display when the package is collapsed (overrides
  `:icon`, if both are set)
- `:icon-color` - The icon color.
- `:expanded-icon-color` - A more specific color for expanded packages.
- `:collapsed-icon-color` - A more specific color for collapsed packages.
- `:kind` - One of `:folder` or `:package`. Dictates the rendering style.
  Namespaces with scenes default to `:package`.
- `:default-folder-icon` - The default icon to use for folders nested under this
  collection.
- `:default-folder-expanded-icon` - The default icon to use for expanded folders
  nested under this collection. Overrides `:default-folder-icon` when set, and
  can be overridden on specific folders.
- `:default-folder-collapsed-icon` - The default icon to use for collapsed folders
  nested under this collection. Overrides `:default-folder-icon` when set, and
  can be overridden on specific folders.
- `:default-package-icon` - The default icon to use for packages nested under this
  collection.
- `:default-package-expanded-icon` - The default icon to use for expanded packages
  nested under this collection. Overrides `:default-package-icon` when set, and
  can be overridden on specific packages.
- `:default-package-collapsed-icon` - The default icon to use for collapsed packages
  nested under this collection. Overrides `:default-package-icon` when set, and
  can be overridden on specific packages.

<a id="icons"></a>
## Scene and collection icons

You might be wondering - "what's an icon, anyway?". Good question. An icon is
either hiccup, an SVG element, or a reference to an icon in
[`phosphor-clj`](https://github.com/cjohansen/phosphor-clj). You can browse
icons on the [Phosphor icons website](https://phosphoricons.com/).

To refer Phosphor icons:

```clj
(require '[phosphor.icons :as icons]
         '[portfolio.reagent :as portfolio])

(portfolio/configure-collections
  {:title "My UI Components"
   :icon (icons/icon :phosphor.regular/hamburger)})
```

## Customizing folders

Folders are collections like packages. If you have scenes in
`ui.components.button-scenes`, Portfolio creates a "package" for
`ui.components.button-scenes`, and a "folder" for `ui.components`. If you
already have a `ui.components` namespace in your project, you can call
`portfolio/configure-scenes` as above to customize it. If you don't, you can
somewhat more manually configure the collection in any namespace of your liking
(e.g. the one where you start the Portfolio UI):

```clj
(require '[portfolio.data :as data])

(data/register-collection!
  :ui.components
  {:title "My loverly components"})
```

## Custom organization

If you don't like the default organization Portfolio sets up, you can create
your own arbitrarily nested organization using the two kinds of renderings
available ("folder" and "package"). These are both collections, but they render
differently.

To dictate what collection a scene belongs to, set the `:collection` keyword:

```clj
(defscene poor-legibility
  :collection :ui-malpractice
  [:div {:style {:background "#000" :color "#333"}}
    "Please don't use insufficient contrast"])
```

You can then define the collection using `portfolio.data/register-collection!`:

```clj
(require '[portfolio.data :as data])

(data/register-collection!
  :ui-malpractice
  {:title "UI Malpractice: Don'ts"
   :kind :folder})
```

It does not matter if you define the scene or the collection first, Portfolio
will connect the pieces. If you refer to a `:collection` that doesn't exist,
Portfolio will default the necessary pieces of information.

Collections can nest, just add `:collection` to a collection to indicate that it
belongs to another collection:

```clj
(require '[portfolio.data :as data])

(data/register-collection!
  :ui-malpractice
  {:title "UI Malpractice: Don'ts"
   :collection :process-samples
   :kind :package})

(data/register-collection!
  :process-samples
  {:title "Process samples"
   :collection :ui-kit
   :kind :folder})

(data/register-collection!
  :ui-kit
  {:title "UI Kit"
   :kind :folder})
```

This will render nested folders:

<img src="./nested-folders.png" style="max-width: 400px" alt="Nested folders">

[All documentation](./index.md)
