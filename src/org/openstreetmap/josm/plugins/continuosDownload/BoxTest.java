package org.openstreetmap.josm.plugins.continuosDownload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class BoxTest {

    @Test
    public void test_valid() {
        assertEquals(true, new Box(0, 0, 1, 1).valid());
        assertEquals(false, new Box(1, 1, 0, 0).valid());
    }

    @Test
    public void test_intersects() {
        assertEquals(true, new Box(0, 0, 2, 2).intersects(new Box(1, 1, 3, 3)));
        assertEquals(false, new Box(0, 0, 1, 1).intersects(new Box(1, 1, 2, 2)));
        assertEquals(false, new Box(0, 0, 1, 1).intersects(new Box(0, 1, 1, 2)));
        assertEquals(false, new Box(0, 0, 1, 1).intersects(new Box(1, 0, 2, 1)));
    }

    @Test
    public void test_equals() {
        assertEquals(new Box(1, 1, 2, 2), new Box(1, 1, 2, 2));
    }

    @Test
    public void test_intersection() {
        assertEquals(new Box(1, 1, 2, 2),
                new Box(0, 0, 2, 2).intersection(new Box(1, 1, 3, 3)));
        assertEquals(false,
                (new Box(0, 0, 1, 1).intersection(new Box(1, 1, 2, 2))).valid());
        assertEquals(false,
                (new Box(0, 0, 1, 1).intersection(new Box(0, 1, 1, 2))).valid());
    }

    @Test
    public void test_union() {
        assertEquals(new Box(0, 0, 2, 2),
                new Box(0, 0, 1, 1).union(new Box(1, 1, 2, 2)));
    }

    @Test
    public void test_inverse() {
        for (int i = 0; i < 100; i++) {
            Box x = random_box();
            for (Box y : x.inverse()) {
                assertFalse(x.intersects(y));
            }
        }
    }

    @Test
    public void test_subtract() {
        for (int i = 0; i < 100; i++) {
            Box x = random_box();
            Box y = random_box();
            for (Box b : x.substract(y)) {
                assertFalse(b.intersects(y));
                assertTrue(b.intersects(x));
            }
        }
    }

    @Test
    public void test_subtract_all() {
        for (int i = 0; i < 10; i++) {
            Box x = random_box();
            Collection<Box> array = new ArrayList<Box>(10);
            for (int j = 0; j < 10; j++) {
                array.add(random_box());
            }
            Collection<Box> subtraction = x.subtract_all(array);
            for (Box b : subtraction) {
                for (Box a : array) {
                    assertFalse(a.intersects(b));
                }
                for (Box a : subtraction) {
                    assertFalse(a != b && a.intersects(b));
                }
                assertTrue(b.intersects(x));
            }
        }
    }

    private Box random_box() {
        long minx = (long) (Math.random() * 2000 - 1000);
        long miny = (long) (Math.random() * 2000 - 1000);
        long maxx = (long) (Math.random() * 2000 - 1000);
        long maxy = (long) (Math.random() * 2000 - 1000);

        if (minx > maxx) {
            long t = minx;
            minx = maxx;
            maxx = t;
        }
        if (miny > maxy) {
            long t = miny;
            miny = maxy;
            maxy = t;
        }
        return new Box(minx, miny, maxx, maxy);
    }
}
