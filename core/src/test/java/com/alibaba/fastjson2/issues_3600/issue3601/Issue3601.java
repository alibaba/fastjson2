package com.alibaba.fastjson2.issues_3600.issue3601;

import com.alibaba.fastjson2.JSON;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3601 {
    @Test
    public void test() {
        FastJson2Reader.init();

        var game = new Game();
        game.players.addAll(Arrays.asList(new Player(1, 100), new Player(2, 200)));

        var gameJson = JSON.toJSONString(game);
        var deserializedGame1 = JSON.parseObject(gameJson, Game.class);
        assertTrue(deserializedGame1.players.get(0).getClass() == Player.class);
    }

    public static class Player {
        public int level;
        public int exp;
        public Player() {
        }
        public Player(int level, int exp) {
            this.level = level;
            this.exp = exp;
        }
    }

    public static class Game {
        private MyArrayList<Player> players = new MyArrayList<>();
        public MyArrayList<Player> getPlayers() {
            return players;
        }
        public void setPlayers(MyArrayList<Player> players) {
            this.players = players;
        }
    }
}
