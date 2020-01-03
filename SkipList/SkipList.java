package psd180000;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;

/**
 *
 * Long Project 2: SkipList
 * @author Pranjal Deshmukh
 * @author Abhilasha Devkar
 * @author Aniket Pathak
 * @author Tanu Rampal
 * @see Entry which provides the basic skeletion for Skip List
 * SkipList - Generalization of sorted linked lists for implementing Dictionary ADT (add, remove,
 * contains, floor, ceiling) in O(log n) per operation "with high probability".
 */

public class SkipList<T extends Comparable<? super T>> {
    static final int PossibleLevels = 33;

    /**
     * Entry class represents the Entry<T> node of the SkipList
     * next[] representing the pointer for max level in the list
     * width[] represents the number of elements that can be spanned by next[i]
     */

    static class Entry<E> {
        E element;
        Entry[] next; //representing the pointer for max level in the list
        Entry prev;
        int level;
        int[] width;
        
        /**
         * Constructor for Entry class:
         * Takes:
         * @param x : element of the Entry (Node)
         * @param lev : level
         */
        
        public Entry(E x, int lev) {
            element = x;
            next = (Entry[]) Array.newInstance(Entry.class, lev);
            width = new int[lev];
            level = lev;            
        }
        
        /**
         * getElement() method
         * @return element of the entry
         */
        
        public E getElement() {
            return element;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                Entry o = (Entry) obj;
                if (this.getElement() != null && o.getElement() != null)
                    return this.getElement().equals(o.getElement());
            }
            return false;
        }


    }


    public class SkipListStack {
        private Entry entry;
        private int totalWidth;

        public SkipListStack(Entry entry, int totalWidth) {
            this.entry = entry;
            this.totalWidth = totalWidth;
        }


    }

    private int maximumLevel;
    private int size;
    private Entry headNode;
    private Entry tailNode;
    private Entry lastNode; 
    private Random random;
    private SkipListStack[] prev;

    /**
     * Constructor for SkipList
     * */
    public SkipList() {
        headNode = new Entry<>(null, PossibleLevels + 2);
        tailNode = new Entry<>(null, PossibleLevels + 2);
        maximumLevel = 1;
        size = 0;
        initializeSentinels();
        random = new Random();
    }

    /**
     * Method add takes:
     * @param x : to be added to the list
     * @return false if x is already present in the list or if x is null else add the element to the list and return true
     */
    public boolean add(T x) {
    	//If x is null or if x is already present in the list, return false
        if (x == null) {
            return false;
        }
        
        if (contains(x)) {
            return false;
        }
        
        int level = Math.min(chooseLevel(), PossibleLevels);
        
        maximumLevel = Math.max(maximumLevel, level);

        //Entry newNode = addElementsWithLevel(x, level, prev);
		Entry newNode = new Entry<>(x, level + 1);

        int distance = 0;

        for (int i = 0; i <= PossibleLevels; i++) {
            Entry prevNode = i < prev.length ? prev[i].entry : headNode;
            if (i <= level) {
                newNode.next[i] = prevNode.next[i];
                prevNode.next[i] = newNode;

                newNode.width[i] = Math.max(prevNode.width[i] - distance, 1);
                prevNode.width[i] = distance + 1;

                distance += prev[i].totalWidth;
            } else {
                prevNode.width[i]++;
            }
        }
		
		
        if (newNode.next[0].getElement() != null) {
            newNode.next[0].prev = newNode;
        }

        if (newNode.next[0].getElement() == null) {
            this.lastNode = newNode;
        }
        size++;
        return true;
    }
    
    /**
     * Helper Method addElementsWithLevel takes:
     * @param x : to be added to the list
     * @param level: the level 
     */
    private void addElementsWithLevel(T x, int level) {
        find(x);

		Entry newNode = new Entry<>(x, level + 1);

        int distance = 0;

        for (int i = 0; i <= PossibleLevels; i++) {
            Entry prevNode = i < prev.length ? prev[i].entry : headNode;
            if (i <= level) {
                newNode.next[i] = prevNode.next[i];
                prevNode.next[i] = newNode;

                newNode.width[i] = Math.max(prevNode.width[i] - distance, 1);
                prevNode.width[i] = distance + 1;

                distance += prev[i].totalWidth;
            } else {
                prevNode.width[i]++;
            }
        }
    }
    


    /**
     * calcDist() method takes:
     *
     * @param src : the source node
     * @param dest : the destination node
     * @return distance between source and destination node in the given list
     */

    private int calcDist(Entry<T> src, Entry<T> dest) {
        if (src == dest || src == null || dest == null) {
            return 0;
        }
        Entry<T> current = src;
        int distance = 0;
        while (current != dest) {
            for (int i = 0; i < maximumLevel; i++) {
                if (!current.next[i].equals(tailNode) && dest.getElement().compareTo((T) current.next[i].getElement()) >= 0) {
                	distance += current.width[i];
                	current = current.next[i];
                    break;
                }
            }
        }
        return distance;
    }

    /**
     * chooseLevel() method 
     * Math.min(level, maximumLevel + 1) done so that maxLevel will grow gradually
     * @return random level value 
     */

    public int chooseLevel() {
        int level = 1 + Integer.numberOfTrailingZeros(random.nextInt());
        return Math.min(level, maximumLevel + 1); 
    }

    /**
     * ceiling() method: takes:
     * @param x : To find the Ceiling value of number x in the list
     * If the list contains x, return x, else find the next greater number in the list
     * @return ceiling of the input number x
     */

    public T ceiling(T x) {
        if (contains(x)) {
            return x;
        }
        Entry next = prev[0].entry;
        if (next == null || next.next[0] == null)
            return null;
        return (T) next.next[0].getElement();
    }

    /**
     * find() method takes:
     * @param x element to be searched in the list
     * updates the next[] array which can be later used to keep track of the height of the SkipList
     */

    public void find(T x) {
        prev = (SkipListStack[])
                Array.newInstance(SkipListStack.class, Math.min(maximumLevel + 2, PossibleLevels+1));
        Entry p = headNode;
        for (int i = Math.min(PossibleLevels, maximumLevel + 1); i >= 0; i--) {
            int hop_counter = 0;
            while (p.next[i].getElement() != null && x.compareTo((T) p.next[i].getElement()) > 0) {
            	hop_counter += p.width[i];
                p = p.next[i];
            }
            prev[i] = new SkipListStack(p, hop_counter);
        }
    }

    /**
     * contains() method takes:
     * @param x : the element to be checked whether it is present in the list
     * @return true if the list contains the element, else, return false 
     */

    public boolean contains(T x) {
        find(x);
        Entry target = prev[0].entry;
        return target.next[0].getElement() != null && x.compareTo((T) target.next[0].getElement()) == 0;
    }

    /**
     * first() method:
     * @return the first element of the SkipList
     */

    public T first() {
        return (T) headNode.next[0].getElement();
    }

    /**
     * ceiling() method: takes:
     * @param x : To find the floor value of number x in the list
     * If the list contains x, return x, else find the just smaller number in the list
     * @return floor of the input number x
     */

    public T floor(T x) {
        if (contains(x)) {
            return x;
        }
        Entry node = prev[0].entry;
        return (T) node.getElement();
    }

    /**
     * get() method takes:
     * @param n: the index input
     * @return null if the index value is invalid, else, return element at index n present in the list
     **/

    public T get(int n) {
        if (n < 0 || n >= size()) {
        	return null;
        } 
        return getLog(n);
        //return getLinear(n);
    }

    private Entry[] cloneSkipheaderNode() {
        Entry[] copy = (Entry[]) Array.newInstance(Entry.class, this.size());
        Entry tempheadNode = this.headNode.next[0];
        int i = 0;
        while (tempheadNode.getElement() != null) {
            copy[i++] = tempheadNode;
            tempheadNode = tempheadNode.next[0]; // move to next level
        }
        return copy;
    }

    private void initializeSentinels() {
        for (int i = 0; i <= PossibleLevels; i++) {
            this.headNode.next[i] = tailNode;
            this.headNode.width[i] = 1;
            this.tailNode.next[i] = null;
        }
    }


    /**
     * getLinear() method takes:
     * @param n : the index value
     * @return the element present at the nth index of the list in o(n) i.e, in Linear time
     * 
     */

    public T getLinear(int n) {
        SkipListIterator<T> iterator = new SkipListIterator<>(0);
        int counter = -1;
        T element = null;
        while (counter++ < n && iterator.hasNext()) {
            element = iterator.next();
        }
        return element;
    }

    /**
     * getLog() method takes:
     * @param n : the index value
     * @return the element present at the nth index of the list in o(logn) i.e, in logarithmic time (more efficient than the linear time solution)
     */
    
    public T getLog(int n) {
        int span = 0;
        Entry node = headNode;
        for (int i = maximumLevel; i >= 0; i--) {
            while (node.next[i] != null && span + node.width[i] <= n) {
            	span += node.width[i];
                node = node.next[i];
            }
        }
        return (T) node.next[0].getElement();
    }

    /**
     * isEmpty() method:
     * @return true if the SkipList is empty (size = 0), else, return false
     */

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Iterate through the elements of list in sorted order
     */

    public Iterator<T> iterator() {
        return new SkipListIterator<>(0);
    }

    /**
     * last() method:
     * @return the last element of the SkipList
     */

    public T last() {
        return (T) this.lastNode.getElement();
    }

    /**
     * rebuild() method:
     * Reorganizes the elements of the list to make it into a Perfect SkipList
     */

    public void rebuild() throws CloneNotSupportedException {
        Entry[] copy = (Entry[]) clone();
        this.maximumLevel = (int) Math.pow(2, Math.ceil(Math.log(this.size()) / Math.log(2)) - 1);
        int nextPower = (int) Math.pow(2, Math.ceil(Math.log(this.size()) / Math.log(2)) );
        initializeSentinels();
        rebuildWithLevels(copy, 0, nextPower, maximumLevel);
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return cloneSkipheaderNode();
    }

    /**
     * rebuildWithLevels method takes:
     * @param : skipArray
     * @param: start position
     * @param : end position
     * @param: level number
     */
    private void rebuildWithLevels(Entry[] skipArray, int start, int end, int level) {

        if (start > end)
            return;


        int mid = (end - start) / 2 + start;

        if (mid < skipArray.length) 
        {
            this.addElementsWithLevel((T) skipArray[mid].getElement(), level);
        }

        rebuildWithLevels(skipArray, start, mid - 1, level - 1);

        if (mid < skipArray.length) 
        {
        	rebuildWithLevels(skipArray, mid + 1, end, level - 1);
        }
    }
    /**
     * remove() method takes:
     * @param x: The element to be removed from the SkipList
     * @return null if x is not present in the list, else, return the element removed
     */

    public T remove(T x) {
        if (!contains(x))
            return null;

        Entry ent = prev[0].entry.next[0];
        if (ent.next[0].getElement() == null) {
            this.lastNode = prev[0].entry;
        }
        for (int i = 0; i <= PossibleLevels; i++) {
            Entry prevNode = i < prev.length ? prev[i].entry : headNode;
            if (prevNode.next[i].getElement() != null && prevNode.next[i].getElement().equals(x)) {
                prevNode.width[i] += ent.width[i] - 1;
                prevNode.next[i] = ent.next[i];
            } else {
                prevNode.width[i]--;
            }

        }
        size = size--;
        return (T) ent.getElement();
    }

    public void printList() {
    	Entry<T> node = headNode;
        int i = size;
        while (i >= 0) {
            System.out.print(node.getElement() + " :");
            node = node.next[0];
            i--;
       }
    }
    /**
     * method size():
     * @return size of the skip list
     */

    public int size() {
        return size;
    }

    /**
     * Iterator implemented for the SkipList
     */

    private class SkipListIterator<T> implements Iterator<T> {
 
        Entry iter;
        int level;

        SkipListIterator(int level) {
            this.level = level;
            iter = headNode.next[level];
        }

        @Override
        public boolean hasNext() {
            if (iter != null && iter.getElement() != null) {
                return true;
            }
            return false;
        }

        @Override
        public T next() {
            Entry<T> node = null;
            if (hasNext()) {
                node = iter;
                iter = iter.next[level];
            }
            return node == null ? null : node.getElement();
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (hasNext()) {
                action.accept(next());
            }
        }
    }
}
