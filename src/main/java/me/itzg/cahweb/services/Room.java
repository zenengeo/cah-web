package me.itzg.cahweb.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.itzg.cahweb.model.BlackCard;
import me.itzg.cahweb.model.DealtCard;
import me.itzg.cahweb.model.GameEvent;
import me.itzg.cahweb.model.PlayerInfo;
import me.itzg.cahweb.model.PlayerScore;
import me.itzg.cahweb.model.WinningCard;
import reactor.core.publisher.Sinks.Many;

@Slf4j
public class Room {

    @Getter
    private final Many<GameEvent> playersSink;
    @Getter
    private final Many<GameEvent> hostSink;

    private final Map<String/*playerId*/, PlayersEntry> players = new HashMap<>();

    @Getter
    private final List<String/*playerId*/> ghostPlayersIds = new ArrayList<>();

    private final Map<String/*playerId*/, List<DealtCard>> dealtCardsByPlayer = new HashMap<>();

    private final Map<Integer/*cardId*/, DealtCard> allDealtCards = new HashMap<>();

    private final Map<String/*playerId*/, Integer/*dealt card id*/> submitted = new HashMap<>();

    @Getter
    private final Map<String/*playerId*/, Integer/*dealt card id*/> votes = new HashMap<>();

    @Getter @Setter
    private List<WinningCard> winners;

    /**
     * The current round number, where zero means no rounds have been started yet.
     */
    @Getter
    private int round;

    private int nextCardId;

    @Getter
    @Setter
    private BlackCard blackCard;

    public Room(Many<GameEvent> hostSink, Many<GameEvent> playersSink) {
        this.hostSink = hostSink;
        this.playersSink = playersSink;
        round = 0;
        nextCardId = 1;

    }

    public synchronized void addPlayer(PlayerInfo playerInfo) {
        players.put(playerInfo.playerId(),
            new PlayersEntry(playerInfo)
        );
    }

    public synchronized void addGhostPlayer(PlayerInfo playerInfo) {
        addPlayer(playerInfo);
        ghostPlayersIds.add(playerInfo.playerId());
    }

    public synchronized Collection<PlayerInfo> players() {
        return players.values().stream()
            .map(PlayersEntry::info)
            .toList();
    }

    public synchronized Collection<PlayerScore> playerScores() {
        return players.values().stream()
            .map(playersEntry -> PlayerScore.builder()
                .score(playersEntry.score())
                .info(playersEntry.info())
                .build()
            )
            .toList();
    }

    public synchronized int allocateCardId() {
        return nextCardId++;
    }

    public synchronized DealtCard findDealtCard(int cardId) {
        return dealtCardsByPlayer.values().stream()
            .flatMap(Collection::stream)
            .filter(dealtCard -> dealtCard.id() == cardId)
            .findFirst()
            .orElseThrow();
    }

    public synchronized boolean hasDealtCardText(String text) {
        return allDealtCards.values()
            .stream().anyMatch(dealtCard -> dealtCard.card().text().equals(text));
    }

    public synchronized void saveDealtHand(String playerId, List<DealtCard> hand) {
        dealtCardsByPlayer.put(playerId, hand);
        for (final DealtCard card : hand) {
            allDealtCards.put(card.id(), card);
        }
    }

    /**
     * @return true if all players have submitted their card
     */
    public synchronized boolean submitCard(String playerId, int cardId) {
        submitted.put(playerId, cardId);
        final int subscriberCount = playersSink.currentSubscriberCount();
        final int dealtPlayerCount = dealtCardsByPlayer.size();
        log.debug("Player={} submitted card={}, submitted={} subscribers={} dealt={}",
            playerId, cardId, submitted, subscriberCount, dealtPlayerCount);
        return submitted.size() >=
            Math.min(subscriberCount, dealtPlayerCount);
    }

    public synchronized void submitGhostCard(String ghostPlayerId, int cardId) {
        log.debug("Ghost player={} submitted card={}",
            ghostPlayerId, cardId);
        submitted.put(ghostPlayerId, cardId);
    }

    /**
     * @return shuffled list of the cards submitted for votes
     */
    public synchronized List<DealtCard> flattenSubmittedCards() {
        return submitted.values().stream()
            .map(allDealtCards::get)
            .collect(Collectors.collectingAndThen(
                Collectors.toCollection(ArrayList::new),
                dealtCards -> {
                    Collections.shuffle(dealtCards);
                    return dealtCards;
                })
            );
    }

    public synchronized boolean submitVote(String playerId, int cardId) {
        votes.put(playerId, cardId);
        return votes.size() >=
            Math.min(
                playersSink.currentSubscriberCount(),
                submitted.size() - ghostPlayersIds().size()
            );
    }

    public PlayerInfo findPlayerThatSubmitted(int cardId) {
        return submitted.entrySet().stream()
            // reverse lookup with card id
            .filter(entry -> entry.getValue().equals(cardId))
            // to player id
            .map(Map.Entry::getKey)
            .findFirst()
            .map(this::mustFindPlayerInfo)
            .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
    }

    private PlayerInfo mustFindPlayerInfo(String playerId) {
        final PlayersEntry entry = players.get(playerId);
        if (entry != null) {
            return entry.info();
        }
        throw new IllegalArgumentException("Player not found: " + playerId);
    }

    public synchronized void incrementsScores(Stream<String> playerIds) {
        playerIds
            .forEach(playerId -> {
                final PlayersEntry entry = players.get(playerId);
                if (entry != null) {
                    entry.score++;
                }
                else {
                    throw new IllegalStateException("PlayerId doesn't exist: "+playerId);
                }
            });
    }

    public synchronized void startRound() {
        round++;
        nextCardId = 1;
        blackCard = null;
        dealtCardsByPlayer.clear();
        allDealtCards.clear();
        submitted.clear();
        votes.clear();
        winners = null;
    }

    @Data
    static class PlayersEntry {
        final PlayerInfo info;
        int score;
    }
}
