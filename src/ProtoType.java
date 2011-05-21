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

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.confab.BulletinBoardEntity;
import org.confab.BulletinBoard;
import org.confab.Forum;
import org.confab.ForumThread;
import org.confab.PhpBB3Parser;
import org.confab.Post;
import org.confab.User;
import org.confab.VBulletinParser;
import org.confab.Utilities;

public class ProtoType {

    /*
    // vbulletin forums
    private VBulletinParser parser = new VBulletinParser();
    private static final BulletinBoard[] test_bbs = {
        new BulletinBoard("http://www.vbforums.com/", new User("", "")),
        //new BulletinBoard("http://www.vbforums.com/", new User("", "")),
        //new BulletinBoard("http://www.eazyeremix.com/", new User("", "")),
        //new BulletinBoard("http://www.java-forums.org/", new User("", "")),
        //new BulletinBoard("http://www.avforums.com/forums/index.php?do=switch_to_exp",
        //                  new User("", "")),
        //new BulletinBoard("http://www.xbox360achievements.org/forum/", new User("", "")),
        //new BulletinBoard("http://forum.xda-developers.com", new User("", "")), 
        //new BulletinBoard("http://www.halo3forum.com/", new User("", "")),
        //new BulletinBoard("http://www.pokecommunity.com/", new User("", "")),
        //new BulletinBoard("http://www.reefcentral.com/forums/", new User("", "")),
        //new BulletinBoard("http://forums.macrumors.com/", new User("", "")),
    }
    */
    
    // phpbb3 forums
    private PhpBB3Parser parser = new PhpBB3Parser();
    private static final BulletinBoard[] test_bbs = {
        new BulletinBoard("http://forums.mozillazine.org/", new User("", "")),
        new BulletinBoard("http://www.phpbb.com/community/", new User("", "")),
    };

    /*
    // phpbb2 forums
    private static final BulletinBoard[] test_bbs = {
        new BulletinBoard("http://www.kvraudio.com/forum/", new User("", "")),
        new BulletinBoard("http://www.theisonews.com/forums/", new User("", "")),
    };
    */ 

    private User user = new User();

    ProtoType() {
        Utilities.debug("ProtoType");

        Document root = null;

        try {
            for (int i=0; i<test_bbs.length; i++) {
                Utilities.debug(test_bbs[i].url);

                // Log in if we have credentials for this bb
                if (!test_bbs[i].user.username.isEmpty() && 
                        !test_bbs[i].user.password.isEmpty()) {
                    user.username = test_bbs[i].user.username;
                    user.password = test_bbs[i].user.password;
                    user = parser.login(test_bbs[i].url, test_bbs[i].user.username,
                                 test_bbs[i].user.password);
                    assert user.httpContext != null;
                    test_bbs[i].loggedIn = true;
                } else {
                    Utilities.debug("Missing username/password for " + test_bbs[i].url + 
                            " , skipping login");
                }
                
                // Test reply to post
                if (false) {
                    testCreatePost();
                }

                // Test create thread
                if (false) {
                    testCreateThread();
                }

                // Load the root html
                Utilities.debug("Loading " + test_bbs[i].url);
                root = Jsoup.connect(test_bbs[i].url).timeout(3000).userAgent("Mozilla").get();
                assert root != null : "Problem loading " + test_bbs[i].url;
                Utilities.debug("loaded html into jsoup");
        
                // Parse forum list
                List<Forum> forums = new ArrayList<Forum>();
                forums.addAll(parser.parseForums(root, test_bbs[i]));

                // For each forum, parse topics
                //TODO: modify loop to check random % of forums
                for (Forum f : forums) {
                    Utilities.debug("Loading forum: " + f.fullURL());
                    root = Jsoup.connect(f.fullURL()).timeout(3000).userAgent("Mozilla").get(); 
                    assert root != null : "Problem loading " + f.fullURL();
                    f.topics.addAll(parser.parseForumThreads(root, f));
                    break; // just test the first one
                }

                /* 
                // For each topic, parse posts
                for (Forum f : forums) {
                    for (ForumThread t : f.topics) {
                        Utilities.debug("Loading thread: " + t.fullURL());
                        root = Jsoup.connect(t.fullURL()).timeout(3000).userAgent("Mozilla").get(); 
                        assert root != null : "Problem loading " + t.fullURL();
                        t.posts.addAll(parser.parsePosts(root, t));
                    }
                }
                */
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        Utilities.debug("end ProtoType");
    }
    
    private void testCreatePost() {
        Forum vbforums_test_forum = new Forum(test_bbs[0]);
        ForumThread vbforums_test_thread = new ForumThread(vbforums_test_forum);
        vbforums_test_thread.id = "646852";
        vbforums_test_thread.url = "showthread.php?t=646852";

        Post replyTo = new Post(vbforums_test_thread);
        replyTo.id = "3992986";

        Post test_reply = new Post(vbforums_test_thread);
        test_reply.message = "foobar";

        parser.createPost(replyTo, test_reply, user);
    }
    
    private void testCreateThread() {
        Forum vbforums_test_forum = new Forum(test_bbs[0]);
        vbforums_test_forum.id = "39";
        vbforums_test_forum.url = "forumdisplay.php?f=39";

        ForumThread new_topic = new ForumThread(vbforums_test_forum);
        new_topic.title = "Hello world";

        Post first_post = new Post(new_topic);
        first_post.message = "another test thread..";

        parser.postForumThread(vbforums_test_forum, first_post, user);
    } 
   
    public static void main(String[] args) {
        new ProtoType();
    }
}
