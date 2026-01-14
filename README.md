# echoes-from-beyond

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

A Lovecraftian Hytale mod with an emphasis on narrative and storytelling.

## Table of Contents

- [Security](#security)
- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [API](#api)
- [Contributing](#contributing)
- [License](#license)

## Security

See `SECURITY.md` for our vulnerability disclosure policy.

## Background

This project was made to act as an early story-based experience that will be playable before the official Adventure Mode.

It has a very different theme from said official mode and we intend to integrate with rather than replace it.

## Install

This project is built using [Gradle](https://gradle.org/), and requires the user to have a local copy of the Hytale server to build against.

```
./gradlew build
```

If there are formatting errors, run `spotlessApply` before `build`. If tests succeed, the mod jar (containing all required dependencies) will be output to `main/build/libs/main.jar`.

### Setting up the Hytale SDK
For the time being (until we can access a proper Maven dependency) users have to build against their local copy of the Hytale server. To do this, create a file named `.hytale` in the root project directory (same level as this readme).

Then, find the path to the server jar. This is generally dependent on your OS:

```
Linux: $XDG_DATA_HOME/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/release/package/game/latest/Server/HytaleServer.jar
Windows: %appdata%\Hytale\install\release\package\game\latest\Server\HytaleServer.jar
MacOS: ~/Application Support/Hytale/install/release/package/game/latest/Server/HytaleServer.jar
```

Once you have located the server, you will want to convert the file to an _absolute path_ as appropriate for your operating system. Then, copy-paste the absolute path into the `.hytale` file. If `./gradlew build` completes without errors, the setup worked.

`.hytale` is gitignore'd and should not be commited to the repo, as it will be different for every user.

## Usage

**In-progress:** Basic examples of using our mod API.

## API

**In Progress**: We don't know what facilities Hytale provides for inter-mod communication yet.

## Contributing

See [the contributing file](CONTRIBUTING.md)!

PRs accepted.

Small note: If editing `README.md`, please conform to the [standard-readme](https://github.com/RichardLitt/standard-readme) specification.

## License

[GPLv3 © Echoes from Beyond Team.](./LICENSE)
