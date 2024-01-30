# Customizing the Portfolio UI

The Portfolio UI is highly customizable: The default canvas tools are all
optional, and their options can be configured to your liking. While not yet
finalized there are also APIs for you to create custom tools - locale selection,
theme selectors, and whatever else your imagination can conjure. Documentation
will be available when these APIs are considered stable.

## Background

The background tool sets a background color for your scene, and adds a class
name to the `body` element, to help your CSS choose between dark mode and light
mode. See
[`portfolio.ui.canvas.background`](../../src/portfolio/ui/canvas/background.cljs)
for specifics about the default options. To default Portfolio to use dark mode
for scenes, specify `:background/default-option-id` when calling `ui/start!`:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
  {:config {:background/default-option-id :dark-mode}})
```

To change the available options, use `:background/options`:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
 {:config
  {:background/options
   [{:id :bright-mode
     :title "Bright mode (.bright-mode)"
     :value {:background/background-color "#f8f8f8"
             :background/document-class "light-mode"}}
    {:id :bleak-mode
     :title "Bleak mode (.bleak-mode)"
     :value {:background/background-color "#000000"
             :background/document-class "dark-mode"}}]

   :background/default-option-id :bleak-mode}})
```

The `:value` takes the following keys:

- `:background/document-class` a class string to add to the html element of your
  scene.
- `:background/body-class` a class string to add to the body element of your
  scene.
- `:background/background-color` a CSS color to set to Portfolio's UI
  surrounding your scene - documentation, code examples, etc.

Beware that the background menu persists your chosen theme in the browser's
local storage. If it appears that setting the `:background/default-option-id`
has no effect, clear your local storage and try again.

Portfolio keys your preferences with the current config, but if you change back
and forth you might run into a situation where you already have a persisted
preference and the default option appears to have no effect.

## Viewport

The viewport tool sets the dimensions of the viewport your scenes are rendered
in, and help with responsive design. The default options are auto and an
iPhone-like size. You can provide your own options if you want:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
 {:config
  {:viewport/options
   [{:title "Auto"
     :value {:viewport/width "100%"
             :viewport/height "100%"}}
    {:title "iPhone 12 / 13 Pro"
     :value {:viewport/width 390
             :viewport/height 844}}]}})
```

Options can use specific pixel dimensions, percentages, or a mix. You can have
as many resolutions as you need. You can optionally control scene offset from
the viewport by adding `:viewport/padding` to either a number, or a vector with
four numbers (padding north, east, south, west).

If you just want to display all scenes in a default viewport, and don't care for
the viewport button in the toolbar, you can configure it like so:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
 {:config
  {:viewport/options []
   :viewport/defaults
   {:viewport/padding [0 0 0 0]
    :viewport/width 390
    :viewport/height 844}}})
```

You can of course also combine viewport options with a default viewport.

## Grid

The grid tool displays a grid in the background of your scenes. The default is
either no grid, or a 5 by 20 pixel grid. Change this as you see fit:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
 {:config
  {:grid/options
   [{:title "5 x 20px"
     :value {:grid/offset 0
             :grid/size 20
             :grid/group-size 5}}
    {:title "No grid"
     :value {:grid/size 0}}]}})
```

## Docs

The docs tool toggle documentation on and off globally. It will toggled on by
default.

## Code

The code tool toggles on and off scene code - e.g. the code in the `defscene`
body. This only works with inline components and the arguments/body form. If
your scene is created with just a reference to a function, Portfolio can't
automatically display its implementation.

## Split windows

The window splitting tool allows you to run multiple panes at once. This allows
you to view multiple versions of a scene simultaneously.

## Compare tool

The compare tool allows you to select different scenes in split panes. This way
you can not only compare different versions of the same scene, but also
different scenes.

## Portfolio documentation

The Portfolio documentation will be available via a button in the sidebar when
Portfolio is served from localhost or directly via an IP address. You can
control the button's appearance manually. The following example disables it
completely:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
  {:config {:portfolio-docs? false}})
```

## More customization

- [Customize CSS](./custom-css.md)
- [Customize canvas HTML](./custom-html.md)
- [All documentation](./index.md)
