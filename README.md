# GatherinG
![GatheringG Sketch](https://github.com/JamesTwisleton/gathering/blob/master/images/gathering.jpeg)

## Live demo
Pushes to master will trigger a redeployment to the [dev server](http://64.227.45.141:3000/gathering).

## Development
### Requirements
[Node.js 12](https://nodejs.org/en/download/)

[Java 14](https://www.oracle.com/java/technologies/javase/jdk14-archive-downloads.html)

[Maven](https://maven.apache.org/download.cgi)
### Running the server
Just do it you melt.
### Running the client
If you're not running the server locally, update `.env` with your server address.

In the js folder
```node
npm install
npm start
```
## TODO
### Server
- [x] Loads user state from disk
- [x] Handles connections from users
- [x] Creates users
- [x] Maintains user state
- [x] Saves user state to disk
- [x] Handles user movement requests
- [x] Updates users on world state when it changes
- [ ] Send individual user updates rather than entire world
- [ ] Chat
- [ ] Auth
### Client
- [x] Draws grid
- [x] Loads user state from server
- [x] Draws users on canvas
- [x] Handles world state updates from server
- [ ] Handle individual user updates rather than entire world
- [ ] Send movement requests
- [ ] Layout
- [ ] Chat
- [ ] Auth