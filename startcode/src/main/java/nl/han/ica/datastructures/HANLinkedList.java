package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList{

    private Node<T> head;

    @Override
    public void addFirst(Object value) {

        if(head == null){
            head = new Node<T>((T) value);
        } else {
            Node<T> newNode = new Node<T>((T) value);
            newNode.previous = head;
            head = newNode;
        }
    }

    @Override
    public void clear() {
        head = null;
    }

    @Override
    public void insert(int index, Object value) {

        if (index == 0) { addFirst(value); return; }

        Node<T> lastNode = head;
        Node<T> currentNode = head.previous;
        Node<T> newNode = new Node<T>((T) value);

        for (int i = 1; i <= index; i++){
            if(i == index){
                newNode.previous = currentNode;
                lastNode.previous = newNode;
                break;
            }

            lastNode = currentNode;

            if(currentNode.previous == null){ // If the index is higher than the length.
                lastNode.previous = newNode; break; // Put it at the end of the list.
            }

            currentNode = currentNode.previous;
        }
    }

    @Override
    public void delete(int pos) {

        if(pos == 0){ head = head.previous; return; }

        Node<T> lastNode = head;
        Node<T> currentNode = head.previous;

        for(int i = 1; i <= pos; i++){
            if(i == pos){
                lastNode.previous = currentNode.previous;
            }

            lastNode = currentNode;
            currentNode = currentNode.previous;
        }
    }

    @Override
    public Object get(int pos) {

        if (pos == 0){ return head.data; }

        Node<T> currentNode = head.previous;

        for (int i = 1; i < pos; i++) {
            currentNode = currentNode.previous;
        }

        return currentNode.data;
    }

    @Override
    public void removeFirst() {
        head = head.previous;
    }

    @Override
    public Object getFirst() {
        return head.data;
    }

    @Override
    public int getSize() {

        if (head == null) return 0;

        int count = 1;
        Node<T> currentNode = head;

        while(currentNode.previous != null){
            count++;
            currentNode = currentNode.previous;
        }

        return count;
    }

    static class Node<T> {

        protected Node (T value){
            data = value;
        }

        private T data;
        private Node<T> previous;
    }
}

