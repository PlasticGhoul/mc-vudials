# MC VUDials - Streacom VU1 Integration
[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green?color=green)](https://plasticghoul.github.io/mc-vudials/javadoc/)
![Release](https://img.shields.io/github/actions/workflow/status/plasticghoul/mc-vudials/release-mod.yaml?color=green)

This mod created for Minecraft integrates the Streacom VU1 dials into the game.  
The following stats are shown on the dials:
- Health
- Food Level
- Armor
- Air

## Installation
Just place the Jar file inside your mods folder (%APPDATA%\\.minecraft\mods)

## External Mod Dependencies
This mod has no dependencies as of now.  
All other dependencies are included in the mcvudials-*-all.jar.

## Configuration
After the first start Forge places a configuration file inside your Minecraft directory.  
You can find it here: %APPDATA%\\.minecraft\config\mcvudials-common.toml  

| Configuration Key | Default Value | Mandatory |
| --- | :---: | --- |
| vuServerEnabled | true | :white_check_mark: |
| vuServerHostname | localhost | :white_check_mark: |
| vuServerPort | 5340 | :white_check_mark: |
| vuServerApiKey | - | :white_check_mark: |