# The `defscene` macro

`defscene` defines a scene in which Portfolio will showcase a component in a
specific state. Whether that "component" is a single element, like a button, or
an entire page layout -- is completely up to you.

At its most minimal, a scene is just a named instance of a component:

```clj
(defscene button
  [:button.button "I am a button"])
```

## Rendering library adapters

Where you require `defscene` from depends on your rendering library of choice.
If you're using [reagent](https://github.com/reagent-project/reagent), you'll
`(:require [portfolio.reagent :refer-macros [defscene]])`.

Currently these adapters are supported:

- [Reagent](https://github.com/reagent-project/reagent)
  - Older versions of React - `portfolio.reagent`
  - React versions 18+ - `portfolio.reagent-18`
- [Rum](https://github.com/tonsky/rum)
  - Older versions of React - `portfolio.rum`
  - React versions 18+ - `portfolio.react-18`
- [Dumdom](https://github.com/cjohansen/dumdom) - `portfolio.dumdom`
- [Replicant](https://github.com/cjohansen/replicant) - `portfolio.replicant`
- React (including [Helix](https://github.com/lilactown/helix), [UIx<sup>2</sup>](https://github.com/pitch-io/uix))
  - Older versions of React - `portfolio.react`
  - React versions 18+ - `portfolio.react-18`
- DOM API - `portfolio.dom`
- HTML strings - `portfolio.html`

All these namespaces have a `defscene` macro that works the same way. Note that
Portfolio does not depend on any of these, so if you're using
`portfolio.reagent`, you must explicitly pull in `reagent` yourself.

## API docs

`defscene` takes a symbol name and a component as its minimum input, but it can
also take additional key/value pairs, and an optional docstring:

```clj
(defscene name
  ;; Optional docstring
  ;; key/value pairs
  component)
```

Docstrings can contain Markdown:

```clj
(defscene empty-input
  "The `LabeledInput` component is a responsive form control"
  (LabeledInput {:label "Your name"}))
```

Scenes can take arguments and have function bodies:

```clj
(defscene name
  ;; key/value pairs
  :params {:title "Your component data here"}
  [params portfolio-opts]
  (render-component params))
```

Scenes can also use existing functions to render:

```clj
(defn render-button [data]
  [:button.button (:text data)])

(defscene reusable-fn
  :params {:text "Click the button!"}
  render-button)
```

By using `:params` and either a function body or an existing function, you allow
Portfolio to know about the scene's component data. This enables you to use
`tap>` and Portfolio's UI to interact with your component, or bind the scene to
an atom for stateful scenes. It also enables you to inspect portfolio's layout
options (background, viewport size, etc) to render the component.

## Stateful scenes

If you pass an atom to a scene, the scene will re-render on every change of the
atom. There's no magic involved, just pass an atom to `:params`:

```clj
(defscene name
  :params (atom {:title "Hello world!"})
  [store portfolio-opts]
  [:h1 (:title @store)])
```

If you pass an atom as `:params`, an atom is what is passed to your component
function. Scenes have mount and unmount hooks that can be useful with atom
params:

```clj
(defn shuffle-titles [store titles]
  (swap! store :idx 0)
  (js/setInterval
   (fn []
     (swap! store
      (fn [state]
        (let [idx (mod (inc (:idx state)) (count titles))]
          (assoc state
                 :title (nth titles idx)
                 :idx idx)))))
   2000))

(defscene name
  :params (atom {})
  :on-mount (fn [store]
              (let [titles ["I am the first title"
                            "It is time for the second title"]]
                (swap! store :timer (shuffle-titles store titles))))
  :on-unmount (fn [store]
                (js/clearInterval (:timer @store)))
  [store]
  [:h1 (:title @store)])
```

## Parameterized static scenes

Even scenes that just need a map can benefit from the indirection provided by
`:params`, because it allows you to access the component data separately. The
uses for this are countless, some suggestions include:

- Using Portfolio's UI to tinker with the parameter
- Power a search that finds scenes based on data

```clj
(defscene name
  :params {:title "Hello world!"}
  [params portfolio-opts]
  [:h1 (:title param)])
```

Portfolio can subscribe to multiple atoms in `:params`. If you set `:params` to
a map or a vector, Portfolio find all contained atoms and re-render the scene
when any of them change.

## Scene names

Portfolio will "humanize" the scene symbol id for a title. If you don't like the
result, you can set `:title` to override the UI title:

```clj
(defscene default-scenario
  :title "'tis the default scenario!"
  :params {:title "Hello world!"}
  [params portfolio-opts]
  [:h1 (:title param)])
```

With `:title`, this will list as `'tis the default scenario!` in the sidebar
instead of `Default scenario`.

## Reference

Currently supported key/value pairs:

- `:title` - Give the scene a nice string name
- `:params` - The initial parameter passed to the component function - also
  aliased as `:param`
- `:on-mount` - A function called when the scene is initially mounted. The
  function is passed the component arguments.
- `:on-unmount` - A function called when the scene is removed from the DOM. The
  function is passed the component arguments.
- `:collection` - What collection the scene belongs to. See [Organizing scenes](./organization.md).
- `:icon` - An icon to display instead of the default bookmark ([about icons](./organization.md#icons)).
- `:selected-icon` - Specific icon to display when the scene is selected.
- `:icon-color` - The color of the scene icon.
- `:selected-icon-color` - Specific icon color for when the scene is selected.

[All documentation](./index.md)
