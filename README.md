Background:
-----------
libconfab is a concept that I intend(ed) to use to build an Android app similar
to Tapatalk (www.tapatalk.com).  Tapatalk is a great app but suffers from the
problem that many/most forum admins seem very reluctant to install the
necessary plugins to enable their api to work.

Assuming that the majority of web forums today are running either VBulletin or
phpbb, this project aims to solve the above problem by building a robust parser
to extract information.

Popular forums that don't run Vbulletin/phpbb could possibly be supported
through a plugin/template system.

Overview:
---------
Currently, the following basic functions are implemented (list may not be up to
date):

```
 - Parse forum list                     ✓         ✓
     - Title                            ✓         x
     - Subforums                        ✓         ✓
     - Description                      ✓         ✓
     - URL                              ✓         x
 - Login                                ✓         x
 - Parse topics from a forum            ✓         ✓
     - Title                            ✓         ✓
     - URL                              ✓         x
 - Parse posts from a topic             ✓         x
   - Author                             ✓         x
   - Content                            ✓         x
 - Reply to post                        ✓         x
 - Post new topic                       ✓         x
```

The main problem that has slowed down development is that a lot of forums use a
completely customized frontend template, or forum list.  The content beneath
these seems mostly the same, but until I can find a way to handle the variety
in these, you're stuck with very variable results depending on the site you're
trying to parse.
