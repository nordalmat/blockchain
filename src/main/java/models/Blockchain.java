package models;

import java.util.ArrayList;
import java.util.HashMap;

public class Blockchain extends ArrayList<Block> {

    private static Blockchain instance = null;
    public static HashMap<String, TransactionOutput> unspentTransactionOutputs = new HashMap<>();

    private Blockchain() {}

    public static Blockchain getInstance() {
        if (instance == null) {
            instance = new Blockchain();
        }
        return instance;
    }


}
