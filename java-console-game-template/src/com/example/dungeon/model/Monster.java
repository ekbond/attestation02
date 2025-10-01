package com.example.dungeon.model;

public class Monster extends Entity {
    private int level;
    private int attack;

    public Monster(String name, int level, int hp) {
        super(name, hp);
        this.level = level;
        this.attack = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }
}
