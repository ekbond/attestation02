package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SaveLoad {
    private static final Path SAVE = Paths.get("save.txt");
    private static final Path SCORES = Paths.get("scores.csv");

    public static void save(GameState s) {
        try (BufferedWriter w = Files.newBufferedWriter(SAVE)) {
            Player p = s.getPlayer();
            w.write("player;" + p.getName() + ";" + p.getHp() + ";" + p.getAttack());
            w.newLine();
            String inv = p.getInventory().stream().map(i -> i.getClass().getSimpleName() + ":" + i.getName()).collect(Collectors.joining(","));
            w.write("inventory;" + inv);
            w.newLine();
            w.write("room;" + s.getCurrent().getName());
            w.newLine();
            System.out.println("Сохранено в " + SAVE.toAbsolutePath());
            writeScore(p.getName(), s.getScore());
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить игру", e);
        }
    }

    public static void load(GameState s) {
        if (!Files.exists(SAVE)) {
            System.out.println("Сохранение не найдено.");
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(SAVE)) {
            Map<String, String> map = new HashMap<>();

            String line;

            while ((line = r.readLine()) != null) {
                String[] parts = line.split(";", 2);
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim());
                }
            }

            Player p = s.getPlayer();

            // Загрузка инвентаря
            p.getInventory().clear();
            if (map.containsKey("inventory")) {
                String inventoryData = map.get("inventory");                
                if (!inventoryData.isEmpty()) {
                    String[] items = inventoryData.split(",");
                    for (String itemStr : items) {
                        String[] itemData = itemStr.split(":", 2);
                        if (itemData.length == 2) {
                            String itemType = itemData[0].trim();
                            String itemName = itemData[1].trim();
                                                        
                            switch (itemType) {
                                case "Potion" -> {
                                    p.getInventory().add(new Potion(itemName, 5));
                                }
                                case "Key" -> {
                                    p.getInventory().add(new Key(itemName));
                                }
                                case "Weapon" -> {
                                    p.getInventory().add(new Weapon(itemName, 3));
                                }
                                default -> System.out.println("Неизвестный тип предмета: " + itemType);
                            }
                        }
                    }
                }
            }
            
            System.out.println("Игра загружена (упрощённо).");        
            
        } catch (IOException e) {
            System.out.println("Не удалось загрузить игру: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Не удалось загрузить игру: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void printScores() {
        if (!Files.exists(SCORES)) {
            System.out.println("Пока нет результатов.");
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(SCORES)) {
            System.out.println("Таблица лидеров (топ-10):");
            r.lines().skip(1).map(l -> l.split(",")).map(a -> new Score(a[1], Integer.parseInt(a[2])))
                    .sorted(Comparator.comparingInt(Score::score).reversed()).limit(10)
                    .forEach(s -> System.out.println(s.player() + " — " + s.score()));
        } catch (IOException e) {
            System.err.println("Ошибка чтения результатов: " + e.getMessage());
        }
    }

    private static void writeScore(String player, int score) {
        try {
            boolean header = !Files.exists(SCORES);
            try (BufferedWriter w = Files.newBufferedWriter(SCORES, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (header) {
                    w.write("ts,player,score");
                    w.newLine();
                }
                w.write(LocalDateTime.now() + "," + player + "," + score);
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Не удалось записать очки: " + e.getMessage());
        }
    }

    private record Score(String player, int score) {
    }
}
