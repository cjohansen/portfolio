# Welcome to Portfolio!

Portfolio is running and ready to help you build UI components. To see your
components, you will need to create examples with [`defscene`](./defscene.md)
and make sure the examples are required in the namespace where you called
[`portfolio.ui/start!`](./ui-start.md).

```clj
(require '[portfolio.react-18 :refer-macros [defscene]])

(defscene my-first-scene
  [:h1 "Hello world!"])

(defscene my-second-scene
  :params {:text "Hello world"}
  [params]
  (MyComponent params))
```

Depending on your component library of choice you may need to require a
different namespace, see [`defscene` docs](./defscene.md).

## Next steps

- [Load custom CSS](./custom-css.md)
- [Customize the Portfolio UI](./customize-ui.md)
- [Using defscene](./defscene.md)

## Help and feedback

If you run into any issues, get stuck, or have any feedback at all, don't
hesitate to get in touch. Feel free to open issues on the [github
repo](https://github.com/cjohansen/portfolio/issues), or join us in [#portfolio
on the Clojurians Slack](https://clojurians.slack.com).

[All documentation](./index.md)
