# Custom HTML

Your scenes render inside an iframe in a blank HTML page called the canvas. This
page has no default styling, and comes as bare as possible out of the package.
If you need to make more adjustments to the canvas, such as adding meta tags,
global JavaScripts etc, you can provide your own canvas:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
  {:config {:canvas-path "/my/custom/portfolio.html"}})
```

There are no requirements for how you format this file. Portfolio will add a div
with id `"canvas"` to it, in which it will render the scene. If there already is
an element with that id, it will be used instead.

## More customization

- [Customize CSS](./custom-css.md)
- [Customize the Portfolio UI](./customize-ui.md)
- [All documentation](./index.md)
