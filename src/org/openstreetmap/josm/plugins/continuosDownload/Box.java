// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.continuosDownload;

/*
 * Original code written by zere
 */

import java.util.ArrayList;
import java.util.Collection;

/*
 * a two-dimensional half-closed interval, or bounding box.
 */
public class Box {

    public Interval x;
    public Interval y;

    /*
     * args are either (minx, miny, maxx, maxy) or two intervals (x, y).
     */
    public Box(long minx, long miny, long maxx, long maxy) {
        x = new Interval(minx, maxx);
        y = new Interval(miny, maxy);
    }

    /*
     * args are either (minx, miny, maxx, maxy) or two intervals (x, y).
     */
    public Box(Interval x, Interval y) {
        this.x = x;
        this.y = y;
    }

    /*
     * if this box has any area, whether it contains a valid amount of space.
     */
    public boolean valid() {
        return x.valid() && y.valid();
    }

    /*
     * whether this box intersects another.
     */
    public boolean intersects(Box other) {
        return x.intersects(other.x) && y.intersects(other.y);
    }

    /*
     * intersection. may return a box that isn't valid.
     */
    public Box intersection(Box other) {
        return new Box(x.intersection(other.x), y.intersection(other.y));
    }

    /*
     * union. return a Box covering this Box and the other
     */
    public Box union(Box other) {
        return new Box(x.union(other.x), y.union(other.y));
    }

    /*
     * inverse. returns an array of 8 Boxes covering all space except for this
     * box.
     */
    public Collection<Box> inverse() {
        long inf = Long.MAX_VALUE;
        ArrayList<Box> r = new ArrayList<Box>(8);
        r.add(new Box(-inf, y.max, x.min, inf)); // Top left
        r.add(new Box(x.min, y.max, x.max, inf)); // Top
        r.add(new Box(x.max, y.max, inf, inf)); // Top right
        r.add(new Box(-inf, y.min, x.min, y.max)); // Left
        r.add(new Box(x.max, y.min, inf, y.max)); // Right
        r.add(new Box(-inf, -inf, x.min, y.min)); // Bottom left
        r.add(new Box(x.min, -inf, x.max, y.min)); // Bottom
        r.add(new Box(x.max, -inf, inf, y.min)); // Bottom right
        return r;
    }

    /*
     * subtraction. take the inverse of one bbox and intersect it with this one.
     * returns an array of Boxes.
     */
    public Collection<Box> substract(Box other) {
        Collection<Box> r = new ArrayList<Box>();
        for (Box box : other.inverse()) {
            Box b = this.intersection(box);
            if (b.valid()) {
                r.add(b);
            }
        }
        return r;
    }

    /*
     * subtract all Boxes in given array. resulting set of boxes will be
     * disjoint.
     */
    public Collection<Box> subtract_all(Collection<Box> others) {
        Collection<Box> memo = new ArrayList<Box>();
        memo.add(this);
        for (Box other : others) {
            Collection<Box> subtracted = new ArrayList<Box>();
            for (Box b : memo) {
                subtracted.addAll(b.substract(other));
            }
            memo = subtracted;
        }
        // do we need to flatten memo here?
        return memo;
    }

    /*
     * merge as many boxes as possible without increasing the total area of the
     * set of boxes.
     */
    public static Collection<Box> merge(Collection<Box> boxes) {
        /*
         * this is an O(n^2) algorithm, so it's going to be very slow on large
         * numbers of boxes. there's almost certainly a better algorithm out
         * there to do the same thing in better time. but it's nice and simple.
         */
        if (boxes.isEmpty())
            return new ArrayList<>();

        Box first = null;
        ArrayList<Box> kept = new ArrayList<>();
        for (Box box : boxes) {
            if (first == null) {
                first = box;
            } else {
                Box union = first.union(box);
                if (union.size() <= first.size() + box.size()) {
                    first = union;
                } else {
                    kept.add(box);
                }
            }
        }

        Collection<Box> r = Box.merge(kept);
        r.add(first);

        return r;
    }

    public boolean equals(Box other) {
        return x.equals(other.x) && y.equals(other.y);
    }

    /**
     * returns the area of the box
     */
    public long size() {
        return x.size() * y.size();
    }

    @Override
    public String toString() {
        return "Box[" + x.min + "," + y.min + "," + x.max + "," + y.max + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Box other = (Box) obj;
        if (x == null) {
            if (other.x != null)
                return false;
        } else if (!x.equals(other.x))
            return false;
        if (y == null) {
            if (other.y != null)
                return false;
        } else if (!y.equals(other.y))
            return false;
        return true;
    }
}
