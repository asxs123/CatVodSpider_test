package com.github.catvod.spider;


import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Dadag extends Spider {


    private static final String siteUrl = "https://www.dadagui.tv";


    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }


    public String homeContent(boolean filter) {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(siteUrl, getHeaders()));
        for (Element element : doc.select("div.stui-vodlist__head > h3 > a")) {
            if (element.attr("href").startsWith("/vodshow")) {
                String id = element.attr("href").replaceAll("\\D+", "");
                String name = element.text();
                classes.add(new Class(id, name));
            }
        }
        for (Element element : doc.select("ul.stui-vodlist > li > div > a")) {
            String img = element.attr("data-original");
            String name = element.attr("title");
            String remark = element.select("span").text();
            String id = element.attr("href").replaceAll("\\D+", "");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(classes, list);

    }


    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl + String.format("/vodtype/%s-%s.html", tid, pg);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select(".stui-vodlist > li > div > a")) {
            String img = element.attr("data-original");
            String name = element.attr("title");
            String remark = element.select("span.pic-text.text-right").text();
            String id = element.attr("href").replaceAll("\\D+", "");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }


    public String detailContent(List<String> ids) {
        Document doc = Jsoup.parse(OkHttp.string(siteUrl.concat("/voddetail/").concat(ids.get(0)), getHeaders()));
        String name = doc.select("h1.title").text();
        String remarks = doc.select("p.data:nth-child(8)").text();
        String img = doc.select("img.lazyload").attr("data-original");
        String type = doc.select("p.data:nth-child(5)").text();
        String actor = doc.select("p.data:nth-child(6)").text();
        String content = doc.select(".detail-content").text();
        String director = doc.select("p.data:nth-child(7)").text();

        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(img);
        vod.setVodName(name);
        vod.setVodActor(actor);
        vod.setVodRemarks(remarks);
        vod.setVodContent(content);
        vod.setVodDirector(director);
        vod.setTypeName(type);


        Map<String, String> sites = new LinkedHashMap<>();
        Elements sources = doc.select("div.stui-vodlist__head > h3");
        Elements sourceList = doc.select("ul.stui-content__playlist");
        for (int i = 0; i < sources.size() - 1; i++) {
            Element source = sources.get(i);
            String sourceName = source.text();
            Elements playList = sourceList.get(i).select("a");
            List<String> vodItems = new ArrayList<>();
            for (int j = 0; j < playList.size(); j++) {
                Element e = playList.get(j);
                vodItems.add(e.text() + "$" + e.attr("href"));
            }
            if (vodItems.size() > 0) {
                sites.put(sourceName, TextUtils.join("#", vodItems));
            }
        }
        if (sites.size() > 0) {
            vod.setVodPlayFrom(TextUtils.join("$$$", sites.keySet()));
            vod.setVodPlayUrl(TextUtils.join("$$$", sites.values()));
        }


        return Result.string(vod);
    }


    public String searchContent(String key, boolean quick) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl.concat("/vodsearch/").concat(key).concat("-------------.html");
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("li.col-md-6 > div > a")) {
            String img = element.attr("data-original");
            String name = element.attr("title");
            String remark = element.select("span.pic-text.text-right").text();
            String id = element.attr("href").replaceAll("\\D+", "");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }

    public String playerContent(String flag, String id, List<String> vipFlags) {
        return Result.get().url(siteUrl + id).parse().header(getHeaders()).string();
    }
}

