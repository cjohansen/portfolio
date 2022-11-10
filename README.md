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

Portfolio is currently a proof of concept under initial development. The basic
API for creating scenes are crystalizing, but details about the Portfolio app,
its plugins, etc, are still subject to change.

## Install

With tools.deps:

```clj
no.cjohansen/portfolio {:mvn/version "0.2022.11.11"}
```

With Leiningen:

```clj
[no.cjohansen/dumdom "0.2022.11.11"]
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

At its very minimal, a scene is just a named instance of a component. Where you
require `defscene` from depends on your rendering library of choice. If you're
using [https://github.com/reagent-project/reagent](reagent), you'll
`(:require [portfolio.reagent :refer-macros [defscene]])`.

Currently these adapters are supported:

- [Reagent](https://github.com/reagent-project/reagent) - `portfolio.reagent`
- [Rum](https://github.com/tonsky/rum) - `portfolio.rum`
- [Dumdom](https://github.com/cjohansen/dumdom) - `portfolio.dumdom`
- DOM API - `portfolio.dom`
- HTML strings - `portfolio.html`

All these namespaces have a `defscene` macro that works the same way.

`defscene` takes a symbol name and a component as its minimum input, but it can
also take additional key/value pairs:

```clj
(defscene name
  ;; key/value pairs
  component)
```

Scenes can also be functions:

```clj
(defscene name
  ;; key/value pairs
  [args]
  (render-component args))
```

By using the latter form, you allow Portfolio to know about the component's
arguments. This enables you to use `tap>` and Portfolio's UI to interact with
your component, or bind the scene to an atom to trigger interactions.

Currently supported key/value pairs:

- `:title` - Give the scene a nice string name
- `:args` - The initial arguments passed to the component function
- `:on-mount` - A function called when the scene is initially mounted. The
  function is passed the component arguments.
- `:on-unmount` - A function called when the scene is removed from the DOM. The
  function is passed the component arguments.

## Try it out

You can take the Portfolio UI for a spin by cloning this repo, starting
figwheel, and then checking out [http://localhost:5995](http://localhost:5995),
which will show you all the scenes defined in [./sample/src/portfolio](the
sample project).
