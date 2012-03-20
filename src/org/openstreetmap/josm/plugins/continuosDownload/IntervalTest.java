package org.openstreetmap.josm.plugins.continuosDownload;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IntervalTest {

    @Test
    public void test_contains() {
        assertEquals(true, new Interval(0, 1).contains(0));
        assertEquals(false, new Interval(0, 1).contains(1));
        assertEquals(true, new Interval(0, 2).contains(1));
        assertEquals(false, new Interval(0, 1).contains(-1));
        assertEquals(false, new Interval(0, 1).contains(2));
    }

  @Test
  public void test_valid() {
        assertEquals(true, new Interval(0, 1).valid());
        assertEquals(false, new Interval(1, 0).valid());
        assertEquals(false, new Interval(0, 0).valid());
        assertEquals(false, new Interval(1, 1).valid());
  }

  @Test
  public void test_intersects() {
        assertEquals(true, new Interval(0, 2).intersects(new Interval(1, 3)));
        assertEquals(false, new Interval(0, 1).intersects(new Interval(1, 2)));
        assertEquals(false, new Interval(1, 0).intersects(new Interval(0, 2)));
        assertEquals(false, new Interval(0, 2).intersects(new Interval(3, 1)));
  }

    @Test
    public void test_union() {
        assertEquals(new Interval(0, 2),
                new Interval(0, 1).union(new Interval(1, 2)));
        assertEquals(new Interval(0, 3),
                new Interval(0, 1).union(new Interval(2, 3)));
        assertEquals(new Interval(0, 2),
                new Interval(0, 2).union(new Interval(0, 2)));
        assertEquals(new Interval(0, 1),
                new Interval(0, 1).union(new Interval(2, 1)));
  }

    @Test
  public void test_intersection() {
        assertEquals(false,
                (new Interval(0, 1).intersection(new Interval(1, 2))).valid());
        assertEquals(new Interval(0, 1),
                new Interval(-1, 1).intersection(new Interval(0, 2)));
  }

}
