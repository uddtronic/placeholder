package se.uddtronic.placeholder.ingestion;

public class StorageUpdate {
    private final StoredData addedItem;
    private final StoredData removedItem;
    private final boolean clear;

    public StorageUpdate(StoredData addedItem, StoredData removedItem) {
        this.addedItem = addedItem;
        this.removedItem = removedItem;
        this.clear = false;
    }

    public StorageUpdate() {
        this.addedItem = null;
        this.removedItem = null;
        this.clear = true;
    }

    public StoredData addedItem() {
        return addedItem;
    }

    public StoredData removedItem() {
        return removedItem;
    }

    public boolean isClear() {
        return clear;
    }
}
