///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Rob Eden All Rights Reserved.
// Copyright (c) 2009, Jeff Randall All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package com.alibaba.fastjson2.internal.trove.map.hash;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * An open addressed Map implementation for long keys and int values.
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 */
public class TLongIntHashMap {
    static final int largestPrime;
    static final int[] primeCapacities = {
            //chunk #1
            5, 11, 23, 47, 97, 197, 397, 797, 1597, 3203, 6421, 12853, 25717, 51437, 102877, 205759,
            411527, 823117, 1646237, 3292489, 6584983, 13169977, 26339969, 52679969, 105359939,
            210719881, 421439783, 842879579, 1685759167,

            //chunk #2
            433, 877, 1759, 3527, 7057, 14143, 28289, 56591, 113189, 226379, 452759, 905551, 1811107,
            3622219, 7244441, 14488931, 28977863, 57955739, 115911563, 231823147, 463646329, 927292699,
            1854585413,

            //chunk #3
            953, 1907, 3821, 7643, 15287, 30577, 61169, 122347, 244703, 489407, 978821, 1957651, 3915341,
            7830701, 15661423, 31322867, 62645741, 125291483, 250582987, 501165979, 1002331963,
            2004663929,

            //chunk #4
            1039, 2081, 4177, 8363, 16729, 33461, 66923, 133853, 267713, 535481, 1070981, 2141977, 4283963,
            8567929, 17135863, 34271747, 68543509, 137087021, 274174111, 548348231, 1096696463,

            //chunk #5
            31, 67, 137, 277, 557, 1117, 2237, 4481, 8963, 17929, 35863, 71741, 143483, 286973, 573953,
            1147921, 2295859, 4591721, 9183457, 18366923, 36733847, 73467739, 146935499, 293871013,
            587742049, 1175484103,

            //chunk #6
            599, 1201, 2411, 4831, 9677, 19373, 38747, 77509, 155027, 310081, 620171, 1240361, 2480729,
            4961459, 9922933, 19845871, 39691759, 79383533, 158767069, 317534141, 635068283, 1270136683,

            //chunk #7
            311, 631, 1277, 2557, 5119, 10243, 20507, 41017, 82037, 164089, 328213, 656429, 1312867,
            2625761, 5251529, 10503061, 21006137, 42012281, 84024581, 168049163, 336098327, 672196673,
            1344393353,

            //chunk #8
            3, 7, 17, 37, 79, 163, 331, 673, 1361, 2729, 5471, 10949, 21911, 43853, 87719, 175447, 350899,
            701819, 1403641, 2807303, 5614657, 11229331, 22458671, 44917381, 89834777, 179669557,
            359339171, 718678369, 1437356741,

            //chunk #9
            43, 89, 179, 359, 719, 1439, 2879, 5779, 11579, 23159, 46327, 92657, 185323, 370661, 741337,
            1482707, 2965421, 5930887, 11861791, 23723597, 47447201, 94894427, 189788857, 379577741,
            759155483, 1518310967,

            //chunk #10
            379, 761, 1523, 3049, 6101, 12203, 24407, 48817, 97649, 195311, 390647, 781301, 1562611,
            3125257, 6250537, 12501169, 25002389, 50004791, 100009607, 200019221, 400038451, 800076929,
            1600153859
    };

    static { //initializer
        // The above prime numbers are formatted for human readability.
        // To find numbers fast, we sort them once and for all.

        Arrays.sort(primeCapacities);
        largestPrime = primeCapacities[primeCapacities.length - 1];
    }

    static int nextPrime(int desiredCapacity) {
        if (desiredCapacity >= largestPrime) {
            return largestPrime;
        }
        int i = Arrays.binarySearch(primeCapacities, desiredCapacity);
        if (i < 0) {
            // desired capacity not found, choose next prime greater
            // than desired capacity
            i = -i - 1; // remember the semantics of binarySearch...
        }
        return primeCapacities[i];
    }

    protected int[] values;
    protected long[] set;
    protected boolean consumeFreeSlot;
    protected int size;
    protected int free;
    protected int maxSize;

    /**
     * Creates a new <code>TLongIntHashMap</code> instance with the default
     * capacity and load factor.
     */
    public TLongIntHashMap() {
        int capacity = 37;
        maxSize = 18;
        free = capacity; // reset the free element count

        set = new long[capacity];
        values = new int[capacity];
    }

    public TLongIntHashMap(long key, int value) {
        int capacity = 37;
        maxSize = 18;

        set = new long[capacity];
        values = new int[capacity];

        int hash = ((int) (key ^ (key >>> 32))) & 0x7fffffff;
        int index = hash % set.length;
        consumeFreeSlot = true;
        set[index] = key;

        values[index] = value;

        free = capacity - 1;
        size = 1;
    }

    public void put(long key, int value) {
//        int index = insertKey( key );
        int index;
        {
            int hash = ((int) (key ^ (key >>> 32))) & 0x7fffffff;
            index = hash % set.length;
            long setKey = set[index];

            consumeFreeSlot = false;

            if (setKey == 0) {
                consumeFreeSlot = true;
//                insertKeyAt(index, key);
                set[index] = key;
            } else if (setKey == key) {
                index = -index - 1;   // already stored
            } else {
                // already FULL or REMOVED, must probe
//                index = insertKeyRehash(key, index, hash);
                {
                    // compute the double hash
                    final int length = set.length;
                    int probe = 1 + (hash % (length - 2));
                    final int loopIndex = index;

                    /**
                     * Look until FREE slot or we start to loop
                     */
                    do {
                        index -= probe;
                        if (index < 0) {
                            index += length;
                        }
                        setKey = set[index];

                        // A FREE slot stops the search
                        if (setKey == 0) {
                            consumeFreeSlot = true;
                            set[index] = key;  // insert value
                            break;
                        }

                        if (setKey == key) {
                            index = -index - 1;
                            break;
                        }

                        // Detect loop
                    } while (index != loopIndex);
                }
            }
        }

        boolean isNewMapping = true;
        if (index < 0) {
            index = -index - 1;
            isNewMapping = false;
        }
        values[index] = value;

        if (isNewMapping) {
            if (consumeFreeSlot) {
                free--;
            }

            // rehash whenever we exhaust the available space in the table
            if (++size > maxSize || free == 0) {
                // choose a new capacity suited to the new state of the table
                // if we've grown beyond our maximum size, double capacity;
                // if we've exhausted the free spots, rehash to the same capacity,
                // which will free up any stale removed slots for reuse.
                int capacity = set.length;
                int newCapacity = size > maxSize ? nextPrime(capacity << 1) : capacity;
//                rehash(newCapacity);
                {
                    int oldCapacity = set.length;

                    long[] oldKeys = set;
                    int[] oldVals = values;

                    set = new long[newCapacity];
                    values = new int[newCapacity];

                    for (int i = oldCapacity; i-- > 0; ) {
                        if (oldKeys[i] != 0) {
                            long o = oldKeys[i];
                            index = insertKey(o);
                            values[index] = oldVals[i];
                        }
                    }
                }

                capacity = set.length;
                // computeMaxSize(capacity);
                maxSize = Math.min(capacity - 1, (int) (capacity * 0.5f));
                free = capacity - size; // reset the free element count
            }
        }
    }

    public int putIfAbsent(long key, int value) {
//        int index = insertKey( key );
        int index;
        {
            int hash = ((int) (key ^ (key >>> 32))) & 0x7fffffff;
            index = hash % set.length;
            long setKey = set[index];

            consumeFreeSlot = false;

            if (setKey == 0) {
                consumeFreeSlot = true;
//                insertKeyAt(index, key);
                set[index] = key;
            } else if (setKey == key) {
                index = -index - 1;   // already stored
            } else {
                // already FULL or REMOVED, must probe
//                index = insertKeyRehash(key, index, hash);
                {
                    // compute the double hash
                    final int length = set.length;
                    int probe = 1 + (hash % (length - 2));
                    final int loopIndex = index;

                    /**
                     * Look until FREE slot or we start to loop
                     */
                    do {
                        index -= probe;
                        if (index < 0) {
                            index += length;
                        }
                        setKey = set[index];

                        // A FREE slot stops the search
                        if (setKey == 0) {
                            consumeFreeSlot = true;
                            set[index] = key;  // insert value
                            break;
                        }

                        if (setKey == key) {
                            index = -index - 1;
                            break;
                        }

                        // Detect loop
                    } while (index != loopIndex);
                }
            }
        }

        if (index < 0) {
            return values[-index - 1];
        }

        boolean isNewMapping = true;
        if (index < 0) {
            index = -index - 1;
            isNewMapping = false;
        }
        values[index] = value;

        if (isNewMapping) {
            if (consumeFreeSlot) {
                free--;
            }

            // rehash whenever we exhaust the available space in the table
            if (++size > maxSize || free == 0) {
                // choose a new capacity suited to the new state of the table
                // if we've grown beyond our maximum size, double capacity;
                // if we've exhausted the free spots, rehash to the same capacity,
                // which will free up any stale removed slots for reuse.
                int capacity = set.length;
                int newCapacity = size > maxSize ? nextPrime(capacity << 1) : capacity;
//                rehash(newCapacity);
                {
                    int oldCapacity = set.length;

                    long[] oldKeys = set;
                    int[] oldVals = values;

                    set = new long[newCapacity];
                    values = new int[newCapacity];

                    for (int i = oldCapacity; i-- > 0; ) {
                        if (oldKeys[i] != 0) {
                            long o = oldKeys[i];
                            index = insertKey(o);
                            values[index] = oldVals[i];
                        }
                    }
                }

                capacity = set.length;
                // computeMaxSize(capacity);
                maxSize = Math.min(capacity - 1, (int) (capacity * 0.5f));
                free = capacity - size; // reset the free element count
            }
        }

        return value;
    }

    public int get(long key) {
        final int DEFAULT_ENTRY_VALUE = -1;

        int length = set.length;
        int hash = ((int) (key ^ (key >>> 32))) & 0x7fffffff;
        int index = hash % length;
        long setKey = set[index];

        if (setKey == 0) {
            return DEFAULT_ENTRY_VALUE;
        }

        if (setKey == key) {
            return values[index];
        }

        // indexRehashed
        int setLength = set.length;
        int probe = 1 + (hash % (setLength - 2));
        final int loopIndex = index;

        do {
            index -= probe;
            if (index < 0) {
                index += setLength;
            }
            setKey = set[index];

            if (setKey == 0) {
                return DEFAULT_ENTRY_VALUE;
            }

            if (key == set[index]) {
                return values[index];
            }
        } while (index != loopIndex);

        return DEFAULT_ENTRY_VALUE;
    }

    public boolean forEachEntry(BiFunction<Long, Integer, Boolean> procedure) {
        long[] keys = set;
        int[] values = this.values;
        for (int i = keys.length; i-- > 0; ) {
            if (set[i] != 0 && !procedure.apply(keys[i], values[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("{");
        forEachEntry(new BiFunction<Long, Integer, Boolean>() {
            private boolean first = true;

            @Override
            public Boolean apply(Long key, Integer value) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", ");
                }
                buf.append(key);
                buf.append("=");
                buf.append(value);
                return true;
            }
        });
        buf.append("}");
        return buf.toString();
    }

    public int size() {
        return size;
    }

    /**
     * Locates the index at which <tt>val</tt> can be inserted.  if
     * there is already a value equal()ing <tt>val</tt> in the set,
     * returns that value as a negative integer.
     *
     * @param key an <code>long</code> value
     * @return an <code>int</code> value
     */
    protected int insertKey(long key) {
        int hash, index;

        hash = ((int) (key ^ (key >>> 32))) & 0x7fffffff;
        index = hash % set.length;
        boolean state = set[index] != 0;

        consumeFreeSlot = false;

        if (!state) {
            consumeFreeSlot = true;
//            insertKeyAt(index, key);
            set[index] = key;  // insert value

            return index;      // empty, all done
        }

        if (state && set[index] == key) {
            return -index - 1;   // already stored
        }

        // already FULL or REMOVED, must probe
//        return insertKeyRehash(key, index, hash);
        {
            // compute the double hash
            final int length = set.length;
            int probe = 1 + (hash % (length - 2));
            final int loopIndex = index;

            /**
             * Look until FREE slot or we start to loop
             */
            do {
                index -= probe;
                if (index < 0) {
                    index += length;
                }

                state = set[index] != 0;

                // A FREE slot stops the search
                if (!state) {
                    consumeFreeSlot = true;
//                    insertKeyAt(index, val);
                    set[index] = key;  // insert value
                    return index;
                }

                if (state && set[index] == key) {
                    return -index - 1;
                }

                // Detect loop
            } while (index != loopIndex);

            // Can a resizing strategy be found that resizes the set?
            throw new IllegalStateException("No free or removed slots available. Key set full?!!");
        }
    }
} // TLongIntHashMap
