package com.yealove.common;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置加载
 * Created by Yealove on 2016-09-25.
 */
public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private static final String SPLIT1 = "\\^";

    private static final String SPLIT2 = ",";

    private static final String SPLIT3 = "=";

    private static final String COMMENT = "#";

    private static final String DEFAULT = "config.txt";

    /**
     * url配置集合
     */
    private static Map<String, UrlConfig> urlConfigMap = new HashMap<String, UrlConfig>();

    /**
     * 初始化，读取配置文件
     */
    public static void init() {
        String configFileName = System.getProperty("config", DEFAULT);
        File configFile = new File(configFileName);
        if (!configFile.isFile()) {
            throw new ConfigCheckedException("配置文件[" + configFile.getAbsolutePath() + "]不存在！");
        }

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                //跳过注释和空行
                if (line.startsWith(COMMENT) || "".equals(line.trim())) {
                    continue;
                }

                //匹配URL
                if (line.startsWith("/")) {
                    String url = line;
                    UrlConfig urlConfig = new UrlConfig();

                    //读取URL对应的配置
                    while ((line = br.readLine()) != null) {
                        line = line.trim();

                        //跳过注释和空行
                        if (line.startsWith(COMMENT) || "".equals(line.trim())) {
                            continue;
                        }

                        //当前URL配置结束
                        if ("_end".equals(line)) {
                            break;
                        }

                        //URL配置解析
                        String[] lineSplit = line.split(SPLIT1);
                        if (lineSplit.length != 2) {
                            throw new ConfigCheckedException("[" + url + "]的配置不正确，请检查配置是否是: 规则^返回文件名");
                        }

                        String rulesStr = lineSplit[0].trim();
                        String fileName = lineSplit[1].trim();

                        if ("_default".equals(rulesStr)) {
                            urlConfig.defaultFileName = fileName;
                            continue;
                        }

                        if ("_match".equals(rulesStr)) {
                            Match match = Match.WORD;

                            if("contain".equals(fileName)) {
                                match = Match.CONTAIN;
                            }

                            if ("regex".equals(fileName)) {
                                match = Match.REGEX;
                            }
                            urlConfig.match = match;
                            continue;
                        }

                        Rule rule = new Rule();
                        rule.fileName = fileName;

                        for (String s : rulesStr.split(SPLIT2)) {
                            try {
                                rule.relations.add(new Relation(s.trim()));
                            } catch (IndexOutOfBoundsException e) {
                                throw new ConfigCheckedException("[" + url + "]的配置不正确，请检查规则是否是：节点=值");
                            }
                        }

                        urlConfig.rules.add(rule);
                    }

                    urlConfigMap.put(url, urlConfig);
                }

            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("---配置加载完成---");
            }
        } catch (FileNotFoundException e) {
            LOG.debug(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            IOUtils.closeQuietly(br);
        }
    }

    /**
     * 根据url和请求内容获取配置的文件名
     *
     * @param url
     * @param xml
     * @return
     */
    public static String getResultFileName(String url, String xml) {

        UrlConfig urlConfig = urlConfigMap.get(url);
        if (urlConfig == null) {
            throw new ConfigCheckedException("[" + url + "]未配置规则，请检查配置！");
        }

        List<Rule> rules = urlConfig.rules;


        for (Rule rule : rules) {
            boolean isThisRule = true;
            //未匹配规则个数
            int noMatchCount = 0;

            //规则校验
            for (Relation relation : rule.relations) {
                boolean isBreakFor = false;

                int startIndex = xml.indexOf("<" + relation.key + ">") + relation.key.length() + 2;
                int endIndex = xml.indexOf("</" + relation.key + ">");

                //有结点，且结束位置在开始位置之后，则有值
                if (startIndex > -1 && endIndex > -1 && endIndex > startIndex) {
                    String value = xml.substring(startIndex, endIndex);
                    //如果报文中的值与配置的值不匹配，则本条规则校验失败
                    switch (urlConfig.match) {
                        case CONTAIN: {
                            if (!relation.value.contains(value)) {
                                isThisRule = false;
                                isBreakFor = true;
                            }
                            break;
                        }

                        case REGEX: {
                            if (!value.matches(relation.value)) {
                                isThisRule = false;
                                isBreakFor = true;
                            }
                            break;
                        }

                        default: {
                            if (!value.equals(relation.value)) {
                                isThisRule = false;
                                isBreakFor = true;
                            }
                            break;
                        }
                    }

                    if(isBreakFor) {
                        break;
                    }
                } else {
                    noMatchCount++;
                }
            }

            //匹配到规则且不全为未匹配规则，则返回对应的文件名称
            if (isThisRule && noMatchCount < rule.relations.size()) {
                return rule.fileName;
            }

        }

        if (urlConfig.defaultFileName == null || "".equals(urlConfig.defaultFileName.trim())) {
            throw new ConfigCheckedException("[" + url + "]未配置默认返回");
        }

        return urlConfig.defaultFileName;
    }


    /**
     * Url配置
     */
    private static class UrlConfig {
        List<Rule> rules = new ArrayList<Rule>();
        String defaultFileName;
        Match match = Match.WORD;
    }

    /**
     * 规则，对应配置关系的一行
     */
    private static class Rule {
        List<Relation> relations = new ArrayList<Relation>();
        String fileName;
    }

    /**
     * 对应关系，一条规则对应多个key-value关系
     */
    private static class Relation {
        String key;
        String value;

        public Relation(String str) throws IndexOutOfBoundsException {
            String[] split = str.split(SPLIT3);
            this.key = split[0];
            this.value = split[1];
        }
    }
}
