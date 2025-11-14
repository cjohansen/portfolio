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

Portfolio is stable and ready to use. APIs documented in this document are final
and will not change. APIs not explicitly documented in this document, especially
those pertaining to extending and customizing the UI, may still be subject to
change.

Is Portfolio good enough that you can port over from Storybook? Probably, yes.
If you're using Storybook extensions not covered by Portfolio, open an issue.

## Show me, don't tell me

[Watch my Portfolio presentation](https://www.youtube.com/watch?v=25JDQRFoQ_U)
at London Clojurians.

## Sample

![The Portfolio UI](./docs/portfolio/sample.png)

There is a [live sample](https://cjohansen.github.io/) to check out. The source
code for the sample is [also available](https://github.com/cjohansen/sasha).

## Install

With tools.deps:

```clj
no.cjohansen/portfolio {:mvn/version "2025.11.2"}
```

With Leiningen:

```clj
[no.cjohansen/portfolio "2025.11.2"]
```

## Usage

Portfolio displays your components in "scenes". A scene is a component in a
specific state. At its most minimal, a scene is just a named instance of a
component:

```clj
(defscene button
  [:button.button "I am a button"])
```

[Read more about defscene](./docs/portfolio/defscene.md).

### Starting the Portfolio UI

After you have created your scenes, start the UI:

```clj
(require '[portfolio.ui :as ui])

(ui/start!)
```

### Custom CSS and HTML

You can add [custom CSS](./docs/portfolio/custom-css.md) and [custom
HTML](./docs/portfolio/custom-html.md) to render your scenes with.

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

Check out [this repo](https://github.com/cormacc/cljserial) for a sample setup
with an app target and a portfolio target with shadow-cljs.

### shadow-cljs + separate dev server

If you are using shadow-cljs to build the front-end and leiningen to run the dev
server separately you need to make sure that you add `classpath:public` to the
`:resource-paths` and the dependency to Portfolio to the `:dependencies` in
`project.clj`. This will then serve the necessary assets to make Portfolio work.

## Customizing the Portfolio UI

Portfolio comes with a bunch of handy (and optional) tools to help you develop
components:

- Control background colors and body classes
- Control viewport sizes
- Display a grid
- Display component documentation
- Display scene source code
- Split windows
- Compare different scenes side-by-side

[Read about UI customization](./docs/portfolio/customize-ui.md).

## Organizing scenes

Portfolio automatically organizes your scenes by component and application
layer, and offers many knobs and tweaks to give you full control. [Read about
organization](./docs/portfolio/organization.md).

## Search

Search your scenes and collections. This feature is not enabled by default, as
it's assumed not to be very useful until you have enough content. To enable it,
create an index and pass it when you start the UI:

```clj
(require '[portfolio.ui.search :as search]
         '[portfolio.ui :as ui])

(ui/start!
 {:config
  {:css-paths ["/styles/app.css"]}
  :index (search/create-index)})
```

`create-index` returns an implementation of
`portfolio.ui.search.protocols/Index`. You can provide custom implementations of
this protocol to completely customize the search. More documentation on this
will follow.

## REPL usage

If you just want to see what a specific component looks like with some data you
caught in your REPL, but don't necessarily want to commit a new scene, you can
`tap>` components, and Portfolio will render it under a dedicated folder.

```clj
;; Evaluate this expression
(tap> (MyComponent {:text "Test"}))
```

## Try it out

You can take the Portfolio UI for a spin by cloning this repo, starting
figwheel, and then checking out [http://localhost:5995](http://localhost:5995),
which will show you all the scenes defined in [the sample project](./sample/src/portfolio).
There are also some scenes in the "mirror"
build, which demonstrate some of Portfolio's own UI components, available at
[http://localhost:5995/mirror.html](http://localhost:5995/mirror.html).

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
- @brandonstubbs React 18-related features.
- @elken made some visual improvements

Portfolio is proudly sponsored by [Clojurists
Together](https://www.clojuriststogether.org/).

## Wishlist

Some features and fixes that are likely to be explored in the close future:

- Improved 404 page when namespaces disappear
- Search in mobile view
- Better error-handling for things like `:on-mount`
- Better first use experience: display some documentation when there are no
  scenes
- Generate scenes from a component and specs

## Changelog

### 2025.11.2

Make `portfolio.replicant` usable from CLJC files.

### 2025.10.1

Improve clj-kondo hook for defscene.

### 2025.08.29

- Improve clj-kondo's understanding of `defscene` ([Mikko
  Koski](https://github.com/rap1ds)).

### 2025.01.28

- Use official Replicant version for the Replicant adapter.

### 2024.09.25

- Properly set/unset :background/document-data when switching themes
  ([#32](https://github.com/cjohansen/portfolio/issues/32))
- Don't start the UI before Prism is loaded
  ([#31](https://github.com/cjohansen/portfolio/issues/31))
- Use DOMContentReady so Portfolio starts when loaded in `<head>`
- Render a proper error message when unable to load CSS
- Skip Prism highlighting when unable to load Prism

### 2024.06.30

- Fix a bug where the reagent adapter re-mounted components too eagerly.

### 2024.03.18

- Add clj-kondo configuration for `defscene`
- Add support for [`:background/document-data`](./docs/portfolio/customize-ui.md)
- Fix a bug where the background tool would not switch background color
  immediately
- Fix React render under advanced optimizations
- Add experimental support for
  [replicant](https://github.com/cjohansen/replicant)

### 2023.12.07

- Properly unmount Reagent components (fix #15)
- Add support for setting classes on the document element with the background
  tool, by setting `:background/document-class`
- Upgrade the phosphor-clj icons dependency to fix a buggy pallette icon

### 2023.12.06

- See 2023.12.07. This release came with a bug that did not re-render scenes
  when changing Portfolio view options.

### 2023.07.15

- Improved error handling in multiple places. If you encounter an uncaught
  exception, report it as a bug.
- React components are now rendered with an error boundary. This allows
  Portfolio to present errors from within your React component lifecycle like
  any other error
- The React adapter now supports the use of hooks directly in scene definitions.
- `tap>` components from your code or the REPL to render them in Portfolio.
- Serve browsable documentation directly inside Portfolio (there's a book icon
  in the top left corner).

### 2023.05.12

- Render icons via [phosphor-clj](https://github.com/cjohansen/phosphor-clj)
- Fix bug: Using a custom canvas HTML failed if Portfolio's rendering target
  wasn't hard-coded.
- Adjust implementation of CSS reloading to also work for shadow-cljs

### 2023.04.26

- Now works properly with advanced compilation mode in shadow-cljs (e.g.
  production builds)
- Add back a missing close icon
- Remove some unnecessary scrollbars in the UI
- Don't trip on scenes with multiple lines of code in their body
- Render compare mode menus with the proper background
- Slightly improved code formatting for scene code

### 2023.04.21

#### UI improvements

- Many improvements to the UI, big and small
- Portfolio now works well on mobile devices
- Split panes can be resized with drag'n'drop
- Persist the state of tools like background and viewport
- Portfolio's console logs are disabled by default
- Added a small intro page for new setups when there are no scenes

#### Compare mode

When running Portfolio in split mode, you can select specific scenes for either
of the panes. This way you can not only compare different versions of the same
scene, but compare different scenes to each other.

#### Documentation and code

Scenes and collections can now have Markdown docstrings. These render above the
scenes, and there is a new toolbar button to toggle their display on or off.

Portfolio can now also optionally display the scene code. This is toggled off by
default, and can be enabled by clicking the brackets button in the toolbar.

#### Organization improvements

There are some improvements to Portfolio's default organization into packages
and folders, and particularly their interaction with your custom collection
configuration.

You can now specify default scene icons for collections.

#### Improved atom param support

Portfolio now watches all atoms in `:param`. This means you can set `:param` to
e.g. a map or a vector, put several atoms inside (arbitrarily nested), and
Portfolio will re-render the scene whenever any of them change. To reflect that
`:param` is no longer necessarily just one thing, it has also been aliased as
`:params`.

#### Search

Add search, including APIs for customizing indexing, searching, and result
preparation.

### 2023.04.05

Added [new APIs for organizing scenes](#organizing-scenes) in the sidebar.

## License

Copyright © 2022-2025 Christian Johansen

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
