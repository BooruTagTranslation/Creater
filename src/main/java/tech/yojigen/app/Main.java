package tech.yojigen.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String URL_TAG = "https://yande.re/tag.json?order=count&limit=0";
    private static final String[] URL_TRANSLATION = {
            "https://raw.githubusercontent.com/BooruTagTranslation/Database/master/artist.md",
            "https://raw.githubusercontent.com/BooruTagTranslation/Database/master/character.md",
            "https://raw.githubusercontent.com/BooruTagTranslation/Database/master/circle.md",
            "https://raw.githubusercontent.com/BooruTagTranslation/Database/master/copyright.md",
            "https://raw.githubusercontent.com/BooruTagTranslation/Database/master/faults.md",
            "https://raw.githubusercontent.com/BooruTagTranslation/Database/master/general.md",
    };

    private static Map<String, String> map_tag = new HashMap<>();
    private static Map<String, String> map_translation = new HashMap<>();


//    private static String json = "[{\"id\":136111,\"name\":\"girls_frontline\",\"count\":4882,\"type\":3,\"ambiguous\":false},{\"id\":38392,\"name\":\"disc_cover\",\"count\":4862,\"type\":0,\"ambiguous\":false},{\"id\":14794,\"name\":\"sketch\",\"count\":4841,\"type\":0,\"ambiguous\":false},{\"id\":750,\"name\":\"yukata\",\"count\":4772,\"type\":0,\"ambiguous\":false},{\"id\":1653,\"name\":\"mecha\",\"count\":4537,\"type\":0,\"ambiguous\":false},{\"id\":33,\"name\":\"mahou_shoujo_lyrical_nanoha\",\"count\":4412,\"type\":3,\"ambiguous\":false},{\"id\":2650,\"name\":\"witch\",\"count\":4349,\"type\":0,\"ambiguous\":false},{\"id\":2341,\"name\":\"landscape\",\"count\":4255,\"type\":0,\"ambiguous\":false},{\"id\":3734,\"name\":\"fixme\",\"count\":4234,\"type\":6,\"ambiguous\":false},{\"id\":28598,\"name\":\"bathing\",\"count\":4227,\"type\":0,\"ambiguous\":false},{\"id\":2844,\"name\":\"garter_belt\",\"count\":4191,\"type\":0,\"ambiguous\":false},{\"id\":374,\"name\":\"bunny_girl\",\"count\":4134,\"type\":0,\"ambiguous\":false},{\"id\":277,\"name\":\"eyepatch\",\"count\":4097,\"type\":0,\"ambiguous\":false},{\"id\":11334,\"name\":\"leotard\",\"count\":4061,\"type\":0,\"ambiguous\":false},{\"id\":64966,\"name\":\"love_live!\",\"count\":4052,\"type\":3,\"ambiguous\":false},{\"id\":1891,\"name\":\"gym_uniform\",\"count\":3860,\"type\":0,\"ambiguous\":false},{\"id\":300,\"name\":\"miko\",\"count\":3826,\"type\":0,\"ambiguous\":false},{\"id\":14068,\"name\":\"summer_dress\",\"count\":3825,\"type\":0,\"ambiguous\":false},{\"id\":120693,\"name\":\"umbrella\",\"count\":3655,\"type\":0,\"ambiguous\":false},{\"id\":3741,\"name\":\"fixed\",\"count\":3654,\"type\":0,\"ambiguous\":false},{\"id\":31033,\"name\":\"cream\",\"count\":3563,\"type\":0,\"ambiguous\":false},{\"id\":6213,\"name\":\"chinadress\",\"count\":3556,\"type\":0,\"ambiguous\":false}]";

    private static Gson gson;

    public static void main(String[] args) throws IOException {
        gson = new GsonBuilder().disableHtmlEscaping().create();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.MINUTES).readTimeout(10, TimeUnit.MINUTES).build();
        System.out.println("获取标签数据");
        String json_tags = client.newCall(new Request.Builder().url(URL_TAG).build()).execute().body().string();
//        String json_tags = json;

        System.out.println("解析标签数据");
        Type type = new TypeToken<List<TagBean>>() {
        }.getType();
        List<TagBean> tags = gson.fromJson(json_tags, type);

        System.out.println("获取翻译数据");
        for (String url : URL_TRANSLATION) {
            String translation = client.newCall(new Request.Builder().url(url).build()).execute().body().string();
            translation = translation.replaceAll("# .+\n.+\n.+\n", "");
            String[] array_translation = translation.split("\n");
            for (String string : array_translation) {
                String[] cutline = string.split("\\|");
                if (cutline[0] != null && cutline[1] != null) {
                    map_translation.put(cutline[0], cutline[1]);
                }
            }
        }
        System.out.println("转换标签数据");
        for (TagBean tag : tags) {
            if (map_translation.containsKey(tag.getName())) {
                map_tag.put(tag.getName(), map_translation.get(tag.getName()));
            } else {
                map_tag.put(tag.getName(), tag.getName());
            }
        }
        if (!Files.exists(Paths.get("./public"), LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectory(Paths.get("./public"));
        }
        System.out.println("写入标签数据");
        Pattern tagsPattern = Pattern.compile("^((?!\\|)[0-9a-zA-Z\\u0000-\\u00FF])+$");
        for (String key : map_tag.keySet()) {
            Matcher matcher = tagsPattern.matcher(key);
            if (matcher.find()) {
                Files.write(Paths.get("./public/" + Base64.getUrlEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8))), map_tag.get(key).getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    class TagBean {
        private String name;

        String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}