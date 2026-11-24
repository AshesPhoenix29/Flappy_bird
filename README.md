# Flappy Bird

A Java Swing Flappy Bird game.

## Download and run without Java

The Windows and macOS app packages include their own Java runtime. You do not need to install Java separately. GitHub Actions builds these packages when a version tag is pushed; they are attached to that version's release.

Download the package for your computer from the [GitHub Releases page](https://github.com/AshesPhoenix29/Flappy_bird/releases).

### Windows

1. Download `FlappyBird-Windows.zip`.
2. Extract the ZIP file.
3. Run `FlappyBird.exe` from the extracted files.

Windows may show a SmartScreen warning because the app is not code-signed.

### macOS

1. Download `FlappyBird-macOS.zip`.
2. Extract the ZIP file.
3. Open `FlappyBird.app`.

Because the app is not signed or notarized with an Apple Developer certificate, macOS may warn that it cannot verify the developer.

## Run the JAR

The JAR is also available on the Releases page. It requires Java 11 or later.

Check for Java:

```text
java -version
```

On Windows, open PowerShell in the download folder and run:

```powershell
java -jar .\FlappyBird.jar
```

On macOS, open Terminal and run:

```bash
cd ~/Downloads
java -jar FlappyBird.jar
```

## Controls

- **Space** or click to flap
- Click the pause button to pause or resume
- Press **R** to restart after game over

## Bird image

The app packages do not include the local bird image, so the game uses its built-in red-circle fallback. Only include artwork that you have permission to redistribute.
