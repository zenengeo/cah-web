package me.itzg.cahweb.web;

import static me.itzg.cahweb.model.ListResponse.ofList;
import static me.itzg.cahweb.model.ValueResponse.ofValue;

import lombok.extern.slf4j.Slf4j;
import me.itzg.cahweb.AppProperties;
import me.itzg.cahweb.model.BlackCard;
import me.itzg.cahweb.model.CardSubmission;
import me.itzg.cahweb.model.DealHandRequest;
import me.itzg.cahweb.model.DealtCard;
import me.itzg.cahweb.model.GameEvent;
import me.itzg.cahweb.model.JoinRequest;
import me.itzg.cahweb.model.ListResponse;
import me.itzg.cahweb.model.PlayerInfo;
import me.itzg.cahweb.model.PlayerScore;
import me.itzg.cahweb.model.PlayerSession;
import me.itzg.cahweb.model.RoomInfo;
import me.itzg.cahweb.model.ValueResponse;
import me.itzg.cahweb.model.WinningCard;
import me.itzg.cahweb.services.RoomService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/room")
@Slf4j
public class RoomController {

    private static final String ATTR_PLAYER = "player";

    private final RoomService roomService;
    private final AppProperties appProperties;

    public RoomController(RoomService roomService, AppProperties appProperties) {
        this.roomService = roomService;
        this.appProperties = appProperties;
    }

    @PostMapping
    public RoomInfo createRoom() {
        return roomService.createRoom();
    }

    @PostMapping("/{roomCode}/ghostPlayer")
    public void addGhostPlayer(@PathVariable String roomCode) {
        roomService.addGhostPlayer(roomCode);
    }

    @PostMapping("/{roomCode}/join")
    public PlayerInfo joinRoom(
        WebSession session,
        @PathVariable String roomCode,
        @RequestBody @Validated JoinRequest joinRequest) {

        final Object playerSessionObj =
            appProperties.disablePlayerSessions() ? null
                : session.getAttributes().get(ATTR_PLAYER);

        final PlayerInfo playerInfo;
        if (playerSessionObj instanceof PlayerSession playerSession &&
            playerSession.roomCode().equals(roomCode)
        ) {
            // Process requested player name to allow for rename
            playerInfo = roomService.rejoin(roomCode, joinRequest.playerName(), playerSession.playerId());
        }
        else {
            playerInfo = roomService.join(roomCode, joinRequest.playerName());
        }

        if (!appProperties.disablePlayerSessions()) {
            session.getAttributes().put(ATTR_PLAYER,
                PlayerSession.builder()
                    .playerId(playerInfo.playerId())
                    .roomCode(roomCode)
                    .build()
                );
        }

        return playerInfo;
    }

    @GetMapping("/{roomCode}/players")
    public ListResponse<PlayerInfo> getPlayers(@PathVariable String roomCode) {
        return ofList(roomService.players(roomCode));
    }

    @GetMapping("/{roomCode}/events/host")
    public ResponseEntity<Flux<GameEvent>> listenToHostEvents(@PathVariable String roomCode) {
        return ResponseEntity
            .ok()
            // See https://github.com/facebook/create-react-app/issues/1633
            .cacheControl(CacheControl.noCache().noTransform())
            .body(
                roomService.listenToHostEvents(roomCode)
            );
    }

    @GetMapping("/{roomCode}/events/player")
    public ResponseEntity<Flux<GameEvent>> listenToPlayerEvents(@PathVariable String roomCode) {
        return ResponseEntity
            .ok()
            // See https://github.com/facebook/create-react-app/issues/1633
            .cacheControl(CacheControl.noCache().noTransform())
            .body(roomService.listenToPlayerEvents(roomCode));
    }

    @PostMapping("/{roomCode}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startRound(@PathVariable String roomCode) {
        roomService.startRound(roomCode);
    }

    @GetMapping("/{roomCode}/round")
    public ValueResponse<Integer> getCurrentRoundNumber(@PathVariable String roomCode) {
        return ofValue(roomService.getCurrentRoundNumber(roomCode));
    }

    @GetMapping("/{roomCode}/blackCard")
    public BlackCard getBlackcard(@PathVariable String roomCode) {
        return roomService.getBlackcard(roomCode);
    }

    @PostMapping("/{roomCode}/whiteCards")
    public ListResponse<DealtCard> dealWhiteCards(@PathVariable String roomCode,
        @Validated @RequestBody DealHandRequest request
        ) {
        return ofList(roomService.dealWhiteCards(
            roomCode, request.playerId(), request.count()
        ));
    }

    @PostMapping("/{roomCode}/submittedCard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitCard(@PathVariable String roomCode,
        @RequestBody @Validated CardSubmission cardSubmission
    ) {
        roomService.submitCard(roomCode, cardSubmission.playerId(), cardSubmission.cardId());
    }

    @GetMapping("/{roomCode}/submittedCards")
    public ListResponse<DealtCard> getCandidateCards(@PathVariable String roomCode) {
        return ofList(
            roomService.listSubmittedCards(roomCode)
        );
    }

    @PostMapping("/{roomCode}/vote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vote(@PathVariable String roomCode,
        @RequestBody @Validated CardSubmission cardSubmission
    ) {
        roomService.vote(roomCode, cardSubmission.playerId(), cardSubmission.cardId());
    }

    @PostMapping("/{roomCode}/tally")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void tallyVotes(@PathVariable String roomCode) {
        roomService.tallyVotes(roomCode);
    }

    @GetMapping("/{roomCode}/winners")
    public ListResponse<WinningCard> getWinners(@PathVariable String roomCode) {
        return ofList(
            roomService.listWinners(roomCode)
        );
    }

    @GetMapping("/{roomCode}/scores")
    public ListResponse<PlayerScore> getScores(@PathVariable String roomCode) {
        return ofList(
            roomService.listPlayerScores(roomCode)
        );
    }
}
