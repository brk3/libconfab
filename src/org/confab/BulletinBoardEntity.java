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

public abstract class BulletinBoardEntity {
    public BulletinBoardEntity parent = null;
    public String url;
    public String title;
    public String id;

    public BulletinBoardEntity() {
        url = new String();
        title = new String();
        id = new String();
    }    

    /**
     * Recursively works its way up the hierarchy to build the fully qualified
     * url for this entity.
     * @return  Fully qualified url for this entity.
     */
    public String fullURL() {
        return fullURL(this, "");
    }

    private String fullURL(BulletinBoardEntity e, String ret) {
        if (e.parent == null) {
            return ret;
        }
        return fullURL(e.parent, e.parent.url+this.url);
    }

    public String rootURL() {
        return rootURL(this);
    }

    private String rootURL(BulletinBoardEntity e) {
        if (e.parent == null) {
            System.out.println("returning: " + e.url);
            return e.url;
        }
        return rootURL(e.parent);
    }
}
