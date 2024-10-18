package koirdsuzu.koicmdlist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Koicmdlist extends JavaPlugin implements TabCompleter {

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
    // kclコマンド関係
    private String kclHelpUser;
    private String kclHelpAdmin;
    private String kclReloadSuccess;
    private String kclNoPermission;
    private String kclCommandUsage;

    //whitekick関係
    private boolean whitelistEnabled = false; // ホワイトリストの有効/無効状態を保持
    private String kickMessage; // キックメッセージ
    private String whitelistEnabledMessage; // ホワイトリスト有効化メッセージ
    private String playerOnly;


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
        flyUserFlyEnabledAdmin = config.getString("message.fly.user-fly-enabled-admin", "&a<MCID> の飛行状態を有効に設定しました。");
        flyUserFlyDisabledAdmin = config.getString("message.fly.user-fly-disabled-admin", "&c<MCID> の飛行状態を無効に設定しました。");
        flyUserFlyEnabledPlayer = config.getString("message.fly.user-fly-enabled-player", "&aあなたの飛行状態が有効に設定されました。");
        flyUserFlyDisabledPlayer = config.getString("message.fly.user-fly-disabled-player", "&cあなたの飛行状態が無効に設定されました。");

        // kclメッセージの読み込み
        kclHelpUser = config.getString("message.kcl.help.user", "&a--- KCL コマンドヘルプ (ユーザー) ---\n/help: ヘルプ表示\n/reload: コンフィグをリロード");
        kclHelpAdmin = config.getString("message.kcl.help.admin", "&a--- KCL コマンドヘルプ (管理者) ---\n/help: ヘルプ表示\n/reload: コンフィグをリロード\n/extra: 管理者専用コマンド");
        kclReloadSuccess = config.getString("message.kcl.reload-success", "&aConfigがリロードされました。");
        kclNoPermission = config.getString("message.kcl.no-permission", "&c権限がありません。");
        kclCommandUsage = config.getString("message.kcl.command-usage", "&cコマンドの使い方が間違っています。使い方: /kcl [help|reload]");

        // whitekickメッセージ読み込み
        kickMessage = config.getString("message.whitekick.kick-message", "&cホワイトリストに登録されていないため、キックされました。");
        whitelistEnabledMessage = config.getString("message.whitekick.enabled", "&aホワイトリストが有効化されました。");

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
                if (sender.hasPermission("koicmdlist.fly.admin")) {
                    enableFly();
                    Bukkit.broadcastMessage(formatMessage(flyServerEnabled));
                } else {
                    sender.sendMessage(formatMessage(flyNoPermission));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("off")) {
                if (sender.hasPermission("koicmdlist.fly.admin")) {
                    disableFly();
                    Bukkit.broadcastMessage(formatMessage(flyServerDisabled));
                } else {
                    sender.sendMessage(formatMessage(flyNoPermission));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("user")) {
                if (args.length < 3) {
                    sender.sendMessage(formatMessage(flyUserCommandUsage));
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage(formatMessage(flyUserNoPlayer));
                    return true;
                }

                if (args[2].equalsIgnoreCase("on")) {
                    targetPlayer.setAllowFlight(true);
                    sender.sendMessage(formatMessage(flyUserFlyEnabledAdmin.replace("<MCID>", targetPlayer.getName())));
                    targetPlayer.sendMessage(formatMessage(flyUserFlyEnabledPlayer));
                } else if (args[2].equalsIgnoreCase("off")) {
                    targetPlayer.setAllowFlight(false);
                    sender.sendMessage(formatMessage(flyUserFlyDisabledAdmin.replace("<MCID>", targetPlayer.getName())));
                    targetPlayer.sendMessage(formatMessage(flyUserFlyDisabledPlayer));
                } else {
                    sender.sendMessage(formatMessage(flyUserCommandUsage));
                }
                return true;
            }
        }
        sender.sendMessage(formatMessage(flyCommandUsage));
        return true;
    }

    // /kclコマンドの処理
    private boolean handleKclCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (sender.hasPermission("koicmdlist.admin")) {
                sender.sendMessage(formatMessage(kclHelpAdmin));
            } else {
                sender.sendMessage(formatMessage(kclHelpUser));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("koicmdlist.admin")) {
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

    // /whitekickコマンドの処理
    private boolean handleWhiteKickCommand(CommandSender sender) {
        if (sender.hasPermission("koicmdlist.whitekick")) {
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
    private void enableFly() {
        flyEnabled = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("koicmdlist.fly.use") || ignorePermission) {
                player.setAllowFlight(true);
            }
        }
    }

    // サーバー全体の飛行を無効化
    private void disableFly() {
        flyEnabled = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
        }
    }

    // メッセージをフォーマットして送信
    private String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("fly")) {
            if (args.length == 1) {
                if (sender.hasPermission("koicmdlist.fly.admin")) {
                    completions.add("on");
                    completions.add("off");
                    completions.add("user");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("user")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("user")) {
                completions.add("on");
                completions.add("off");
            }
        } else if (command.getName().equalsIgnoreCase("kcl") || command.getName().equalsIgnoreCase("koicmdlist")) {
            if (args.length == 1) {
                completions.add("help");
                if (sender.hasPermission("koicmdlist.admin")) {
                    completions.add("reload");
                }
            }
        }
        return completions;
    }
}
