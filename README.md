# echoes-from-beyond

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

Hytale mods and supporting libraries made by the Echoes from Beyond team.

This repository contains:
* `main`: A Lovecraftian Hytale mod with an emphasis on narrative and storytelling
* [codec](./codec/README.md): A library which contains a system for automatically generating codecs
* `util`: A general utility library

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

This project was originally made to act as an early story-based experience that will be playable before the official Adventure Mode.

It has a very different theme from said official mode, and we intend to integrate with rather than replace it.

During development, as we've identified needs that are missing from the ecosystem, we've decided to also develop supporting libraries like `codec`.

## Install

This project is built using [Gradle](https://gradle.org/). Run `./gradlew build` to run tests and build all submodules.

Alternatively, run `./gradlew check syncPlugins`. This will assemble all Hytale plugins into the `run/mods` directory.

If there are formatting errors, run `./gradlew spotlessApply` to fix them.

### Setting up a development environment
In order to actually test any mods in-game, users need to specify their local Hytale installation. 

To do this, create a file named `.hytale` in the root project directory (same level as this readme).

Then, find the path to your Hytale installation. This is generally dependent on your OS.

Note: all of these are _pre-release_ versions! We always build against the latest pre-release server, so make sure your client is on that branch as well.

```
Linux: $XDG_DATA_HOME/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/pre-release/package/game/latest
Windows: %appdata%\Hytale\install\pre-release\package\game\latest
MacOS: ~/Application Support/Hytale/install/pre-release/package/game/latest
```

Once you have located the installation, you will need to convert the file to an _absolute path_ as appropriate for your operating system. Then, copy-paste the absolute path into the `.hytale` file.

`.hytale` is gitignore'd and should not be commited to the repo, as it will be different for every user. 

Finally, execute `./gradlew runDevServer` to launch a Hytale server on your local machine for testing. This task will also ensure that your development server is using the same version as your client. Ensure that you apply the latest available updates/patches through the Hytale launcher _before_ running this command!

All server files are created inside a gitignore'd directory named `run`, relative to the project root. This includes logs, worlds, configs, etc. All of these will persist between launches, and the total size can grow over time as logs accrue and the world is explored. Execute `./gradlew cleanRunDir` to restore `run` to a "minimal" state. **Warning: this will recursively delete EVERYTHING in the run directory except for top-level .json files, `auth.enc`, `Assets.zip`, and `HytaleServer.jar`!**

First time launch will require you to authenticate the server with your Hytale account. Follow the instructions in the terminal (the steps are the same as in [the Hytale server manual](https://support.hytale.com/hc/en-us/articles/45326769420827-Hytale-Server-Manual)). Run the Hytale server command `/auth persistence Encrypted` after to persist your credentials and avoid needing to sign in on every launch.

Choosing this authentication storage method will create a file named `auth.enc` inside the `run` directory. **Do not commit or share this file.** It should already be gitignore'd as a consequence of being within `run`.

[IntelliJ IDEA](https://www.jetbrains.com/idea/) users can use the provided run configuration `Launch development server` to run or debug the server, instead of executing the Gradle task manually.

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
