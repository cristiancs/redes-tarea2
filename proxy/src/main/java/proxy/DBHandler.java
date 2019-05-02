package proxy;

import java.nio.file.Paths;
import java.nio.file.Files;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class DBHandler {

    public void getServers() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);

            JSONObject server_array = (JSONObject) data.get("servers");

            server_array.keySet().forEach(keyStr -> {
                Object keyvalue = server_array.get(keyStr);
                System.out.println("key: " + keyStr + "value: " + keyvalue);

            });

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String getServerData(String server) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);

            JSONObject server_array = (JSONObject) data.get("servers");

            return (String) server_array.get(server);

        } catch (Exception e) {
            System.out.println(e);
            return "";
        }
    }

    public HashMap<String, ArrayList> getFiles() {
        try {
            HashMap<String, ArrayList> respuesta = new HashMap<String, ArrayList>();

            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);

            JSONObject files_object = (JSONObject) data.get("files");

            files_object.keySet().forEach(keyStr -> {

                JSONArray partes_array = files_object.getJSONArray(keyStr);

                ArrayList<String> partes_salida = new <String>ArrayList();

                for (int i = 0; i < partes_array.length(); i++) {
                    String parte = (String) partes_array.get(i);
                    partes_salida.add(parte);
                }

                respuesta.put(keyStr, partes_salida);

            });
            return respuesta;

        } catch (Exception e) {
            System.out.println(e);
            HashMap<String, ArrayList> respuesta = new HashMap<String, ArrayList>();
            return respuesta;
        }
    }

    public ArrayList getChunks(String name) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);

            JSONObject files_object = (JSONObject) data.get("files");

            JSONArray partes_array = files_object.getJSONArray(name);

            ArrayList<String> partes_salida = new <String>ArrayList();

            for (int i = 0; i < partes_array.length(); i++) {
                String parte = (String) partes_array.get(i);
                partes_salida.add(parte);
            }

            return partes_salida;

        } catch (Exception e) {
            System.out.println(e);
            ArrayList respuesta = new ArrayList();
            return respuesta;
        }
    }

}