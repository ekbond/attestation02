package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Game {
    
    private final GameState state = new GameState();
    private final Map<String, Command> commands = new LinkedHashMap<>();

    static {
        WorldInfo.touch("Game");
    }

    public Game() {
        registerCommands();
        bootstrapWorld();
    }

    private void registerCommands() {
        commands.put("help", (ctx, a) -> System.out.println("Команды: " + String.join(", ", commands.keySet())));

        commands.put("about", (ctx, a) -> {
            Player player = ctx.getPlayer();
            Room currentRoom = ctx.getCurrent();

            boolean saveExists = java.nio.file.Files.exists(java.nio.file.Paths.get("save.txt"));

            System.out.println("=== DungeonMini (Промежуточная аттестация)===");
            System.out.println("Версия: 1.1 - внесены изменения в команды");
            System.out.println("Разработчик: Бондарева Екатерина, группа 1, наставник Ольга");
            System.out.println("Цель 1: на практике применить изученные темы курса");
            System.out.println("Цель 2: исследовать подземелья, собирать предметы и побеждать монстров!");
            System.out.println("===================");
            System.out.println("║        Текущее состояние        ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║ • Игрок: " + player.getName());
            System.out.println("║ • Здоровье: " + player.getHp() + " HP");
            System.out.println("║ • Атака: " + player.getAttack());
            System.out.println("║ • Очки: " + ctx.getScore());
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║ • Комната: " + currentRoom.getName());
            System.out.println("║ • Предметы: " + currentRoom.getItems().size());
            System.out.println("║ • Монстр: " + (currentRoom.getMonster() != null ? "есть" : "нет"));
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║ • Инвентарь: " + player.getInventory().size() + " шт.");
            System.out.println("║ • Сохранение: " + (saveExists ? "есть" : "нет"));
            System.out.println("╚══════════════════════════════════╝");


            // Детали инвентаря
            if (!player.getInventory().isEmpty()) {
                System.out.println("Инвентарь содержит:");
                player.getInventory().forEach(item -> 
                    System.out.println("  - " + item.getName())
                );
            }
    
         });

        commands.put("gc-stats", (ctx, a) -> {
            Runtime rt = Runtime.getRuntime();
            long free = rt.freeMemory(), total = rt.totalMemory(), used = total - free;
            System.out.println("Память: used=" + used + " free=" + free + " total=" + total);
        });

        commands.put("look", (ctx, a) -> System.out.println(ctx.getCurrent().describe()));
        
        // РЕАЛИЗАЦИЯ КОМАНДЫ MOVE
        commands.put("move", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Ошибка: требуется указать направление (north, south, east, west)");
            }

            String direction = a.getFirst().toLowerCase();
            Room current_room = ctx.getCurrent();
            Room next_room = current_room.getNeighbors().get(direction);

             if (next_room == null) {
                throw new InvalidCommandException("Нет пути в стороне: " + direction);
            }

            ctx.setCurrent(next_room);

            System.out.println("Вы перешли в: " + next_room.getName());

            System.out.println(next_room.describe());

            
        });
        
        commands.put("take", (ctx, a) -> {
            if (a.isEmpty()) {
                throw new InvalidCommandException("Ошибка: требуется указать название волшебного предмета");
            }
            
            String itemName = String.join(" ", a);
            Room current_room = ctx.getCurrent();
            Player player = ctx.getPlayer();
    
            // Поиск волшебного предмета (фильтрация)
            Optional<Item> foundItem = current_room.getItems().stream()
                .filter(item -> item.getName().equalsIgnoreCase(itemName))
                .findFirst();
            
            // Обработка ошибок
            if (foundItem.isEmpty()) {
                throw new InvalidCommandException("Волшебного предмета нету: " + itemName);
            }
    
            Item item = foundItem.get();
            current_room.getItems().remove(item);
            player.getInventory().add(item);
            
            System.out.println("Взято: " + item.getName());
        });
            
        
        commands.put("inventory", (ctx, a) -> {
            //System.out.println("TODO-3: вывести инвентарь (Streams)");
            
            Player player = ctx.getPlayer();
    
            if (player.getInventory().isEmpty()) {
                System.out.println("Инвентарь отсутствует");
                return;
            }
            
            // Stream API для группировки и сортировки
            player.getInventory().stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        item -> item.getClass().getSimpleName(),

                        // Сортировка по типу предмета
                        java.util.TreeMap::new, 
                        java.util.stream.Collectors.mapping(Item::getName, java.util.stream.Collectors.toList())
                    ))
                    .forEach((type, items) -> {

                        // Сортировка по названию предмета
                        java.util.Collections.sort(items); 
                        System.out.println("- " + type + " (" + items.size() + "): " + String.join(", ", items));
                    });

        });
        
        commands.put("use", (ctx, a) -> {
            // throw new InvalidCommandException("TODO-4: реализуйте использование предмета");

            if (a.isEmpty()) {
                throw new InvalidCommandException("Ошибка: требуется указать название предмета");
            }
            
            String itemName = String.join(" ", a);
            Player player = ctx.getPlayer();
            
            // Поиск предмета в разделе инвенторя
            Optional<Item> foundItem = player.getInventory().stream()
                    .filter(item -> item.getName().equalsIgnoreCase(itemName))
                    .findFirst();
            
            if (foundItem.isEmpty()) {
                throw new InvalidCommandException("Предмет не найден в разделе инвентаря: " + itemName);
            }
            
            Item item = foundItem.get();

            item.apply(ctx); // Использование apply согласно условию


        });
        
        commands.put("fight", (ctx, a) -> {
            // throw new InvalidCommandException("TODO-5: реализуйте бой");

                Room current_room = ctx.getCurrent();
                Monster monster = current_room.getMonster();
                Player player = ctx.getPlayer();
                
                if (monster == null) {
                    throw new InvalidCommandException("Ошибка: в комнате нет монстра");
                }
                                
                // Пошаговый бой ((несколько раз можно бить мостра, пока он будет не побежден))
                while (player.getHp() > 0 && monster.getHp() > 0) {

                    // Ход игрока
                    int playerDamage = player.getAttack();
                    monster.setHp(monster.getHp() - playerDamage);
                    System.out.println("Вы бьёте " + monster.getName() + " на " + playerDamage + ". HP монстра: " + Math.max(0, monster.getHp()));
                    
                    if (monster.getHp() <= 0) {
                        System.out.println("Монстр побежден!");
                        current_room.setMonster(null);
                        // Выпадение лута
                        ctx.addScore(10); 
                        return;
                    }
                    
                    // Ход монстра
                    int monsterDamage = 1;
                    player.setHp(player.getHp() - monsterDamage);
                    System.out.println("Монстр отвечает на " + monsterDamage + ". Ваше HP: " + Math.max(0, player.getHp()));
                    
                    if (player.getHp() <= 0) {
                        System.out.println("Вы убиты! Game over.");
                        System.exit(0);
                    }
                }

        });
        
        
        commands.put("save", (ctx, a) -> SaveLoad.save(ctx));
        commands.put("load", (ctx, a) -> SaveLoad.load(ctx));
        commands.put("scores", (ctx, a) -> SaveLoad.printScores());
        commands.put("exit", (ctx, a) -> {
            System.out.println("Пока!");
            System.exit(0);
        });
    }

    private void bootstrapWorld() {
        Player hero = new Player("Герой", 20, 5);
        state.setPlayer(hero);

        Room square = new Room("Площадь", "Каменная площадь с фонтаном.");
        Room forest = new Room("Лес", "Шелест листвы и птичий щебет.");
        Room cave = new Room("Пещера", "Темно и сыро.");
        square.getNeighbors().put("north", forest);
        forest.getNeighbors().put("south", square);
        forest.getNeighbors().put("east", cave);
        cave.getNeighbors().put("west", forest);

        forest.getItems().add(new Potion("Малое зелье", 5));
        forest.setMonster(new Monster("Волк", 1, 8));

        state.setCurrent(square);
    }

    public void run() {
        System.out.println("DungeonMini (TEMPLATE). 'help' — команды.");
        // try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
        // ЕСТЬ ПРОБЛЕМЫ С КОДИРОВКОЙ, БЫЛО ИСПРАВЛЕНО НА ДРУГУЮ ФУНКЦИЮ ЧТЕНИЯ
        
        // НЕ используем try-with-resources для Scanner, так как он закроет System.in
        Scanner scanner = new Scanner(System.in, "cp866");
        try {
            while (true) {
                System.out.print("> ");

                //String line = in.readLine();

                 String line = scanner.nextLine();

                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> parts = Arrays.asList(line.split("\s+"));
                String cmd = parts.getFirst().toLowerCase(Locale.ROOT);
                List<String> args = parts.subList(1, parts.size());
                Command c = commands.get(cmd);
                try {
                    if (c == null) throw new InvalidCommandException("Неизвестная команда: " + cmd);
                    c.execute(state, args);
                    state.addScore(1);
                } catch (InvalidCommandException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Непредвиденная ошибка: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка ввода/вывода: " + e.getMessage());        
        } finally {
            // Закрываем Scanner вручную в finally блоке
            scanner.close();
        }

    }
}
