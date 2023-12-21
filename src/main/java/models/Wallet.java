package models;

import java.math.BigDecimal;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final Map<String, TransactionOutput> walletUnspentTransactionOutputs = new HashMap<>();

    public Wallet() {
        generateKeyPair();
    }

    public BigDecimal getBalance() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, TransactionOutput> entry : Blockchain.unspentTransactionOutputs.entrySet()) {
            TransactionOutput unspentTransaction = entry.getValue();
            if (unspentTransaction.isMine(publicKey)) {
                walletUnspentTransactionOutputs.put(unspentTransaction.getHash(), unspentTransaction);
                total = total.add(unspentTransaction.getAmount());
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey recipient, BigDecimal amount) {
        if (getBalance().compareTo(amount) < 0) {
            System.out.println("#Not Enough funds to send transaction. Transaction denied.");
            return null;
        }
        List<TransactionInput> inputList = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, TransactionOutput> entry : walletUnspentTransactionOutputs.entrySet()) {
            TransactionOutput unspentTransaction = entry.getValue();
            total = total.add(unspentTransaction.getAmount());
            inputList.add(new TransactionInput(unspentTransaction.getHash()));
            if (total.compareTo(amount) > 0) break;
        }
        Transaction newTransaction = new Transaction(publicKey, recipient, amount, inputList);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputList) {
            walletUnspentTransactionOutputs.remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            keyPairGenerator.initialize(ecSpec, random);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
