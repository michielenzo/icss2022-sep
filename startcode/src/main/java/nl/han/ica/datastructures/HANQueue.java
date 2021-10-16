package nl.han.ica.datastructures;

import java.util.ArrayList;

public class HANQueue<T> implements IHANQueue {

    ArrayList<T> list = new ArrayList<>();

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public void enqueue(Object value) {
        list.add((T) value);
    }

    @Override
    public Object dequeue() {
        T value = list.get(0);
        list.remove(0);

        return value;
    }

    @Override
    public Object peek() {
        return list.get(0);
    }

    @Override
    public int getSize() {
        return list.size();
    }
}
