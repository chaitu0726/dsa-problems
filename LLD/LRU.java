class LRUCache {

    private class Node{
        int key;
        int value;
        Node prev;
        Node next;
    }

    private final Map<Integer,Node> map;
    private final Node head;
    private final Node tail;
    private final int capacity;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>(capacity);
        this.head = new Node();
        this.tail = new Node();

        this.head.next = this.tail;
        this.tail.prev = this.head;
    }

    private Node createNode(int key, int value){
        Node node = new Node();
        node.key = key;
        node.value = value;
        node.prev = null;
        node.next = null;
        return node;
    }

    private void removeLRU(){
        Node node = tail.prev;
        node.prev.next = tail;
        tail.prev = node.prev;
        map.remove(node.key);
    }

    private void updateMRU(Node node){
        node.prev.next = node.next;
        node.next.prev = node.prev;

        node.next = head.next;
        node.prev = head;

        head.next.prev = node;
        head.next = node;

        return;
    }
    
    public int get(int key) {
        if(map.get(key) != null){
            Node node = map.get(key);
            updateMRU(node);
            return node.value;
        }
        return -1;
    }
    
    public void put(int key, int value) {
        if(map.get(key) != null){
            Node node = map.get(key);
            node.value = value;
            updateMRU(node);
            return;
        }
        if(map.size() == capacity){
            removeLRU();
        }
        Node node = createNode(key,value);
        node.prev = head;
        node.next = head.next;

        head.next.prev = node;
        head.next = node;
        map.put(key,node);
    }
}
