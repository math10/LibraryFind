import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {

    static private String api = "https://api.github.com/search/repositories?q=test%20language:java&sort=stars&order=desc&type=repository";
    static private String libraryGroupId = "";
    private static JsonObject executeGetRequest(String apiURI) throws IOException {
        URL url = new URL(apiURI);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((con.getInputStream())));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        JsonObject jsonObject = new JsonParser().parse(sb.toString()).getAsJsonObject();
        return jsonObject;
    }

    private static void parseGitRepo(JsonObject jsonObject) {
        try {
            Git.cloneRepository()
                    .setURI(jsonObject.get("git_url").getAsString())
                    .setDirectory(new File(jsonObject.get("name").getAsString()))
                    .call();
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("cmd.exe",
                    "/c",
                    "cd " + jsonObject.get("name").getAsString(),
                    "mvn dependency:tree -Dincludes=" + libraryGroupId);
            Process process = processBuilder.start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String res = sb.toString();
            if(res.contains(libraryGroupId)) {
                System.out.println(jsonObject.get("git_url").getAsString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidRemoteException e) {
            e.printStackTrace();
        } catch (TransportException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        try {
            JsonObject jsonObject = executeGetRequest(api);
            JsonArray jsonArray = jsonObject.getAsJsonArray("items");
            for(JsonElement jsonElement : jsonArray) {
                JsonObject object = jsonElement.getAsJsonObject();
                parseGitRepo(object);
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
