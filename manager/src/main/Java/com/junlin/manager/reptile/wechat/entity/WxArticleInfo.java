package com.junlin.manager.reptile.wechat.entity;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by junlinhui eight on 2017/3/3.
 * 微信文章
 */
public class WxArticleInfo extends Model<WxArticleInfo> {
    //utils
    public static final WxArticleInfo dao = new WxArticleInfo();

    //张恒
    private String author;
    //内容
    private String content;
    //文章地址
    private String content_url;
    //版权状态 100
    private String copyright_stat;
    //封面地址
    private String cover;
    //描述
    private String digest;
    //":507168874,
    private int fileid;
    //是否为多条文1,是否为头条
    private int is_multi;
    //阅读原文的链接
    private String source_url;
    //9
    private int subtype;
    //标题
    private String title;
    //发布时间
    private double datetime;//1488461266,
    //
    private String fakeid;//"3002156712",
    //
    private int id;//1000010229,
    //状态
    private int status;//2,
    //type=49代表是图文消息
    private int type;//49
    //唯一标示
    private String sn;

    public WxArticleInfo(String author, String content, String content_url, String copyright_stat, String cover, String digest, int fileid, int
            is_multi, String source_url, int subtype, String title, double datetime, String fakeid, int id, int status, int type, String sn) {
        this.author = author;
        this.content = content;
        this.content_url = content_url;
        this.copyright_stat = copyright_stat;
        this.cover = cover;
        this.digest = digest;
        this.fileid = fileid;
        this.is_multi = is_multi;
        this.source_url = source_url;
        this.subtype = subtype;
        this.title = title;
        this.datetime = datetime;
        this.fakeid = fakeid;
        this.id = id;
        this.status = status;
        this.type = type;
        this.sn = sn;
    }

    public WxArticleInfo() {
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContent_url(String content_url) {
        this.content_url = content_url;
    }

    public void setCopyright_stat(String copyright_stat) {
        this.copyright_stat = copyright_stat;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public void setFileid(int fileid) {
        this.fileid = fileid;
    }

    public void setIs_multi(int is_multi) {
        this.is_multi = is_multi;
    }

    public void setSource_url(String source_url) {
        this.source_url = source_url;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDatetime(double datetime) {
        this.datetime = datetime;
    }

    public void setFakeid(String fakeid) {
        this.fakeid = fakeid;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }


    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getContent_url() {
        return content_url;
    }

    public String getCopyright_stat() {
        return copyright_stat;
    }

    public String getCover() {
        return cover;
    }

    public String getDigest() {
        return digest;
    }

    public int getFileid() {
        return fileid;
    }

    public int getIs_multi() {
        return is_multi;
    }

    public String getSource_url() {
        return source_url;
    }

    public int getSubtype() {
        return subtype;
    }

    public String getTitle() {
        return title;
    }

    public double getDatetime() {
        return datetime;
    }

    public String getFakeid() {
        return fakeid;
    }

    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public int getType() {
        return type;
    }


    public String getSn() {
        return sn;
    }

    @Override
    public String toString() {
        return "WxArticleInfo{" +
                "author='" + author + '\'' +
                ", content='" + content + '\'' +
                ", content_url='" + content_url + '\'' +
                ", copyright_stat='" + copyright_stat + '\'' +
                ", cover='" + cover + '\'' +
                ", digest='" + digest + '\'' +
                ", fileid=" + fileid +
                ", is_multi=" + is_multi +
                ", source_url='" + source_url + '\'' +
                ", subtype=" + subtype +
                ", title='" + title + '\'' +
                ", datetime=" + datetime +
                ", fakeid='" + fakeid + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", type=" + type +
                ", sn='" + sn + '\'' +
                '}';
    }
}
