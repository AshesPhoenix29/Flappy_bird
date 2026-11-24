(() => {
  "use strict";

  const DESIGN_WIDTH = 360;
  const DESIGN_HEIGHT = 640;
  const GROUND_HEIGHT = 80;
  const PIPE_WIDTH = 60;
  const PIPE_GAP = 150;
  const PIPE_INTERVAL_FRAMES = 100;
  const BIRD_SIZE = 45;
  const BEST_SCORE_KEY = "flappyBirdBestScore";
  const canvas = document.getElementById("game");
  const context = canvas.getContext("2d");
  const stage = document.querySelector(".game-shell");
  const status = document.getElementById("game-status");
  const storageNote = document.getElementById("storage-note");
  const game = {
    state: "start",
    score: 0,
    bestScore: 0,
    newBest: false,
    paused: false,
    frameCount: 0,
    pipeTimer: 0,
    groundScroll: 0,
    scale: 1,
    width: DESIGN_WIDTH,
    height: DESIGN_HEIGHT,
    lastFrame: 0,
    bird: { x: 150, y: 250, width: BIRD_SIZE, height: BIRD_SIZE, velocity: 0 },
    pipes: [],
    pauseButton: { x: 16, y: 18, width: 38, height: 38 },
    playButton: null
  };

  function readBestScore() {
    try {
      const saved = Number.parseInt(window.localStorage.getItem(BEST_SCORE_KEY) || "0", 10);
      return Number.isSafeInteger(saved) && saved >= 0 ? saved : 0;
    } catch (error) {
      storageNote.hidden = false;
      console.warn("Best-score storage is unavailable.", error);
      return 0;
    }
  }

  function saveBestScore() {
    try {
      window.localStorage.setItem(BEST_SCORE_KEY, String(game.bestScore));
    } catch (error) {
      storageNote.hidden = false;
      console.warn("Could not save the best score.", error);
    }
  }

  function announce(message) {
    status.textContent = message;
  }

  function resizeCanvas() {
    const bounds = stage.getBoundingClientRect();
    const ratio = Math.min(window.devicePixelRatio || 1, 2);
    const nextScale = bounds.width / DESIGN_WIDTH;

    if (game.scale !== nextScale) {
      const scaleChange = nextScale / game.scale;
      game.bird.x *= scaleChange;
      game.bird.y *= scaleChange;
      game.bird.width *= scaleChange;
      game.bird.height *= scaleChange;
      game.bird.velocity *= scaleChange;
      game.pipes.forEach((pipe) => {
        pipe.x *= scaleChange;
        pipe.gapY *= scaleChange;
      });
      game.groundScroll *= scaleChange;
    }

    game.scale = nextScale;
    game.width = bounds.width;
    game.height = bounds.height;
    canvas.width = Math.round(bounds.width * ratio);
    canvas.height = Math.round(bounds.height * ratio);
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
  }

  function scaled(value) {
    return value * game.scale;
  }

  function resetGame() {
    game.state = "start";
    game.score = 0;
    game.paused = false;
    game.newBest = false;
    game.pipeTimer = 0;
    game.groundScroll = 0;
    game.bird.x = scaled(150);
    game.bird.y = scaled(250);
    game.bird.width = scaled(BIRD_SIZE);
    game.bird.height = scaled(BIRD_SIZE);
    game.bird.velocity = 0;
    game.pipes = [{ x: scaled(DESIGN_WIDTH), gapY: scaled(180), scored: false }];
    game.playButton = null;
    announce(`Ready. Best score ${game.bestScore}. Tap or click to start.`);
  }

  function flap() {
    game.bird.velocity = scaled(-8.2 * 60);
  }

  function startGame() {
    game.state = "playing";
    flap();
    announce("Game started. Tap or click to flap.");
  }

  function finishGame() {
    if (game.state === "gameover") return;
    game.state = "gameover";
    game.newBest = game.score > game.bestScore;
    if (game.newBest) {
      game.bestScore = game.score;
      saveBestScore();
    }
    announce(`Game over. Score ${game.score}. Best score ${game.bestScore}. Press R or tap to play again.`);
  }

  function togglePause() {
    if (game.state !== "playing") return;
    game.paused = !game.paused;
    announce(game.paused ? "Game paused. Press P or tap the pause button to resume." : "Game resumed.");
  }

  function pointInRect(point, rect) {
    return point.x >= rect.x && point.x <= rect.x + rect.width
      && point.y >= rect.y && point.y <= rect.y + rect.height;
  }

  function handlePointer(event) {
    event.preventDefault();
    canvas.focus({ preventScroll: true });
    const bounds = canvas.getBoundingClientRect();
    const point = { x: event.clientX - bounds.left, y: event.clientY - bounds.top };

    if (game.state === "start") {
      startGame();
    } else if (game.state === "gameover") {
      resetGame();
    } else if (pointInRect(point, game.pauseButton)) {
      togglePause();
    } else if (game.paused) {
      togglePause();
    } else {
      flap();
    }
  }

  function handleKey(event) {
    if (event.repeat) return;
    const key = event.key.toLowerCase();
    if (event.code === "Space" || key === "arrowup") {
      event.preventDefault();
      if (game.state === "start") startGame();
      else if (game.state === "playing" && game.paused) togglePause();
      else if (game.state === "playing") flap();
    } else if (key === "p") {
      event.preventDefault();
      togglePause();
    } else if (key === "r" && game.state === "gameover") {
      event.preventDefault();
      resetGame();
    }
  }

  function createPipe() {
    const playfieldHeight = game.height - scaled(GROUND_HEIGHT);
    const minGapY = scaled(60);
    const maxGapY = playfieldHeight - scaled(PIPE_GAP) - scaled(60);
    const gapY = Math.round(minGapY + Math.random() * Math.max(0, maxGapY - minGapY));
    game.pipes.push({ x: game.width, gapY, scored: false });
  }

  function intersects(a, b) {
    return a.x < b.x + b.width && a.x + a.width > b.x
      && a.y < b.y + b.height && a.y + a.height > b.y;
  }

  function update(deltaSeconds) {
    if (game.state !== "playing" || game.paused) return;

    const bird = game.bird;
    bird.velocity = Math.min(bird.velocity + scaled(0.42 * 60 * 60) * deltaSeconds, scaled(11 * 60));
    bird.y += bird.velocity * deltaSeconds;

    const pipeSpeed = scaled(2.8 * 60);
    game.groundScroll = (game.groundScroll + pipeSpeed * deltaSeconds) % scaled(28);
    game.pipes.forEach((pipe) => {
      pipe.x -= pipeSpeed * deltaSeconds;
      if (!pipe.scored && pipe.x + scaled(PIPE_WIDTH / 2) < bird.x + bird.width / 2) {
        pipe.scored = true;
        game.score += 1;
        announce(`Score ${game.score}. Best score ${game.bestScore}.`);
      }

      const birdBounds = {
        x: bird.x + scaled(3),
        y: bird.y + scaled(3),
        width: bird.width - scaled(6),
        height: bird.height - scaled(6)
      };
      const upperPipe = { x: pipe.x, y: 0, width: scaled(PIPE_WIDTH), height: pipe.gapY };
      const lowerY = pipe.gapY + scaled(PIPE_GAP);
      const lowerPipe = {
        x: pipe.x,
        y: lowerY,
        width: scaled(PIPE_WIDTH),
        height: Math.max(0, game.height - scaled(GROUND_HEIGHT) - lowerY)
      };
      if (intersects(birdBounds, upperPipe) || intersects(birdBounds, lowerPipe)) finishGame();
    });

    if (bird.y + bird.height >= game.height - scaled(GROUND_HEIGHT)) finishGame();

    game.pipeTimer += deltaSeconds * 60;
    if (game.pipeTimer >= PIPE_INTERVAL_FRAMES) {
      game.pipeTimer -= PIPE_INTERVAL_FRAMES;
      createPipe();
    }
    game.pipes = game.pipes.filter((pipe) => pipe.x + scaled(PIPE_WIDTH) >= 0);
  }

  function fillRoundedRect(x, y, width, height, radius, fillStyle, strokeStyle) {
    const r = Math.min(radius, width / 2, height / 2);
    context.beginPath();
    context.moveTo(x + r, y);
    context.lineTo(x + width - r, y);
    context.quadraticCurveTo(x + width, y, x + width, y + r);
    context.lineTo(x + width, y + height - r);
    context.quadraticCurveTo(x + width, y + height, x + width - r, y + height);
    context.lineTo(x + r, y + height);
    context.quadraticCurveTo(x, y + height, x, y + height - r);
    context.lineTo(x, y + r);
    context.quadraticCurveTo(x, y, x + r, y);
    context.closePath();
    if (fillStyle) {
      context.fillStyle = fillStyle;
      context.fill();
    }
    if (strokeStyle) {
      context.strokeStyle = strokeStyle;
      context.stroke();
    }
  }

  function drawCloud(x, y, size, color) {
    context.fillStyle = color;
    context.beginPath();
    context.ellipse(x + size * 0.25, y + size * 0.18, size * 0.25, size * 0.14, 0, 0, Math.PI * 2);
    context.ellipse(x + size * 0.48, y, size * 0.23, size * 0.2, 0, 0, Math.PI * 2);
    context.ellipse(x + size * 0.73, y + size * 0.15, size * 0.27, size * 0.16, 0, 0, Math.PI * 2);
    context.fill();
  }

  function drawSky() {
    const groundY = game.height - scaled(GROUND_HEIGHT);
    const dark = game.score >= 25;
    const dusk = game.score >= 10;
    const top = dark ? "#141936" : dusk ? "#e27b58" : "#4ec0ca";
    const bottom = dark ? "#1d2a4a" : "#ffe0b0";
    if (dusk) {
      const gradient = context.createLinearGradient(0, 0, 0, groundY);
      gradient.addColorStop(0, top);
      gradient.addColorStop(1, bottom);
      context.fillStyle = gradient;
    } else {
      context.fillStyle = top;
    }
    context.fillRect(0, 0, game.width, groundY);

    if (dark) {
      for (let i = 0; i < 28; i += 1) {
        const x = scaled((i * 67 + 23) % DESIGN_WIDTH);
        const y = scaled((i * 43 + 31) % 500);
        const twinkle = 0.55 + (Math.sin(game.frameCount * 0.08 + i) + 1) * 0.2;
        context.fillStyle = `rgba(255, 255, 220, ${twinkle})`;
        context.fillRect(x, y, scaled(i % 5 === 0 ? 3 : 2), scaled(i % 5 === 0 ? 3 : 2));
      }
      context.fillStyle = "rgba(255, 247, 205, 0.16)";
      context.beginPath();
      context.arc(game.width - scaled(52), scaled(64), scaled(27), 0, Math.PI * 2);
      context.fill();
      context.fillStyle = "#fff7cd";
      context.beginPath();
      context.arc(game.width - scaled(52), scaled(64), scaled(21), 0, Math.PI * 2);
      context.fill();
    } else {
      const sunY = dusk ? groundY / 2 : scaled(88);
      context.fillStyle = "rgba(255, 237, 167, 0.75)";
      context.beginPath();
      context.arc(game.width - scaled(55), sunY, scaled(21), 0, Math.PI * 2);
      context.fill();
      const cloudColor = dusk ? "rgba(255, 204, 165, 0.53)" : "rgba(255, 255, 255, 0.51)";
      const frame = game.frameCount;
      const cloudX = (base, rate, offset) =>
        scaled((base + frame * rate) % (DESIGN_WIDTH + offset) - offset * 0.7);
      drawCloud(cloudX(65, 0.25, 90), game.height * 0.19, scaled(76), cloudColor);
      drawCloud(cloudX(260, 0.18, 100), game.height * 0.36, scaled(58), cloudColor);
      drawCloud(cloudX(145, 0.12, 110), game.height * 0.27, scaled(68), cloudColor);
      if (dusk) {
        context.fillStyle = "rgba(117, 75, 82, 0.35)";
        context.beginPath();
        context.ellipse(scaled(85), groundY - scaled(32), scaled(120), scaled(50), 0, 0, Math.PI * 2);
        context.ellipse(scaled(300), groundY - scaled(24), scaled(115), scaled(42), 0, 0, Math.PI * 2);
        context.fill();
      }
    }
    drawScenery(groundY, dark, dusk);
  }

  function drawScenery(groundY, dark, dusk) {
    context.fillStyle = dark ? "#243d4c" : dusk ? "#827766" : "#8ed593";
    context.beginPath();
    context.ellipse(scaled(30), groundY - scaled(22), scaled(65), scaled(34), 0, 0, Math.PI * 2);
    context.ellipse(scaled(112), groundY - scaled(23), scaled(86), scaled(39), 0, 0, Math.PI * 2);
    context.ellipse(scaled(264), groundY - scaled(19), scaled(108), scaled(34), 0, 0, Math.PI * 2);
    context.fill();
    context.fillStyle = dark ? "#1b303e" : dusk ? "#747565" : "#57ae8b";
    [
      [18, 32, 27], [58, 45, 24], [101, 26, 34], [159, 38, 29],
      [205, 27, 27], [253, 43, 31], [300, 31, 25]
    ].forEach(([x, h, width]) => {
      context.fillRect(scaled(x), groundY - scaled(h), scaled(width), scaled(h));
    });
  }

  function drawPipe(pipe) {
    const x = pipe.x;
    const width = scaled(PIPE_WIDTH);
    const lowerY = pipe.gapY + scaled(PIPE_GAP);
    const lowerHeight = Math.max(0, game.height - scaled(GROUND_HEIGHT) - lowerY);
    const gradient = context.createLinearGradient(x, 0, x + width, 0);
    gradient.addColorStop(0, "#7ce131");
    gradient.addColorStop(1, "#39991e");
    context.fillStyle = gradient;
    context.fillRect(x, 0, width, pipe.gapY);
    context.fillRect(x, lowerY, width, lowerHeight);
    context.fillStyle = "#b1f552";
    context.fillRect(x + scaled(8), 0, scaled(8), pipe.gapY);
    context.fillRect(x + scaled(8), lowerY, scaled(8), lowerHeight);
    context.fillStyle = "#2b701c";
    context.fillRect(x + width - scaled(10), 0, scaled(7), pipe.gapY);
    context.fillRect(x + width - scaled(10), lowerY, scaled(7), lowerHeight);
    context.lineWidth = scaled(2.5);
    context.strokeStyle = "#0f1910";
    context.strokeRect(x, 0, width, pipe.gapY);
    context.strokeRect(x, lowerY, width, lowerHeight);
    drawPipeCap(x, pipe.gapY - scaled(17));
    drawPipeCap(x, lowerY - scaled(15));
  }

  function drawPipeCap(x, y) {
    const width = scaled(PIPE_WIDTH + 10);
    const capX = x - scaled(5);
    const gradient = context.createLinearGradient(capX, y, capX + width, y);
    gradient.addColorStop(0, "#8ceb3a");
    gradient.addColorStop(1, "#46b11f");
    fillRoundedRect(capX, y, width, scaled(34), scaled(3), gradient, "#0f1910");
    context.fillStyle = "#b1f552";
    context.fillRect(x + scaled(3), y + scaled(3), scaled(8), scaled(28));
  }

  function drawGround() {
    const groundY = game.height - scaled(GROUND_HEIGHT);
    const dark = game.score >= 25;
    const dusk = game.score >= 10;
    context.fillStyle = dark ? "#36694e" : dusk ? "#6f8f51" : "#70be4a";
    context.fillRect(0, groundY, game.width, scaled(16));
    const dirt = context.createLinearGradient(0, groundY + scaled(16), 0, game.height);
    dirt.addColorStop(0, dark ? "#434b59" : dusk ? "#ac775a" : "#dada93");
    dirt.addColorStop(1, dark ? "#2a3141" : dusk ? "#77504e" : "#dada93");
    context.fillStyle = dirt;
    context.fillRect(0, groundY + scaled(16), game.width, scaled(64));
    context.strokeStyle = dark ? "#8b9e99" : dusk ? "#d2aa7f" : "#beb971";
    context.lineWidth = scaled(1);
    for (let x = -game.groundScroll; x < game.width; x += scaled(12)) {
      context.beginPath();
      context.moveTo(x, groundY + scaled(29));
      context.lineTo(x + scaled(5), groundY + scaled(17));
      context.stroke();
    }
    context.fillStyle = dark ? "#254e40" : "#529d39";
    context.fillRect(0, groundY, game.width, scaled(4));
    for (let x = -game.groundScroll; x < game.width; x += scaled(14)) {
      context.beginPath();
      context.moveTo(x, groundY + scaled(13));
      context.lineTo(x + scaled(4), groundY + scaled(4));
      context.lineTo(x + scaled(8), groundY + scaled(13));
      context.closePath();
      context.fill();
    }
  }

  function drawBird() {
    const bird = game.bird;
    const angle = game.state === "playing" ? Math.max(-25, Math.min(70, bird.velocity / scaled(60) * 9)) : 0;
    const yOffset = game.state === "start" ? Math.sin(game.frameCount * 0.08) * scaled(8) : 0;
    context.save();
    context.translate(bird.x + bird.width / 2, bird.y + yOffset + bird.height / 2);
    context.rotate(angle * Math.PI / 180);
    context.fillStyle = "#e5253a";
    context.beginPath();
    context.ellipse(0, 0, bird.width / 2, bird.height / 2, 0, 0, Math.PI * 2);
    context.fill();
    context.restore();
  }

  function drawCenteredText(text, x, y, font, color, strokeColor) {
    context.font = font;
    context.textAlign = "center";
    context.textBaseline = "alphabetic";
    if (strokeColor) {
      context.lineWidth = scaled(4);
      context.strokeStyle = strokeColor;
      context.strokeText(text, x, y);
    }
    context.fillStyle = color;
    context.fillText(text, x, y);
  }

  function drawScore() {
    drawCenteredText(String(game.score), game.width / 2, scaled(70),
      `bold ${scaled(50)}px Arial`, "#fff", "#172b28");
  }

  function drawPauseButton() {
    const button = game.pauseButton;
    fillRoundedRect(button.x - scaled(2), button.y - scaled(2),
      scaled(42), scaled(42), scaled(8), "#142026");
    fillRoundedRect(button.x, button.y, button.width, button.height,
      scaled(7), "#f5700e");
    context.fillStyle = "#fff";
    if (game.paused) {
      context.beginPath();
      context.moveTo(button.x + scaled(14), button.y + scaled(9));
      context.lineTo(button.x + scaled(14), button.y + scaled(29));
      context.lineTo(button.x + scaled(27), button.y + scaled(19));
      context.closePath();
      context.fill();
    } else {
      fillRoundedRect(button.x + scaled(10), button.y + scaled(9),
        scaled(6), scaled(20), scaled(2), "#fff");
      fillRoundedRect(button.x + scaled(22), button.y + scaled(9),
        scaled(6), scaled(20), scaled(2), "#fff");
    }
  }

  function drawCard(x, y, width, height) {
    fillRoundedRect(x + scaled(3), y + scaled(6), width, height,
      scaled(24), "rgba(20, 43, 54, 0.22)");
    fillRoundedRect(x, y, width, height, scaled(24), "rgba(255, 255, 255, 0.94)");
  }

  function drawButton(text, x, y, width, height) {
    game.playButton = { x, y, width, height };
    fillRoundedRect(x, y + scaled(3), width, height, scaled(20), "#e9a030");
    fillRoundedRect(x, y, width, height - scaled(3), scaled(20), "#ffc242");
    drawCenteredText(text, x + width / 2, y + height / 2 + scaled(5),
      `bold ${scaled(14)}px Arial`, "#584224");
  }

  function drawBestScore() {
    const text = `BEST ${game.bestScore}`;
    context.font = `bold ${scaled(13)}px Arial`;
    const width = context.measureText(text).width + scaled(22);
    fillRoundedRect(scaled(10), scaled(14), width, scaled(29), scaled(15),
      "rgba(27, 55, 69, 0.6)");
    drawCenteredText(text, scaled(10) + width / 2, scaled(33),
      `bold ${scaled(13)}px Arial`, "#fff");
  }

  function drawStartOverlay() {
    drawBestScore();
    const titleWidth = Math.min(scaled(310), game.width - scaled(28));
    fillRoundedRect((game.width - titleWidth) / 2, scaled(38), titleWidth,
      scaled(52), scaled(18), "rgba(31, 76, 87, 0.75)");
    drawCenteredText("FLAPPY BIRD", game.width / 2, scaled(76),
      `bold ${scaled(29)}px Arial`, "#ffc242");
    const cardX = scaled(14);
    const cardWidth = game.width - scaled(28);
    drawCard(cardX, scaled(108), cardWidth, scaled(132));
    drawCenteredText("GET READY!", game.width / 2, scaled(140),
      `bold ${scaled(22)}px Arial`, "#274a53");
    drawCenteredText("TAP TO FLAP", game.width / 2, scaled(164),
      `${scaled(14)}px Arial`, "#5c7479");
    drawCenteredText("SPACE OR CLICK TO LAUNCH", game.width / 2, scaled(183),
      `${scaled(12)}px Arial`, "#5c7479");
    drawButton("PLAY", game.width / 2 - scaled(52), scaled(193),
      scaled(104), scaled(36));
  }

  function drawGameOverOverlay() {
    const cardWidth = Math.min(scaled(306), game.width - scaled(28));
    const cardHeight = scaled(286);
    const x = (game.width - cardWidth) / 2;
    const y = (game.height - cardHeight) / 2;
    drawCard(x, y, cardWidth, cardHeight);
    drawCenteredText("GAME OVER!", game.width / 2, y + scaled(43),
      `bold ${scaled(29)}px Arial`, "#ce4b43");
    drawCenteredText("SCORE", game.width / 2, y + scaled(72),
      `bold ${scaled(13)}px Arial`, "#6e7d82");
    drawCenteredText(String(game.score), game.width / 2, y + scaled(112),
      `bold ${scaled(38)}px Arial`, "#274a53");
    if (game.newBest) {
      drawCenteredText("* NEW BEST!", game.width / 2, y + scaled(135),
        `bold ${scaled(13)}px Arial`, "#f4a930");
    }
    drawCenteredText(`BEST  ${game.bestScore}`, game.width / 2, y + scaled(163),
      `bold ${scaled(14)}px Arial`, "#6e7d82");
    const medal = game.score >= 50 ? "PLATINUM MEDAL"
      : game.score >= 35 ? "GOLD MEDAL"
        : game.score >= 20 ? "SILVER MEDAL"
          : game.score >= 10 ? "BRONZE MEDAL" : "NO MEDAL YET";
    drawCenteredText(medal, game.width / 2, y + scaled(202),
      `bold ${scaled(14)}px Arial`, "#ae6f46");
    drawButton("PLAY AGAIN", game.width / 2 - scaled(78), y + scaled(226),
      scaled(156), scaled(42));
  }

  function drawPauseOverlay() {
    const width = scaled(230);
    const height = scaled(120);
    const x = (game.width - width) / 2;
    const y = (game.height - height) / 2;
    drawCard(x, y, width, height);
    drawCenteredText("PAUSED", game.width / 2, y + scaled(48),
      `bold ${scaled(25)}px Arial`, "#274a53");
    drawCenteredText("PRESS P OR TAP TO RESUME", game.width / 2, y + scaled(80),
      `${scaled(12)}px Arial`, "#5c7479");
  }

  function draw() {
    context.clearRect(0, 0, game.width, game.height);
    drawSky();
    game.pipes.forEach(drawPipe);
    drawGround();
    drawBird();
    if (game.state === "start") {
      drawStartOverlay();
    } else if (game.state === "playing") {
      drawScore();
      drawPauseButton();
      if (game.paused) drawPauseOverlay();
    } else if (game.state === "gameover") {
      drawGameOverOverlay();
    }
  }

  function frame(timestamp) {
    const elapsed = game.lastFrame ? (timestamp - game.lastFrame) / 1000 : 0;
    game.lastFrame = timestamp;
    const deltaSeconds = Math.min(elapsed, 0.05);
    if (!game.paused) game.frameCount += deltaSeconds * 60;
    update(deltaSeconds);
    draw();
    window.requestAnimationFrame(frame);
  }

  game.bestScore = readBestScore();
  resetGame();
  resizeCanvas();
  window.addEventListener("resize", resizeCanvas);
  if (window.visualViewport) window.visualViewport.addEventListener("resize", resizeCanvas);
  canvas.addEventListener("pointerdown", handlePointer);
  document.addEventListener("keydown", handleKey);
  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "visible") game.lastFrame = 0;
  });
  window.requestAnimationFrame(frame);
})();
