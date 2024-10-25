package koirdsuzu.koicmdlist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


public final class Koicmdlist extends JavaPlugin implements TabCompleter {

    private String version;

    private boolean flyEnabled = true; // サーバー全体の飛行許可状態を保持
    private boolean ignorePermission = false; // 権限を無視するかどうかを保持
    private String prefix = ""; // メッセージのプレフィックス

    // メッセージ用変数
    // flyコマンド関係
    private String flyNoPermission;
    private String flyDisabled;
    private String flyEnabledMessage;
    private String flyAlreadyDisabled;
    private String flyCommandUsage;
    private String flyServerEnabled;
    private String flyServerDisabled;
    private String flyUserCommandUsage;
    private String flyUserNoPlayer;
    private String flyUserFlyEnabledAdmin;
    private String flyUserFlyDisabledAdmin;
    private String flyUserFlyEnabledPlayer;
    private String flyUserFlyDisabledPlayer;
    private String playerflying;
    private String playernotflying;
    private String flyingplayerslist;
    private String noflyingplayers;

    // kclコマンド関係
    private String kclHelpUser;
    private String kclHelpAdmin;
    private String kclReloadSuccess;
    private String kclNoPermission;
    private String kclCommandUsage;

    // whitekick関係
    private boolean whitelistEnabled = false; // ホワイトリストの有効/無効状態を保持
    private String kickMessage; // キックメッセージ
    private String whitelistEnabledMessage; // ホワイトリスト有効化メッセージ

    // uuid関係
    private String noPermissionMessage;
    private String specifyPlayerMessage;
    private String validPlayerMessage;
    private String validEntityMessage;
    private String noValidFoundMessage;
    // 一般メッセージ関係
    private String playerOnly;
    private String currentArg;

    // Configから権限無視の設定を取得
    FileConfiguration config = getConfig();
    boolean ignoreKclPermission = config.getBoolean("permission.kcl", false);
    boolean ignoreFlyPermission = config.getBoolean("permission.fly", false);
    boolean ignoreUuidPermission = config.getBoolean("permission.uuid", false);
    boolean ignoreWhitekickPermission = config.getBoolean("permission.whitekick", false);


    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig(); // config.ymlが存在しない場合はデフォルトを作成する
        loadConfig(); // 設定の読み込み

        getCommand("fly").setExecutor(this);
        getCommand("fly").setTabCompleter(this); // Tab補完を追加
        getCommand("kcl").setExecutor(this);
        getCommand("kcl").setTabCompleter(this); // Tab補完を追加
        getCommand("koicmdlist").setExecutor(this); // /koicmdlist コマンドでも同じ処理を実行可能に
        getCommand("whitekick").setExecutor(this); // /whitekick コマンドを追加
        getCommand("whitekick").setTabCompleter(this);// Tab保管を追加
        getCommand("uuid").setExecutor(this); // /uuid コマンドを追加
        getCommand("uuid").setTabCompleter(this);// Tab保管を追加

        // config.ymlからサーバー起動メッセージを取得
        String serverStartMessage = getConfig().getString("message.server.start", "&aプラグインが正常に有効化されました。");
        serverStartMessage = ChatColor.translateAlternateColorCodes('&', prefix + serverStartMessage);

        // サーバー起動時にコンソールにメッセージを表示
        Bukkit.getLogger().info(ChatColor.stripColor(serverStartMessage)); // コンソールに出力
    }

    @Override
    public void onDisable() {
        //config.ymlからサーバーストップメッセージを取得
        String serverStopMessage = getConfig().getString("message.server.stop", "&cプラグインが正常に無効化されました。");
        serverStopMessage = ChatColor.translateAlternateColorCodes('&', prefix + serverStopMessage);
        // サーバーストップ時にコンソールにメッセージを表示
        Bukkit.getLogger().info(ChatColor.stripColor(serverStopMessage)); // コンソールに出力
    }

    // 設定をロードする
    private void loadConfig() {
        FileConfiguration config = getConfig();
        ignorePermission = config.getBoolean("ignore-fly-permission", false); // 設定から権限無視の値を取得
        prefix = config.getString("prefix", "&7[&aKoicmdlist&7]&r "); // prefixの設定を取得

        // flyメッセージの読み込み
        flyNoPermission = config.getString("message.fly.no-permission", "&c権限がありません。");
        flyDisabled = config.getString("message.fly.disabled", "&cサーバー全体で飛行が無効化されています。");
        flyEnabledMessage = config.getString("message.fly.enabled", "&a飛行が有効化されました。");
        flyAlreadyDisabled = config.getString("message.fly.already-disabled", "&c飛行が無効化されました。");
        flyCommandUsage = config.getString("message.fly.command-usage", "&cコマンドの使い方が間違っています。使い方: /fly [on|off]");
        flyServerEnabled = config.getString("message.fly.server-enabled", "&aサーバー全体の飛行が有効化されました。");
        flyServerDisabled = config.getString("message.fly.server-disabled", "&cサーバー全体の飛行が無効化されました。");
        flyUserCommandUsage = config.getString("message.fly.user-command-usage", "&cコマンドの使い方が間違っています。使い方: /fly user <mcid> <on|off>");
        flyUserNoPlayer = config.getString("message.fly.no-player", "&c指定されたプレイヤーが見つかりません。");
        flyUserFlyEnabledAdmin = config.getString("message.fly.user-fly-enabled-admin", "&a{player} の飛行状態を有効に設定しました。");
        flyUserFlyDisabledAdmin = config.getString("message.fly.user-fly-disabled-admin", "&c{player} の飛行状態を無効に設定しました。");
        flyUserFlyEnabledPlayer = config.getString("message.fly.user-fly-enabled-player", "&aあなたの飛行状態が有効に設定されました。");
        flyUserFlyDisabledPlayer = config.getString("message.fly.user-fly-disabled-player", "&cあなたの飛行状態が無効に設定されました。");
        playerflying = config.getString("message.fly.player-flying", "&a{player} は現在飛行状態です。");
        playernotflying = config.getString("message.fly.player-not-flying", "&c{player} は現在飛行していません。");
        flyingplayerslist = config.getString("message.fly.flying-players-list", "&a飛行状態のプレイヤー: {players}");
        noflyingplayers = config.getString("message.fly.no-flying-players", "&a現在、飛行状態のプレイヤーはいません。");

        // kclメッセージの読み込み
        kclHelpUser = config.getString("message.kcl.help.user", "&a--- KCL コマンドヘルプ (ユーザー) ---\n/help: ヘルプ表示\n/reload: コンフィグをリロード");
        kclHelpAdmin = config.getString("message.kcl.help.admin", "&a--- KCL コマンドヘルプ (管理者) ---\n/help: ヘルプ表示\n/reload: コンフィグをリロード\n/extra: 管理者専用コマンド");
        kclReloadSuccess = config.getString("message.kcl.reload-success", "&aConfigがリロードされました。");
        kclNoPermission = config.getString("message.kcl.no-permission", "&c権限がありません。");
        kclCommandUsage = config.getString("message.kcl.command-usage", "&cコマンドの使い方が間違っています。使い方: /kcl [help|reload]");

        // whitekickメッセージ読み込み
        kickMessage = config.getString("message.whitekick.kick-message", "&cホワイトリストに登録されていないため、キックされました。");
        whitelistEnabledMessage = config.getString("message.whitekick.enabled", "&aホワイトリストが有効化されました。");

        // uuidメッセージの読み込み
        noPermissionMessage = config.getString("message.uuid.no-permission", "&c権限がありません。");
        specifyPlayerMessage = config.getString("message.uuid.specify-player", "&cプレイヤーまたはエンティティを指定してください。");
        validPlayerMessage = config.getString("message.uuid.valid-player", "&aプレイヤー {player} のUUID: {uuid}");
        validEntityMessage = config.getString("message.uuid.valid-entity", "&aエンティティのUUID: {uuid}");
        noValidFoundMessage = config.getString("message.uuid.no-valid-found", "&c有効なプレイヤーまたはエンティティが見つかりません。");

        // 一般メッセージの読み込み
        playerOnly = config.getString("message.general.player-only", "&cこのコマンドはプレイヤーのみが使用できます。");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fly")) {
            return handleFlyCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("kcl") || command.getName().equalsIgnoreCase("koicmdlist")) {
            return handleKclCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("whitekick")) {
            return handleWhiteKickCommand(sender);
        } else if (command.getName().equalsIgnoreCase("uuid")) {
            return handleUuidCommand(sender, args);
        }
        return false;
    }

    //flyコマンドの処理
    private boolean handleFlyCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage(playerOnly));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            if (flyEnabled) {
                if (ignorePermission || player.hasPermission("koicmdlist.fly.use")) {
                    toggleFly(player);
                } else {
                    player.sendMessage(formatMessage(flyNoPermission));
                }
            } else {
                player.sendMessage(formatMessage(flyDisabled));
            }
            return true;
            } else if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("on")) {
                    if (sender.hasPermission("koicmdlist.fly.on")) {
                        enableFly();
                        Bukkit.broadcastMessage(formatMessage(flyServerEnabled));
                    } else {
                        sender.sendMessage(formatMessage(flyNoPermission));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("off")) {
                    if (sender.hasPermission("koicmdlist.fly.off")) {
                        disableFly();
                        Bukkit.broadcastMessage(formatMessage(flyServerDisabled));
                    } else {
                        sender.sendMessage(formatMessage(flyNoPermission));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("user")) {
                    if (sender.hasPermission("koicmdlist.fly.user")) {
                        if (args.length < 3) {
                            sender.sendMessage(formatMessage(flyUserCommandUsage));
                            return true;
                        }
                    }

                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer == null) {
                        sender.sendMessage(formatMessage(flyUserNoPlayer));
                        return true;
                    }

                    if (args[2].equalsIgnoreCase("on")) {
                        if (!sender.hasPermission("koicmdlist.fly.user")) {
                            sender.sendMessage(formatMessage(flyNoPermission));
                            return true;
                        }
                        targetPlayer.setAllowFlight(true);
                        sender.sendMessage(formatMessage(flyUserFlyEnabledAdmin).replace("{player}", targetPlayer.getName()));
                        targetPlayer.sendMessage(formatMessage(flyUserFlyEnabledPlayer));
                    } else if (args[2].equalsIgnoreCase("off")) {
                        if (!sender.hasPermission("koicmdlist.fly.user")) {
                            sender.sendMessage(formatMessage(flyNoPermission));
                            return true;
                        }
                        targetPlayer.setAllowFlight(false);
                        sender.sendMessage(formatMessage(flyUserFlyDisabledAdmin).replace("{player}", targetPlayer.getName()));
                        targetPlayer.sendMessage(formatMessage(flyUserFlyDisabledPlayer));
                    } else {
                        sender.sendMessage(formatMessage(flyUserCommandUsage));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("get")) {
                    if (sender.hasPermission("koicmdlist.fly.get")) {
                        return handleFlyGetCommand(sender, args);
                    }
                }
            }
        sender.sendMessage(formatMessage(flyCommandUsage));
        return true;
    }

    private void enableFly() {
    }

    private void disableFly() {
    }

    // fly get関係
    private boolean handleFlyGetCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(formatMessage(flyCommandUsage));
            return true;
        }

        if (args[1].equalsIgnoreCase("list")) {
            List<String> flyingPlayers = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getAllowFlight()) {
                    flyingPlayers.add(player.getName());
                }
            }

            if (flyingPlayers.isEmpty()) {
                sender.sendMessage(formatMessage(noflyingplayers));
            } else {
                sender.sendMessage(formatMessage(flyingplayerslist).replace("{players}", String.join(", ", flyingPlayers)));
            }
            return true;
        } else {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(formatMessage(flyUserNoPlayer));
                return true;
            }

            if (targetPlayer.getAllowFlight()) {
                sender.sendMessage(formatMessage(playerflying).replace("{player}", targetPlayer.getName()));
            } else {
                sender.sendMessage(formatMessage(playernotflying).replace("{player}", targetPlayer.getName()));
            }
            return true;
        }
    }

    // 飛行状態を切り替える
    private void toggleFly(Player player) {
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.sendMessage(formatMessage(flyAlreadyDisabled)); // コンソールにも出力される
        } else {
            player.setAllowFlight(true);
            player.sendMessage(formatMessage(flyEnabledMessage)); // コンソールにも出力される
        }
    }

    // サーバー全体の飛行を有効化
    private void enableFly(CommandSender sender, String[] args) {
        flyEnabled = true;
        if (!ignoreFlyPermission && !sender.hasPermission("koicmdlist.fly.on")) {
        } else {
            sender.sendMessage(formatMessage(flyNoPermission));
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("koicmdlist.fly.on") || ignorePermission) {
                player.setAllowFlight(true);
            }
        }
    }

    // サーバー全体の飛行を無効化
    private void disableFly(CommandSender sender, String[] args) {
        flyEnabled = false;
        if (!ignoreFlyPermission && !sender.hasPermission("koicmdlist.fly.off")) {
        } else {
            sender.sendMessage(formatMessage(flyNoPermission));
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
        }
    }

    // kclコマンドの処理
    private boolean handleKclCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (sender.hasPermission("koicmdlist.kcl.*")) {
                sender.sendMessage(formatMessage(kclHelpAdmin));
            } else {
                sender.sendMessage(formatMessage(kclHelpUser));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("koicmdlist.reload")) {
                reloadConfig();
                loadConfig();
                sender.sendMessage(formatMessage(kclReloadSuccess));
            } else {
                sender.sendMessage(formatMessage(kclNoPermission));
            }
            return true;
        }
        sender.sendMessage(formatMessage(kclCommandUsage));
        return true;
    }

    // whitekickコマンドの処理
    private boolean handleWhiteKickCommand(CommandSender sender) {
        if (!ignoreWhitekickPermission && !sender.hasPermission("koicmdlist.whitekick")) {
            sender.sendMessage(formatMessage(kickMessage));
            return true;
        }
        if (sender.hasPermission("koicmdlist.whitekick.use")) {
            whitelistEnabled = true;

            // Bukkitのホワイトリスト機能を有効にする
            Bukkit.setWhitelist(true);

            // メッセージを全員に通知
            Bukkit.broadcastMessage(formatMessage(whitelistEnabledMessage));

            // ホワイトリストに登録されていないプレイヤーをキックする
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isWhitelisted()) {
                    player.kickPlayer(formatMessage(kickMessage));
                }
            }
            return true;
        } else {
            sender.sendMessage(formatMessage(flyNoPermission));
            return true;
        }
    }

    // uuid コマンドの処理

    private boolean handleUuidCommand(CommandSender sender, String[] args) {
        if (!ignoreUuidPermission && !sender.hasPermission("koicmdlist.uuid")) {
            sender.sendMessage(formatMessage(noPermissionMessage));
            return true;
        }
        if (!sender.hasPermission("koicmdlist.uuid.use")) {
            sender.sendMessage(formatMessage(noPermissionMessage));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(formatMessage(specifyPlayerMessage));
            return true;
        }

        String target = args[0];

        // プレイヤーを指定した場合
        Player player = Bukkit.getPlayer(target);
        if (player != null) {
            String uuid = player.getUniqueId().toString();
            sender.sendMessage(formatMessage(validPlayerMessage.replace("{player}", player.getName()).replace("{uuid}", uuid)));
            Bukkit.getConsoleSender().sendMessage("Player " + player.getName() + "'s UUID: " + uuid);
            return true;
        }

        // ターゲットセレクター (@p, @a, @s, @e) または @e[type=] などを指定した場合
        try {
            List<Entity> entities = Bukkit.selectEntities(sender, target);
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    String uuid = entity.getUniqueId().toString();
                    sender.sendMessage(formatMessage(validEntityMessage.replace("{uuid}", uuid)));
                    Bukkit.getConsoleSender().sendMessage("Entity's UUID: " + uuid);
                }
                return true;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(formatMessage(noValidFoundMessage));
            return true;
        }

        sender.sendMessage(formatMessage(noValidFoundMessage));
        return true;
    }


    // メッセージをフォーマットして実行
    private String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("fly")) {
            if (args.length == 1) {
                if (sender.hasPermission("koicmdlist.fly.on")) {
                    completions.add("on");
                }
                if (sender.hasPermission("koicmdlist.fly.off")) {
                    completions.add("off");
                }
                if (sender.hasPermission("koicmdlist.fly.user")) {
                    completions.add("user");
                }
                if (sender.hasPermission("koicmdlist.fly.get")) {
                    completions.add("get");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("user")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("user")) {
                completions.add("on");
                completions.add("off");
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("get")) {
                    completions.add("list");  // "list" を候補に追加
                    for (Player player : sender.getServer().getOnlinePlayers()) {
                        completions.add(player.getName());  // オンラインプレイヤー名を候補に追加
                    }
                    return completions;
                }
            }
    } else if (command.getName().equalsIgnoreCase("kcl") || command.getName().equalsIgnoreCase("koicmdlist")) {
            if (args.length == 1) {
                completions.add("help");
                if (sender.hasPermission("koicmdlist.kcl.reload")) {
                    completions.add("reload");
                }
            }
        } else if (command.getName().equalsIgnoreCase("uuid")) {
            if (args.length == 0) {
                return completions;
            }

            String currentArg = args[0];

            // currentArg が null の場合の保護
            if (currentArg == null) {
                return completions;
            }

            // プレイヤーまたはターゲットセレクターの補完
            if (args.length == 1) {

                // ターゲットセレクターの補完
                if (!currentArg.startsWith("@e[") && !currentArg.contains("]")) {
                    if (currentArg.isEmpty() || "@".startsWith(currentArg)) {
                        completions.add("@p");
                        completions.add("@a");
                        completions.add("@s");
                        completions.add("@r");
                        completions.add("@e");
                    }

                    // プレイヤー名を補完
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player != null && player.getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                            completions.add(player.getName());
                        }
                    }
                }

                // @e[で始まる場合はターゲットセレクターのコンポーネントを補完
                if (currentArg.startsWith("@e[") && !currentArg.contains("]")) {
                    String insideBrackets = currentArg.substring(3); // @e[の後ろを取得
                    String prefix = currentArg.substring(0, currentArg.lastIndexOf("[") + 1); // "@e["の部分を保持

                    // 入力された文字に応じたフィルタリング (部分一致をサポート)
                    if (insideBrackets.toLowerCase().startsWith("t")) {
                        completions.add(prefix + "type=");
                    }
                    if (insideBrackets.toLowerCase().startsWith("d")) {
                        completions.add(prefix + "distance=");
                    }
                    if (insideBrackets.toLowerCase().startsWith("n")) {
                        completions.add(prefix + "name=");
                    }
                    if (insideBrackets.toLowerCase().startsWith("l")) {
                        completions.add(prefix + "limit=");
                    }
                    if (insideBrackets.toLowerCase().startsWith("s")) {
                        completions.add(prefix + "sort=");
                    }
                    if (insideBrackets.toLowerCase().startsWith("x")) {
                        completions.add(prefix + "x=");
                    }
                    if (insideBrackets.toLowerCase().startsWith("y")) {
                        completions.add(prefix + "y=");
                    }
                    if (insideBrackets.toLowerCase().startsWith("z")) {
                        completions.add(prefix + "z=");
                    }

                    // `type=` の後のエンティティリストの補完
                    if (insideBrackets.startsWith("type=")) {
                        String entityPart = insideBrackets.substring(5); // "type="の後ろの文字列を取得
                        for (EntityType type : EntityType.values()) {
                            if (type.isAlive() && type.name().toLowerCase().startsWith(entityPart.toLowerCase())) {
                                completions.add(prefix + "type=" + type.name().toLowerCase());
                            }
                        }
                    }
                }
            }

            // 引数がプレイヤー名またはターゲットセレクターであり、さらにスペースが続く場合は補完を停止
            if (args.length > 1) {
                String previousArg = args[args.length - 2];

                // プレイヤー指定やセレクターが完全に閉じている場合は補完をしない
                if (Bukkit.getPlayer(previousArg) != null || previousArg.matches("@[par]") || previousArg.matches("@e\\[.*\\]")) {
                    return completions; // 空の補完リストを返す
                }
            }

            return completions;
        }
        return completions;
    }
}
