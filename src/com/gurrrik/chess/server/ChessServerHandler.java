package com.gurrrik.chess.server;

import com.gurrrik.chess.protos.Messages;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

@ChannelHandler.Sharable
public class ChessServerHandler extends SimpleChannelInboundHandler<Messages.MClientMessage> {
    private Map<Long, ChessGameRoom> games = new HashMap<>();
    private Map<SocketAddress, Long> playersInGame = new HashMap<>();

    protected synchronized void handleStartGameMessage(ChannelHandlerContext ctx,
                                          Messages.MClientMessage.MStartGame msg) throws Exception {
        SocketAddress playerAddress = ctx.channel().remoteAddress();
        if (playersInGame.containsKey(playerAddress)) {
            System.err.println("Player already in game: " + playerAddress.toString());
            return;
        }

        long gameId = msg.getGameId();
        if (games.containsKey(gameId)) {
            ChessGameRoom gameRoom = games.get(gameId);
            if (!gameRoom.hasRoom()) {
                System.err.println("The room is full: " + gameId);
                return;
            } else {
                gameRoom.setNewPlayer(playerAddress, ctx.channel());
                System.err.println("Added player " + playerAddress.toString() + " to game room " + gameId);
            }
        } else {
            ChessGameRoom gameRoom = new ChessGameRoom(gameId);
            games.put(gameId, gameRoom);
            System.err.println("Created new game room for " + gameId);
            gameRoom.setNewPlayer(playerAddress, ctx.channel());
            System.err.println("Added player " + playerAddress.toString() + " to game room " + gameId);
        }

        playersInGame.put(playerAddress, gameId);
    }

    protected synchronized void handleMoveMessage(ChannelHandlerContext ctx,
                                                  Messages.MClientMessage.MMove msg) throws Exception {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                Messages.MClientMessage msg) throws Exception {
        System.err.println(msg.getType().toString());
        switch (msg.getType()) {
            case START_GAME:
                if (!msg.hasStartGame()) {
                    System.err.println("Malformed message: " + msg.toString());
                    return;
                }
                handleStartGameMessage(ctx, msg.getStartGame());
                break;
            case MOVE:
                if (!msg.hasMove()) {
                    System.err.println("Malformed message: " + msg.toString());
                    return;
                }
                handleMoveMessage(ctx, msg.getMove());
                break;
        }
    }
}