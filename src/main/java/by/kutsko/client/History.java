package by.kutsko.client;

import java.util.ArrayList;

public class History {
    private ArrayList<String> listHistory = new ArrayList<>();

    public synchronized void addText(String text) {
        listHistory.add(text);
    }

    public synchronized void clearHistory() {
        listHistory.clear();
    }

    public synchronized ArrayList<String> getListHistory() {
        return listHistory;
    }
}
