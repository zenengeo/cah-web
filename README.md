This is yet another clone of Cards Against Humanity&trade;, with all original content, in a web based format that lets you play in the same room with two or more people.

![image](https://user-images.githubusercontent.com/988985/169138459-3e41159c-7e4f-4f44-8375-bb61d076e457.png)

> **Fun Fact:** It's named "-web" since this is a port of the mechanics and content from [enenbee](https://github.com/enenbee)'s [Scratch project](https://scratch.mit.edu/projects/677362945/).

## What you need to play

- A deployment of the server, which is available [as a Docker image](https://github.com/zenengeo/cah-web/pkgs/container/cah-web)
- A computer or web-enabled TV that everyone can see
- Each player uses a web browser on their mobile device and/or computer

## Hosting the game

Going into the "Host" option, the join code that players can use at the same website is displayed at the bottom. As players join, they are shown in the listing along with any ghost players. Ghost players will randomly choose a white card that gets included for voting.

![image](https://user-images.githubusercontent.com/988985/169141372-c12660a4-f15f-4931-8687-e0d6b2068b47.png)

## Joining the game

With the join code from the host's lobby, go into the "Join" option, enter your player name and the join code.

![image](https://user-images.githubusercontent.com/988985/169142553-9c578075-2192-49c0-85be-b274dfbfa526.png)

## Playing a round

When the host starts the game or a new round, everyone's display will update to show a hand of randomly chosen white cards. Your goal is to pick the funniest, silliest, most offensive card that goes with the blank slot on the host's displayed black card.

When everyone has submitted their card for voting, each player gets the list of submitted cards to pick their favorite. Maybe it's your own. That's ok.

When all votes are submitted, the host's display will show which card(s) got the most votes and award points to the submitters. If two or more cards got the most votes, then all of those players get a point. Even ghosts can get points -- sometimes you lose to the roll of a dice.

# Deploying

## Docker Quick Start

```
docker run -d --name cah -p 8080:8080 ghcr.io/zenengeo/cah-web
```

# Development

This application is a hybrid of Spring Boot for the backend/API and a React for the frontend. The Spring Boot code is located in the usual places, such as `src/main/java` along with cards and config in `src/main/resources`. The frontend code is located in `src/main/ui`.

Building the image with `./gradlew jib` blends the frontend and backend all into one server jar.
