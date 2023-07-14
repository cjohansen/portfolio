# Custom CSS

Your scenes render inside an iframe in a blank HTML page called the canvas. This
page has no default styling, and comes as bare as possible out of the package.
You might want to add some CSS files to the canvas, which can be done with
`portfolio.ui/start!`:

```clj
(require '[portfolio.ui :as ui])

(ui/start!
  {:config {:css-paths ["/myapp/custom.css"]}})
```

Add as many CSS files as you like. CSS files will automatically reload when
changed by your build process (tested and verified with shadow-cljs and
figwheel-main).

## More customization

- [Customize canvas HTML](./custom-html.md)
- [Customize the Portfolio UI](./customize-ui.md)
- [All documentation](./index.md)
