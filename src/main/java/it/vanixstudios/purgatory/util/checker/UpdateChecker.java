package it.vanixstudios.purgatory.util.checker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.vanixstudios.purgatory.Purgatory;
import it.vanixstudios.purgatory.util.console.Logger;
import it.vanixstudios.purgatory.util.strings.C;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    private static final String UPDATE_URL = "https://raw.githubusercontent.com/LorenzoZ-DEV/UpdatePlugins/main/Update.json";
    private static String latestVersion = "unknown";
    private static final Gson gson = new Gson();

    public static void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(UPDATE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Purgatory-Plugin/" + Purgatory.getInstance().getDescription().getVersion());
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    Logger.warning("⚠ GitHub API returned an error: HTTP " + responseCode);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                JsonObject jsonResponse = gson.fromJson(reader, JsonObject.class);
                reader.close();

                if (jsonResponse.has("plugins")) {
                    JsonObject plugins = jsonResponse.getAsJsonObject("plugins");
                    if (plugins.has("Purgatory")) {
                        JsonObject nUtils = plugins.getAsJsonObject("Purgatory");
                        if (nUtils.has("latest_version")) {
                            latestVersion = nUtils.get("latest_version").getAsString();
                        }
                    }
                }

                String currentVersion = Purgatory.getInstance().getDescription().getVersion();


                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    String updateMessage = C.translate("&aPurgatory is outdated! &7(Current: &e" + currentVersion + "&7, Latest: &e" + latestVersion + "&7) ask Lorenz to update");

                    Logger.info(updateMessage);
                } else {
                    Logger.line();
                    Logger.info("Purgatory is up to date!");
                    Logger.line();
                }

            } catch (Exception e) {
                Logger.error ("Failed to check for updates on GitHub!");
                e.printStackTrace();
            }
        });
    }

    public static String getLatestVersion() {
        return latestVersion;
    }
}
