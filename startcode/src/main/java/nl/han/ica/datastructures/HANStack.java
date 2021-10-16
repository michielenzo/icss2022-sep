package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.List;

public class HANStack<T> implements IHANStack{

    List<T> list = new ArrayList<>();

    @Override
    public void push(Object value) {
        list.add((T) value);
    }

    @Override
    public Object pop() {
        T value = list.get(list.size() - 1);
        list.remove(list.size() - 1);
        return value;
    }

    @Override
    public Object peek() {
        return list.get(list.size() - 1);
    }
}
