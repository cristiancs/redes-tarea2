package proxy;

import java.nio.file.Paths;
import java.io.FileWriter;
import java.nio.file.Files;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHandler {

    public ArrayList<String> getServers() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);

            JSONObject server_array = (JSONObject) data.get("servers");

            ArrayList<String> salida = new ArrayList<String>();
            server_array.keySet().forEach(keyStr -> {
                salida.add(keyStr);

            });
            return salida;

        } catch (Exception e) {

            System.out.println(e);
            return new ArrayList<String>();
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

    public HashMap<String, ArrayList<String>> getFiles() {
        try {
            HashMap<String, ArrayList<String>> respuesta = new HashMap<String, ArrayList<String>>();

            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);

            JSONObject files_object = (JSONObject) data.get("files");

            files_object.keySet().forEach(keyStr -> {

                JSONArray partes_array = files_object.getJSONArray(keyStr);

                ArrayList<String> partes_salida = new ArrayList<String>();

                for (int i = 0; i < partes_array.length(); i++) {
                    String parte = (String) partes_array.get(i);
                    partes_salida.add(parte);
                }

                respuesta.put(keyStr, partes_salida);

            });
            return respuesta;

        } catch (Exception e) {
            System.out.println(e);
            HashMap<String, ArrayList<String>> respuesta = new HashMap<String, ArrayList<String>>();
            return respuesta;
        }
    }

    public ArrayList<String> getChunks(String name) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);

            JSONObject files_object = (JSONObject) data.get("files");

            JSONArray partes_array = files_object.getJSONArray(name);

            ArrayList<String> partes_salida = new ArrayList<String>();

            for (int i = 0; i < partes_array.length(); i++) {
                String parte = (String) partes_array.get(i);
                partes_salida.add(parte);
            }

            return partes_salida;

        } catch (Exception e) {
            ArrayList<String> respuesta = new ArrayList<String>();
            return respuesta;
        }
    }

    public Boolean fileExist(String name) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);
            JSONObject files_object = (JSONObject) data.get("files");
            try {
                files_object.get(name);
                return true;
            } catch (Exception e) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    public Boolean deleteFile(String name) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject

            JSONObject data = new JSONObject(content);

            JSONObject files_object = (JSONObject) data.get("files");
            JSONObject fileSizes_object = (JSONObject) data.get("fileSizes");
            files_object.remove(name);
            fileSizes_object.remove(name);

            FileWriter file = new FileWriter("db.json");
            file.write(data.toString());
            file.close();
            return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public Boolean saveFile(String fileName, Integer largo) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject

            JSONObject data = new JSONObject(content);

            JSONObject files_object = (JSONObject) data.get("files");
            JSONObject fileSizes_object = (JSONObject) data.get("fileSizes");

            files_object.put(fileName, new JSONArray());
            fileSizes_object.put(fileName, largo);
            FileWriter file = new FileWriter("db.json");
            file.write(data.toString());
            file.close();
            return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public Integer getfileSize(String name) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject
            JSONObject data = new JSONObject(content);

            JSONObject fileSizes_object = (JSONObject) data.get("fileSizes");

            return (Integer) fileSizes_object.get(name);
        } catch (Exception e) {

            return 0;
        }
    }

    public Boolean SaveChunk(String fileName, String chunk, String server) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("db.json")));
            // Convert JSON string to JSONObject

            JSONObject data = new JSONObject(content);

            JSONObject files_object = (JSONObject) data.get("files");

            JSONArray partes_array = files_object.getJSONArray(fileName);
            partes_array.put(chunk + "|" + server);

            FileWriter file = new FileWriter("db.json");
            file.write(data.toString());
            file.close();
            return true;

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }
}