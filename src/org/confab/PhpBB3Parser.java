/*
 * Copyright (C) 2011 Paul Bourke <pauldbourke@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.confab;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.cookie.Cookie;

public class PhpBB3Parser {
    private AndroidHttpClient httpclient = AndroidHttpClient.newInstance("Mozilla");

    public List<Forum> parseForums(Document root, BulletinBoard parent) {
        Utilities.debug("parseForums");

        List<Forum> ret = new ArrayList<Forum>();

        // get table
        Elements forum_tables = root.select("ul[class=topiclist forums]");
        assert !forum_tables.isEmpty() : root.html();

        for (Element forum_table : forum_tables) {
            Elements els_li = forum_table.select("li.row");
            assert !els_li.isEmpty();
            for (Element el_li : els_li) {
                Forum new_forum = new Forum(parent);

                // Get the forum url
                Elements els_a = el_li.select("a.forumtitle");
                Element el_a = els_a.first();
                assert el_a != null;
                new_forum.url = el_a.attr("href");
                assert new_forum.url != null;
                Utilities.debug("new_forum.url : " + new_forum.url);

                // Get the title text
                new_forum.title = el_a.text();
                assert new_forum.title != null;
                Utilities.debug("new_forum.title : " + new_forum.title);

                // Check for any subforums in remaining a elements
                els_a.remove(els_a.first()); 
                for (Element _el_a : els_a) {
                    Forum sub_forum = new Forum(parent);
                    sub_forum.url = el_a.attr("href");
                    assert sub_forum.url != null;
                    sub_forum.title = el_a.text();
                    assert sub_forum.title != null;
                    new_forum.subForums.add(sub_forum);
                    Utilities.debug("added subForum: " + sub_forum.title);
                }

                // Get the description/message of this topic
                String el_description = el_a.parent().text();
                if (el_description != null) {
                    new_forum.description = el_description;
                } else {
                    new_forum.description = "";
                }
                Utilities.debug("new_forum.description : " + new_forum.description);

                Utilities.debug("new_forum.parent.url : " + new_forum.parent.url);

                ret.add(new_forum);
                Utilities.debug("-----");
            }
        }
        Utilities.debug("end parseForums");
        return ret;
    }

     /**
     * Constructs and submits a POST with the appropriate parameters to login to a vbulletin.
     * @param  rootURL     Base or root URL for the site to log into 
     * @param  username    User's login name
     * @param  password    User's password
     * @return             User object initialised with a HttpContext
     */
    public User login(String rootURL, String username, String password) {
        Utilities.debug("login");

        User ret = new User(username, password);

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        try {
            // set up the POST
            HttpPost httppost = new HttpPost(rootURL+"login.php");
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("do", "login"));
            nvps.add(new BasicNameValuePair("vb_login_username", username));
            nvps.add(new BasicNameValuePair("vb_login_password", ""));
            nvps.add(new BasicNameValuePair("s", ""));
            nvps.add(new BasicNameValuePair("securitytoken", "guest"));
            nvps.add(new BasicNameValuePair("do", "login"));
            nvps.add(new BasicNameValuePair("vb_login_md5password", Utilities.md5(password)));
            nvps.add(new BasicNameValuePair("vb_login_md5password_utf", Utilities.md5(password)));
            httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            // execute the POST 
            Utilities.debug("Executing POST");
            HttpResponse response = httpclient.execute(httppost, localContext);
            Utilities.debug("POST response: " + response.getStatusLine());
            assert response.getStatusLine().getStatusCode() == 200;

            //TODO: store the cookies
            //http://bit.ly/e7yY5i (CookieStore javadoc)

            Utilities.printCookieStore(cookieStore);

            // confirm we are logged in 
            HttpGet httpget = new HttpGet(rootURL);
            response = httpclient.execute(httpget, localContext);
            HttpEntity entity = response.getEntity();
            Document page = Jsoup.parse(EntityUtils.toString(entity));
            EntityUtils.consume(entity);
            assert page != null;

            Utilities.debug("Checking that we are logged in..");
            Element username_box = page.select("input[name=vb_login_username]").first();
            assert username_box == null;
            Element password_box = page.select("input[name=vb_login_password]").first();
            assert password_box == null;

            // parse the user's new securitytoken
            Element el_security_token = page.select("input[name=securitytoken]").first();
            assert el_security_token != null;             
            String security_token = el_security_token.attr("value");
            assert security_token != null;
            String[] token_array = security_token.split("-");
            assert token_array.length == 2;
            ret.vb_security_token = token_array[1];
            assert ret.vb_security_token.length() == 40;
            Utilities.debug("securitytoken: " + ret.vb_security_token);

            Utilities.debug("Login seems ok");
            ret.httpContext = localContext;
        } catch (IOException e) {
            System.out.println(e);
        } 

        Utilities.debug("end login");
        return ret;
    }   

    /**
     * Parses each topic for a particular forum.
     * @param  forum        Document of html containing topics
     * @param  parent       Forum the threads belong to
     * @return              List of ForumThread objects 
     */
    public List<ForumThread> parseForumThreads(Document forum, Forum parent) {
        Utilities.debug("parseForumThreads");

        List<ForumThread> ret = new ArrayList<ForumThread>();

        // Get topic table
        Elements thread_table_tds = forum.select("tbody[id*=threadbits_forum_] td");
        if (thread_table_tds.isEmpty()) {
            Utilities.debug("It seems " + parent.url + " has no topics.");
            return ret;
        }

        // Get any stickies
        Elements stickies = thread_table_tds.select(
            "td:contains(Sticky:)  a[id*=thread_title_]");

        // Get all topics
        Elements els_a = thread_table_tds.select("a[id*=thread_title_]");
        assert !els_a.isEmpty();

        // Loop topics and grab info about each
        for (Element el_a : els_a) {
            ForumThread new_topic = new ForumThread(parent);

            // Get topic 
            new_topic.title = el_a.text();
            assert new_topic.title != null;
            Utilities.debug("new_topic.title: " + new_topic.title);

            // Check if sticky
            if (stickies.html().contains(new_topic.title)) {
                new_topic.isSticky = true; 
                Utilities.debug("new_topic.isSticky: " + new_topic.isSticky);
            } 
            
            // Get URL
            new_topic.url = el_a.attr("href");
            assert new_topic.url != null;
            Utilities.debug("new_topic.url:" + new_topic.url);

            ret.add(new_topic);        
        }

        Utilities.debug("end printForumThreads");
        return ret;
    }

    /**
     * Parses each post for a particular topic.
     * @param  html         Html containing the posts to be parsed 
     * @return              List of Post objects 
     */
    public List<Post> parsePosts(Document html, ForumThread parent) {
        Utilities.debug("Starting parsePosts");
        List<Post> ret = new ArrayList<Post>();

        // Each post should have it's own table
        Elements div_posts = html.select("div#posts");
        assert !div_posts.isEmpty();
        Elements posts_table = div_posts.select("table[id~=(post\\d+)]");
        assert !posts_table.isEmpty();

        for (Element el_post : posts_table) {
            Post new_post = new Post(parent);

            // Get post id (id=post\d+)
            new_post.id = el_post.attr("id").replace("post", "").trim();
            assert new_post.id != null;

            // Get post message 
            Elements el_message = el_post.select("div[id~=(post_message_\\d+)]");
            assert !el_message.isEmpty();
            new_post.message = el_message.first().text();
            assert new_post.message != null;
            Utilities.debug("new_post.message: " + new_post.message);

            // Get post author
            Elements el_author = el_post.select(".bigusername");
            assert !el_author.isEmpty();
            new_post.author.username = el_author.first().text();
            assert new_post.author != null;
            Utilities.debug("new_post.author: " + new_post.author);

            ret.add(new_post);
        }

        Utilities.debug("Finished parsePosts");
        return ret;
    }

    public void postForumThread(Forum targetForum, Post newPost, User user) {
        Utilities.debug("postForumThread");

        try {
            String reply_page = targetForum.rootURL() + 
                "newthread.php?do=newthread&f=" + targetForum.id;
            Utilities.debug("GET: " + reply_page);
            HttpGet httpget = new HttpGet(reply_page);
            HttpResponse response = httpclient.execute(httpget, user.httpContext);
            HttpEntity entity = response.getEntity();
            Document page = Jsoup.parse(EntityUtils.toString(entity));
            EntityUtils.consume(entity);
            assert page != null;

            // TODO: need check to make sure we're on the right page.  HttpEntity's
            // can just contain garbage and jsoup will still consume it

            // Make sure we're logged in before going any further
            Element username_box = page.select("input[name=vb_login_username]").first();
            assert username_box == null;
            Element password_box = page.select("input[name=vb_login_password]").first();
            assert password_box == null;

            // Construct POST 
            HttpPost httppost = new HttpPost(targetForum.rootURL()+"newthread.php");
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();

            // TODO: fix subject
            nvps.add(new BasicNameValuePair("subject", "hello world"));
            nvps.add(new BasicNameValuePair("message", newPost.message));

            // Find the form - we can parse the rest of the needed elements from it
            Element reply_form = page.select("form[action*=newthread.php?do=postthread&f=]")
                .first();
            assert reply_form != null;
            String[] vals_array = {"s", "securitytoken", "f", "do", 
                                   "posthash", "poststarttime", "loggedinuser"};
            List<String> vals = Arrays.asList(vals_array);
            for (String val : vals) {
                Element el = reply_form.select("input[name="+val+"]").first();        
                assert el != null : val;
                nvps.add(new BasicNameValuePair(val, el.attr("value")));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            // Execute the POST 
            Utilities.debug("Executing POST");
            response = httpclient.execute(httppost, user.httpContext);
            Utilities.debug("POST response: " + response.getStatusLine());
            assert response.getStatusLine().getStatusCode() == 302;
        } catch (IOException e) {
            System.out.println(e);
        }
        Utilities.debug("end postForumThread");
    }   

    public void createPost(Post replyTo, Post newPost, User user) {
        Utilities.debug("createPost");

        try {
            String reply_page = replyTo.rootURL() + "newreply.php?do=newreply&noquote=1&p=" + 
                    replyTo.id;
            HttpGet httpget = new HttpGet(reply_page);
            HttpResponse response = httpclient.execute(httpget, user.httpContext);
            HttpEntity entity = response.getEntity();
            Document page = Jsoup.parse(EntityUtils.toString(entity));
            EntityUtils.consume(entity);
            assert page != null;

            // TODO: need check to make sure we're on the right page.  HttpEntity's
            // can just contain garbage and jsoup will still consume it

            // Make sure we're logged in before going any further
            Element username_box = page.select("input[name=vb_login_username]").first();
            assert username_box == null;
            Element password_box = page.select("input[name=vb_login_password]").first();
            assert password_box == null;

            // Construct POST 
            HttpPost httppost = new HttpPost(replyTo.rootURL()+"newreply.php");
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();

            // There is a title param but think it's optional..
            //nvps.add(new BasicNameValuePair("title", "");

            nvps.add(new BasicNameValuePair("message", newPost.message));

            // Find the form - we can parse the rest of the needed elements from it
            Element reply_form = page.select("form[action*=newreply.php?do=postreply&t=]").first();
            assert reply_form != null;
            String[] vals_array = {"s", "securitytoken", "do", "t", "p", "specifiedpost",
                                   "posthash", "poststarttime", "loggedinuser", "multiquoteempty"};
            List<String> vals = Arrays.asList(vals_array);
            for (String val : vals) {
                Element el = reply_form.select("input[name="+val+"]").first();        
                assert el != null : val;
                nvps.add(new BasicNameValuePair(val, el.attr("value")));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            // Execute the POST 
            Utilities.debug("Executing POST");
            response = httpclient.execute(httppost, user.httpContext);
            Utilities.debug("POST response: " + response.getStatusLine());
            assert response.getStatusLine().getStatusCode() == 302;
        } catch (IOException e) {
            System.out.println(e);
        }
        Utilities.debug("end createPost");
    }
}
