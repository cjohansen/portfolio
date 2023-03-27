# Portfolio

Develop ClojureScript UI components in isolation in a "visual REPL". Increase
your visual bandwidth by seeing your components in various states, screen
resolutions, and other configurations simultaneously.

Portfolio brings some of the best features of
[Storybook.js](https://github.com/storybookjs/storybook) to ClojureScript, and
adds a few of its own. While Storybook.js was its starting point, Portfolio does
not aspire to feature-parity with it, and instead caters to the REPL-oriented
ClojureScript development process.

## Status

Portfolio is stable and ready to use, but not yet feature complete. APIs
documented in this document are final and will not change. APIs not explicitly
documented in this document, especially those pertaining to extending and
customizing the UI, may still be subject to change.

## Sample

![The Portfolio UI](./docs/sample.png)

There is a [live sample](https://cjohansen.github.io/) to check out. The source
code for the sample is [also available](https://github.com/cjohansen/sasha).

## Install

With tools.deps:

```clj
no.cjohansen/portfolio {:mvn/version "2023.03.28"}
```

With Leiningen:

```clj
[no.cjohansen/portfolio "2023.03.28"]
```

## Usage

Portfolio displays your components in "scenes". A scene is a component in a
specific state. Whether that "component" is a single element, like a button, or
an entire page layout, is completely up to you. You define scenes with the
`defscene` macro:

```clj
(defscene button
  [:button.button "I am a button"])
```

At its most minimal, a scene is just a named instance of a component. Where you
require `defscene` from depends on your rendering library of choice. If you're
using [reagent](https://github.com/reagent-project/reagent), you'll `(:require
[portfolio.reagent :refer-macros [defscene]])`.

Currently these adapters are supported:

- [Reagent](https://github.com/reagent-project/reagent) - `portfolio.reagent`
- [Rum](https://github.com/tonsky/rum) - `portfolio.rum`
- [Dumdom](https://github.com/cjohansen/dumdom) - `portfolio.dumdom`
- React (including [Helix](https://github.com/lilactown/helix)) - `portfolio.react`
- DOM API - `portfolio.dom`
- HTML strings - `portfolio.html`

All these namespaces have a `defscene` macro that works the same way. Note that
Portfolio does not depend on any of these, so if you're using
`portfolio.reagent`, you must explicitly pull in `reagent` yourself.

`defscene` takes a symbol name and a component as its minimum input, but it can
also take additional key/value pairs:

```clj
(defscene name
  ;; key/value pairs
  component)
```

Scenes can be functions:

```clj
(defscene name
  ;; key/value pairs
  [param portfolio-opts]
  (render-component param))
```

By using the latter form, you allow Portfolio to know about the component's
arguments. This enables you to use `tap>` and Portfolio's UI to interact with
your component, or bind the scene to an atom to trigger interactions. It also
allows you to use portfolio's layout options (background, viewport size, etc) to
render the component.

Here's an example of passing an atom to your scene:

```clj
(defscene name
  :param (atom {:title "Hello world!"})
  [param portfolio-opts]
  [:h1 (:title @param)])
```

As you can see - if you pass an atom as `:param`, an atom is what is passed to
your component function. If you just want a map, that can also benefit from this
indirection, because it allows you to use Portfolio's UI to tinker with the
parameter:

```clj
(defscene name
  :param {:title "Hello world!"}
  [param portfolio-opts]
  [:h1 (:title param)])
```

While a symbol is a good identifier, you probably want to set `:title` for a
more pleasant-looking UI:

```clj
(defscene default-scenario
  :title "Default scenario!"
  :param {:title "Hello world!"}
  [param portfolio-opts]
  [:h1 (:title param)])
```

With `:title`, this will list as `Default scenario` in the sidebar instead of
`default-scenario`.

Currently supported key/value pairs:

- `:title` - Give the scene a nice string name
- `:param` - The initial parameter passed to the component function
- `:on-mount` - A function called when the scene is initially mounted. The
  function is passed the component arguments.
- `:on-unmount` - A function called when the scene is removed from the DOM. The
  function is passed the component arguments.

### Starting the Portfolio UI

After you have created your scenes, start the UI:

```clj
(require '[portfolio.ui :as ui])

(ui/start!)
```

### Custom CSS

By default your scenes will render in a blank HTML page called the canvas. This
page has no default styling, and comes as bare as possible out of the package.
You might want to add some CSS files to the canvas, which can be done with
`ui/start!`:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
  {:config {:css-paths ["/myapp/custom.css"]}})
```

Add as many CSS files as you like. If you run Portfolio with figwheel-main, CSS
files will automatically reloaded when changed.

### Custom Canvas HTML

If you need to make more adjustments to the canvas, such as adding meta tags,
global JavaScripts etc, you're better off providing your own canvas:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
  {:config {:canvas-path "/my/custom/portfolio.html"}})
```

There are no requirements to how you format this file. Portfolio will add a div
with id `"canvas"` to it, in which it will render the scene. If there already is
an element with that id, it will be used instead, so be aware of that.

## shadow-cljs

To use Portfolio with shadow-cljs, you must ensure that Portfolio's resources
are served by the development HTTP server. Include `"classpath:public"` in your
`:dev-http` sources:

```clj
:dev-http {8080 ["public" "classpath:public"]}
```

This will serve files from `public` in your project (where presumably your
index.html and CSS files are), and resources in `public` on the classpath (e.g.
Portfolio's resources). Adjust as necessary.

## Customizing the Portfolio UI

The Portfolio UI is highly customizable: The default canvas tools are all
optional, and their options can be configured to your liking. While not yet
finalized there are also APIs for you to create custom tools - locale selection,
theme selectors, and whatever else your imagination can conjure. Documentation
will be available when these APIs are considered stable.

### Background

The background tool sets a background color for your scene, and adds a class
name to the `body` element, to help your CSS choose between dark mode and light
mode. See [./src/portfolio/views/canvas/background.cljs](background.cljs) for
specifics about the default options. To default Portfolio to use dark mode for
scenes, specify `:background/default-option-id` when calling `ui/start!`:

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
             :background/body-class "light-mode"}}
    {:id :bleak-mode
     :title "Bleak mode (.bleak-mode)"
     :value {:background/background-color "#000000"
             :background/body-class "dark-mode"}}]

   :background/default-option-id :bleak-mode}})
```

### Viewport

The viewport tool sets the dimensions of the viewport your scenes are rendered
in, and can help with responsive design. The default options are auto and an
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

### Grid

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

## Try it out

You can take the Portfolio UI for a spin by cloning this repo, starting
figwheel, and then checking out [http://localhost:5995](http://localhost:5995),
which will show you all the scenes defined in [./sample/src/portfolio](the
sample project).

## Disabling Portfolio in production

The `defscene` macro can be placed anywhere you like - in separate files, or
inline in production code alongside the implementation being demonstrated. In
the latter case, you probably want to strip the macros from you production
builds. It is assumed that most people will put Portfolio scenes in a separate
directory that can easily be excluded from production builds, so Portfolio is
enabled by default. To disable it in your build, use any of the following two
options.

### Adding a compiler option

Add `:portfolio/enabled? false` to your ClojureScript compiler options:

```clj
{:main "myns.prod"
 :optimizations :advanced
 :source-map true
 :portfolio/enabled? false}
```

### Using a Closure define

Your second option is to set the `portfolio.core/enabled` [Closure
define](https://clojurescript.org/reference/compiler-options#closure-defines) to
`false`. Closure defines can be set several ways, see the link.

## Contributions

Yes please! Feel free to contribute more framework adapters, UI extensions or
whatever. Please open an issue or a draft PR to discuss larger changes before
pouring too much work into them, so we're sure we're one the same page.

- @rome-user added [support for pure React components and Helix](https://github.com/cjohansen/portfolio/pull/2).
- @BorisKourt and @thheller helped with shadow-cljs support.

## License

Copyright © 2022-2023 Christian Johansen

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
