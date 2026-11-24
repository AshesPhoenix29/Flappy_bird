# Flappy Bird web game

A standalone HTML5 Canvas version of the Java game, optimized for portrait
mobile browsers and playable on desktop. It is plain HTML, CSS, and JavaScript;
no build step, dependencies, install, or app store is required.

## Run locally

Open `index.html` in a browser, or serve this directory over HTTP:

```powershell
python -m http.server 8000 --directory web
```

Then visit <http://localhost:8000/>.

## Controls

- Tap or click the game area to start and flap.
- Press **Space** or **Up Arrow** to flap.
- Tap the pause icon or press **P** to pause or resume.
- After a collision, tap the game or press **R** to return to the start screen.

The page prevents touch scrolling while playing. Best score is saved in browser
`localStorage` on the device and origin where the game is played. Audio is not
included because no licensed sound assets are present.

## GitHub Pages

The repository workflow at `.github/workflows/pages.yml` publishes this
directory using GitHub Pages. It runs on pushes to `main` and can also be run
from the Actions tab. The repository's Pages source is configured as **GitHub
Actions**.

The web files use relative URLs, so the game works both locally and at
<https://ashesphoenix29.github.io/Flappy_bird/> under the repository path.

## Browser testing

| Browser/environment | Result |
| --- | --- |
| Chrome, desktop | Pass: mouse/keyboard input, game states, scoring, collision, restart, and persistence. |
| Chrome, 390x844 and 844x390 emulated viewports | Pass: touch-style input, pause/resume, scoring, restart, storage, and no page scrolling. |
| Edge, desktop | Pass: page load, mouse start, pause, and no JavaScript errors. |
| Android Chrome on a physical device | Not tested. |
| iOS Safari on a physical device | Not tested. |
| Desktop Firefox | Not tested; Firefox is not installed in the available environment. |

Viewport emulation does not replace testing on physical Android or iOS devices.
