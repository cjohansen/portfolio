(function () {
  var ready = true;
  console.log("Portfolio accessibility plugin ready");

  window.addEventListener("message", function (e) {
    if (e.data.event === "scene-rendered" && ready) {
      ready = false;
      setTimeout(function () {
        window.axe.run(document.getElementById("canvas"))
          .then(function (results) {
            ready = true;
            window.postMessage({
              action: "assoc-in",
              data: JSON.stringify([["axe-results", e.data["scene-id"]], results])
            });
          });
      }, 100);
    }
  });
}());
