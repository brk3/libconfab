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

public class Post extends BulletinBoardEntity {
    public User author;
    public String message;
    public ForumThread parent;

    public Post(ForumThread parent) {
        super();
        this.parent = parent;
        this.url = parent.url;
        author = new User();
        message = new String();
    }
}
