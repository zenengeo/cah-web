package me.itzg.cahweb.services;

import lombok.extern.slf4j.Slf4j;
import me.itzg.cahweb.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.EmitResult;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomService {

    private final RoomStorage roomStorage;
    private final CardsProvider cardsProvider;

    public RoomService(RoomStorage roomStorage,
        CardsProvider cardsProvider
    ) {
        this.roomStorage = roomStorage;
        this.cardsProvider = cardsProvider;
    }

    public RoomInfo createRoom() {
        final String roomCode = RandomStringUtils.secure().next(4).toUpperCase();

        roomStorage.getRoom(roomCode);

        return RoomInfo.builder()
            .roomCode(roomCode)
            .build();
    }

    public void addGhostPlayer(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);

        final String playerName = cardsProvider.getSomeWhiteCards(
                1,
                WhiteCard::useAsName)
            .iterator().next().text();

        final PlayerInfo playerInfo = PlayerInfo.builder()
            .playerName(playerName)
            .playerId(generatePlayerId())
            .ghost(true)
            .build();
        room.addGhostPlayer(playerInfo);

        log.debug("Ghost player={} joined room={}", playerInfo, roomCode);

        tellHost(room, roomCode, GameEvent.builder()
            .action(Action.PlayerJoined)
            .build());
    }

    public PlayerInfo join(String roomCode, String playerName) {
        log.debug("Player joining room={} name={}", roomCode, playerName);
        final Room room = roomStorage.getRoom(roomCode);

        final PlayerInfo playerInfo = PlayerInfo.builder()
            .playerName(playerName)
            .playerId(generatePlayerId())
            .build();

        room.addPlayer(playerInfo);

        tellHost(room, roomCode, GameEvent.builder()
            .action(Action.PlayerJoined)
            .build());

        log.debug("Player={} joined room={}", playerInfo, roomCode);

        return playerInfo;
    }

    public PlayerInfo rejoin(String roomCode, String playerName, String playerId) {
        log.debug("Player re-joining room={} name={} id={}", roomCode, playerName, playerId);
        final Room room = roomStorage.getRoom(roomCode);

        final PlayerInfo updated = PlayerInfo.builder()
            .playerId(playerId)
            .playerName(playerName)
            .build();
        room.ensurePlayer(updated);

        tellHost(room, roomCode, GameEvent.builder()
            .action(Action.PlayerJoined)
            .build());

        return updated;
    }

    private String generatePlayerId() {
        return UUID.randomUUID().toString();
    }

    private void tellHost(Room room, String roomCode, GameEvent event) {
        log.debug("Telling Host of room={} event={}", roomCode, event);
        room.hostSink().emitNext(
            event,
            (signalType, emitResult) -> {
                log.error("Emit failed with signalType={}, emitResult={}", signalType, emitResult);
                return (emitResult == EmitResult.FAIL_ZERO_SUBSCRIBER);
            });
    }

    private void tellPlayers(Room room, String roomCode, GameEvent event) {
        log.debug("Telling players of room={} event={}", roomCode, event);
        room.playersSink().emitNext(
            event,
            EmitFailureHandler.FAIL_FAST);
    }

    public Flux<GameEvent> listenToHostEvents(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);

        try {
            return room.hostSink().asFlux();
        } catch (Exception e) {
            log.error("Host already connected", e);
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host has already been associated with room"));
        }
    }

    public Flux<GameEvent> listenToPlayerEvents(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);

        return room.playersSink().asFlux();
    }

    public Collection<PlayerInfo> players(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);
        return room.players().stream()
            .sorted(Comparator.comparing(PlayerInfo::playerName, String::compareToIgnoreCase))
            .toList();
    }

    public void startRound(String roomCode) {
        log.debug("Starting round for room={}", roomCode);
        final Room room = roomStorage.getRoom(roomCode);

        room.startRound();

        room.blackCard(
            room.dealNextBlackCard(() -> cardsProvider.shuffleDeckOfBlackCards(
                // can only handle one slot for now
                blackCard -> blackCard.slots() == 1
            ))
        );

        final GameEvent event = GameEvent.builder()
            .action(Action.RoundStarted)
            .build();
        tellHost(room, roomCode, event);
        tellPlayers(room, roomCode, event);
    }

    public int getCurrentRoundNumber(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);
        return room.round();
    }

    public BlackCard getBlackcard(String roomCode) {
        log.debug("Getting black card for room={}", roomCode);
        final Room room = roomStorage.getRoom(roomCode);

        return room.blackCard();
    }

    public List<DealtCard> dealWhiteCards(String roomCode, String playerId, int count) {
        final Room room = roomStorage.getRoom(roomCode);

        final List<DealtCard> hand = cardsProvider.getSomeWhiteCards(
                count,
                ensureNotDealt(room)
            ).stream()
            .map(whiteCard -> DealtCard.builder()
                .card(whiteCard)
                .id(room.allocateCardId())
                .build())
            .toList();

        room.saveDealtHand(playerId, hand);

        log.debug("Player={} in room={} dealt whiteCards={}",
            playerId, roomCode, hand);

        return hand;
    }

    private Predicate<WhiteCard> ensureNotDealt(Room room) {
        return card -> !room.hasDealtCardText(card.text());
    }

    public void submitCard(String roomCode, String playerId, int cardId) {
        log.debug("Player={} in room={} submitted card={}", playerId, roomCode, cardId);
        final Room room = roomStorage.getRoom(roomCode);

        tellHost(room, roomCode, GameEvent.builder()
            .action(Action.CardSubmitted)
            .build());

        final boolean allReady = room.submitCard(playerId, cardId);

        if (allReady) {
            submitGhostCards(room);

            final GameEvent event = GameEvent.builder()
                .action(Action.VotingStarted)
                .build();
            tellHost(room, roomCode, event);
            tellPlayers(room, roomCode, event);
        }
    }

    private void submitGhostCards(Room room) {
        final List<String> ids = room.ghostPlayersIds();

        final Collection<WhiteCard> cards = cardsProvider.getSomeWhiteCards(ids.size(), ensureNotDealt(room));

        final Iterator<String> idsItr = ids.iterator();
        final Iterator<WhiteCard> cardsItr = cards.iterator();
        while (idsItr.hasNext() && cardsItr.hasNext()) {
            final int cardId = room.allocateCardId();
            final String ghostPlayerId = idsItr.next();
            room.saveDealtHand(
                ghostPlayerId,
                List.of(
                    DealtCard.builder()
                        .id(cardId)
                        .card(cardsItr.next())
                        .build()
                )
            );
            room.submitGhostCard(ghostPlayerId, cardId);
        }
    }

    public List<DealtCard> listSubmittedCards(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);

        return room.flattenSubmittedCards();
    }

    public void vote(String roomCode, String playerId, int cardId) {
        final Room room = roomStorage.getRoom(roomCode);

        tellHost(room, roomCode, GameEvent.builder()
            .action(Action.VoteSubmitted)
            .build());

        final boolean allHaveVoted = room.submitVote(playerId, cardId);
        if (allHaveVoted) {
            tellHost(room, roomCode, GameEvent.builder()
                .action(Action.VotingCompleted)
                .build());
        }
    }

    public void tallyVotes(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);

        synchronized (room) {
            final Map<String, Integer> votes = room.votes();
            if (votes.isEmpty()) {
                throw new NotReadyForRequestException("No votes to tally");
            }
            final Map<Integer, Long> counts = votes.values().stream()
                .collect(Collectors.groupingBy(cardId -> cardId, Collectors.counting()));

            // find the max vote(s)
            final List<WinningCard> winners = counts.values().stream()
                .max(Long::compareTo)
                // winning cardId -> votes at this point
                .map(maxCount -> listWinningCards(room, counts, maxCount))
                // should never happen with empty check above
                .orElseThrow(IllegalStateException::new);

            room.winners(winners);

            room.incrementsScores(
                winners.stream()
                    .map(winningCard -> winningCard.playedBy().playerId())
            );
        }

        final GameEvent event = GameEvent.builder()
            .action(Action.WinnerRevealed)
            .build();
        tellHost(room, roomCode, event);
        tellPlayers(room, roomCode, event);
    }

    private List<WinningCard> listWinningCards(Room room, Map<Integer, Long> counts, long winningCount) {
        return counts.entrySet().stream()
            // reverse lookup card from counts that match the winning amount
            .filter(cardIdCountEntry -> cardIdCountEntry.getValue().equals(winningCount))
            // with only winning cardId -> votes associate dealt card and
            // player that submitted it with how many votes they got
            .map(cardIdVotesEntry ->
                buildWinningCard(room,
                    cardIdVotesEntry.getKey(),
                    winningCount))
            .toList();
    }

    private WinningCard buildWinningCard(Room room, Integer cardId, long winningVotesCount) {
        return WinningCard.builder()
            .votes(winningVotesCount)
            .playedBy(room.findPlayerThatSubmitted(cardId))
            .card(room.findDealtCard(cardId))
            .build();
    }


    public List<WinningCard> listWinners(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);
        return room.winners();
    }

    public Collection<PlayerScore> listPlayerScores(String roomCode) {
        final Room room = roomStorage.getRoom(roomCode);
        return room.playerScores().stream()
            .sorted(Comparator.comparing(PlayerScore::score, Collections.reverseOrder()))
            .toList();
    }
}
