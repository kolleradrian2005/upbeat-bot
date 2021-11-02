# UpBeat

This repository contains the entire code of the discord bot. Every file is needed to placed in root folder. If exported Jar file, place all files in the directory the file is started from.

## Setting up media

### Discord

You have to provide a __token__ for the discord bot in irder to log in. Path: `./config.json`

```json
{
    ...,
    "token":"<token>"
}
```

### YouTube

You have to provide an __API key__ in order to use YouTube API. Path: `./apiKey.txt`

### Spotify

You have to provide __clientId__, __clientSecret__. Path: `./config.json`

```json
{
    ...,
    "clientId":"<clientId>",
    "clientSecret":"<clientSecret>"
}
```

### Webserver

__SSL certification__: Provide certification in order to enable SSL. Path: `./cert/` <br>
_(Read `./cert/readme.txt` for further instructions.)_

## Exporting jar file

Run gradle task __shadowJar__ in order to build jar file with all its dependencies. Output path: `./build/libs/Bot-all.jar`

## Notes

Webserver integration needs rethink, because of open-port-shortage.

## Credits

- Adrian - developer
- Marci - Ideas, solutions