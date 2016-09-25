package com.yealove.common;

import java.util.List;
import java.util.Map;

/**
 * 配置加载
 * Created by Yealove on 2016-09-25.
 */
public class Config {

    /**
     * 一个url对应多个规则
     */
    private Map<String, UrlConfig> urlConfigMap;

    public static void init() {

    }

    public String getResultFileName(String url, String xml) {

        UrlConfig urlConfig = urlConfigMap.get(url);
        if(urlConfig == null) {
            throw new ConfigCheckedException("Url未配置，请检查配置！");
        }

        List<Rule> rules = urlConfig.rules;


        for (Rule rule : rules) {
            boolean isThisRule = true;

            //规则校验
            for (Relation relation : rule.relations) {
                int startIndex = xml.indexOf("<" + relation.key + ">") + relation.key.length() + 2;
                int endIndex = xml.indexOf("</" + relation.key + ">");

                //有结点，且结束位置在开始位置之后，则有值
                if(startIndex > -1 && endIndex > -1 && endIndex > startIndex) {
                    String value = xml.substring(startIndex, endIndex);
                    //如果报文中的值与配置的值不匹配，则本条规则校验失败
                    if(!value.equals(relation.value)) {
                        isThisRule = false;
                        break;
                    }
                }
            }

            //匹配到规则则返回对应的文件名称
            if (isThisRule) {
                return rule.fileName;
            }

        }

        if(urlConfig.defaultFileName == null || "".equals(urlConfig.defaultFileName.trim())) {
            throw new ConfigCheckedException("[" + url + "]未配置默认返回");
        }

        return urlConfig.defaultFileName;
    }


    class UrlConfig {
        List<Rule> rules;
        String defaultFileName;
    }

    /**
     * 规则，对应配置关系的一行
     */
    class Rule {

        /**
         * 一条规则对应多个key-value关系
         */
        List<Relation> relations;

        String fileName;
    }

    /**
     * 对应关系
     */
    class Relation {
        String key;
        String value;
    }


//    public static void main(String[] args) {
//        String xml = "<html><td>123</td></html>";
//        int startIndex = xml.indexOf("<td>");
//        int endIndex = xml.indexOf("</td>");
//
//
//        System.out.println(startIndex + "-------" + endIndex);
//    }
}
