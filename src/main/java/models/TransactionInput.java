package models;

public class TransactionInput {
    private final String transactionOutputId;
    private TransactionOutput unsentTransactionOutput;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionOutput getUnsentTransactionOutput() {
        return unsentTransactionOutput;
    }

    public void setUnsentTransactionOutput(TransactionOutput unsentTransactionOutput) {
        this.unsentTransactionOutput = unsentTransactionOutput;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }
}
