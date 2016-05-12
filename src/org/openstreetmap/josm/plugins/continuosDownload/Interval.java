// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.continuosDownload;

public class Interval {
    public long min, max;

    public Interval(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public boolean valid() {
        return min < max;
    }

    public boolean intersects(Interval other) {
        return (max > other.min) && (min < other.max);
    }

    public long size() {
        return max - min;
    }

    public Interval union(Interval x) {
        return new Interval(Math.min(min, x.min), Math.max(max, x.max));
    }

    public Interval intersection(Interval x) {
        return new Interval(Math.max(min, x.min), Math.min(max, x.max));
    }

    public boolean contains(long d) {
        return min <= d && max > d;
    }

    @Override
    public String toString() {
        return "Interval[" + min + ", " + max + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Interval other = (Interval) obj;
        if (max != other.max)
            return false;
        if (min != other.min)
            return false;
        return true;
    }
}
