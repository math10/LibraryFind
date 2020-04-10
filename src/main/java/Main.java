package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {

    static private String api = "https://api.github.com/search/repositories?q=test%20language:java&sort=stars&order=desc&type=repository";
    static private String libraryGroupId = "";
    private static JsonObject executeGetRequest(String apiURI) throws IOException {
        System.out.println("Start fetching all the repo according to query...");
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
        System.out.println("Fetching end.");
        return jsonObject;
    }

    private static void parseGitRepo(JsonObject jsonObject) {
        try {
            System.out.println("Start cloning repo " + jsonObject.get("git_url").getAsString() + "...");
            Git.cloneRepository()
                    .setURI(jsonObject.get("git_url").getAsString())
                    .setDirectory(new File(jsonObject.get("name").getAsString()))
                    .call();
            System.out.println("End clone.");
            System.out.println("Checking repo is maven project or not...");
            File tmpDir = new File(jsonObject.get("name").getAsString() + File.pathSeparator + "pom.xml");
            if(!tmpDir.exists()) {
                System.out.println("Not maven project");
                return;
            }
            System.out.println("Maven project.");
            System.out.println("Start analyzing project dependency...");
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("cmd.exe",
                    "/c",
                    "cd " + jsonObject.get("name").getAsString(),
                    "mvn dependency:tree -Dincludes=" + libraryGroupId);
            Process process = processBuilder.start();
            String res = getStingFromInputStream(process.getInputStream());
            System.out.println("End analyzing.");
            if(res.contains(libraryGroupId)) {
                System.out.println(jsonObject.get("git_url").getAsString() + " has dependency on " + libraryGroupId);
            } else {
                System.out.println(jsonObject.get("git_url").getAsString() + " is not dependent on " + libraryGroupId);
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

    private static String getStingFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream));

        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static void parseDirectDependency(JsonObject jsonObject) {
        try {
            String repoLink = jsonObject.get("html_url").getAsString() + "/master/pom.xml";
            repoLink = repoLink.replace("github.com", "raw.githubusercontent.com");
            URL url = new URL(repoLink);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            String res = getStingFromInputStream(httpURLConnection.getInputStream());
            if(res.contains(libraryGroupId)) {
                System.out.println(jsonObject.get("html_url").getAsString() + " has dependency on " + libraryGroupId);
            } else {
                System.out.println(jsonObject.get("html_url").getAsString() + " is not dependent on " + libraryGroupId);
            }
        } catch(IOException e) {
            System.out.println(jsonObject.get("html_url").getAsString() + " is not dependent on " + libraryGroupId);
        }
    }


    public static void main(String args[]) {
        try {
            JsonObject jsonObject = executeGetRequest(api);
            JsonArray jsonArray = jsonObject.getAsJsonArray("items");
            for(JsonElement jsonElement : jsonArray) {
                JsonObject object = jsonElement.getAsJsonObject();
                // parseGitRepo(object);
                parseDirectDependency(object);
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
